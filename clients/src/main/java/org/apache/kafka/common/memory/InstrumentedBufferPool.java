package org.apache.kafka.common.memory;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NOT FOR PRODUCTION USE. this implementation is a development aid
 */
public class InstrumentedBufferPool implements BufferPool {
    private final BufferPool delegate;
    private final ReferenceQueue<ByteBuffer> garbageCollectedBuffers = new ReferenceQueue<>();
    private final Map<Integer, BufferStats> buffersInFlight = new ConcurrentHashMap<>();

    public InstrumentedBufferPool(BufferPool delegate) {
        this.delegate = delegate;
        Thread watchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferReference ref = (BufferReference) garbageCollectedBuffers.remove();
                        int identity = ref.getBufferIdentity();
                        BufferStats stats = buffersInFlight.remove(identity);
                        if (stats != null) {
                            System.err.println("found a leaked buffer"); //TODO - log.fatal, add timestamp and site info
                        }
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            }
        }, "buffer pool watchdog");
        watchdog.setDaemon(true); //so wont keep the JVM up and dont need to worry about shutdown
        watchdog.start();
    }

    @Override
    public ByteBuffer tryAllocate(long sizeBytes) {
        ByteBuffer buffer = delegate.tryAllocate(sizeBytes);
        if (buffer == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        Exception allocationSite = new Exception();
        BufferStats stats = new BufferStats(now, allocationSite);
        BufferReference ref = new BufferReference(buffer, garbageCollectedBuffers);
        buffersInFlight.put(ref.getBufferIdentity(), stats);
        return buffer;
    }

    @Override
    public void release(ByteBuffer previouslyAllocated) {
        int identity = System.identityHashCode(previouslyAllocated);
        BufferStats stats = buffersInFlight.remove(identity);
        if (stats == null) {
            throw new IllegalArgumentException("attempted to release an unknown buffer");
        }
        delegate.release(previouslyAllocated);
    }


    private static class BufferReference extends PhantomReference<ByteBuffer> {
        private final int bufferIdentity;
        public BufferReference(ByteBuffer referent, ReferenceQueue<? super ByteBuffer> q) {
            super(referent, q);
            bufferIdentity = System.identityHashCode(referent);
        }

        public int getBufferIdentity() {
            return bufferIdentity;
        }
    }

    private static class BufferStats {
        private final long allocationTimestamp;
        private final Exception allocationSite;

        public BufferStats(long allocationTimestamp, Exception allocationSite) {
            this.allocationTimestamp = allocationTimestamp;
            this.allocationSite = allocationSite;
        }

        public long getAllocationTimestamp() {
            return allocationTimestamp;
        }

        public Exception getAllocationSite() {
            return allocationSite;
        }
    }
}
