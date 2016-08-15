package org.apache.kafka.common.memory;

import java.nio.ByteBuffer;


/**
 * a non-blocking memory pool interface
 */
public interface BufferPool {
  /**
   * tries to acquire a ByteBuffer of the specified size
   * @param sizeBytes size required
   * @return a ByteBuffer (which later needs to be release()ed), or null if no memory available.
   *         the buffer will be of the exact size requested, even if backed by a larger chunk of memory
   */
  ByteBuffer tryAllocate(long sizeBytes);

  /**
   * returns a previously allocated buffer to the pool.
   * @param previouslyAllocated a buffer previously returned from tryAllocate()
   */
  void release(ByteBuffer previouslyAllocated);
}
