package org.apache.kafka.common.memory;

import java.nio.ByteBuffer;


public class NopBufferPool implements BufferPool {
  @Override
  public ByteBuffer tryAllocate(long sizeBytes) {
    return ByteBuffer.allocate((int) sizeBytes);
  }

  @Override
  public void release(ByteBuffer previouslyAllocated) {

  }
}
