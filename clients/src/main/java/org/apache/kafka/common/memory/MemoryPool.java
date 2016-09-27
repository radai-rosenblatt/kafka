/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kafka.common.memory;

import java.nio.ByteBuffer;


/**
 * A common memory pool interface
 */
public interface MemoryPool {
    MemoryPool NONE = new MemoryPool() {
        @Override
        public ByteBuffer tryAllocate(int sizeBytes) {
            return ByteBuffer.allocate(sizeBytes);
        }

        @Override
        public void release(ByteBuffer previouslyAllocated) {
            //nop
        }

        @Override
        public boolean isLowOnMemory() {
            return false;
        }

        @Override
        public boolean isOutOfMemory() {
            return false;
        }
    };

    /**
     * Tries to acquire a ByteBuffer of the specified size
     * @param sizeBytes size required
     * @return a ByteBuffer (which later needs to be release()ed), or null if no memory available.
     *         the buffer will be of the exact size requested, even if backed by a larger chunk of memory
     */
    ByteBuffer tryAllocate(int sizeBytes);

    /**
     * Returns a previously allocated buffer to the pool.
     * @param previouslyAllocated a buffer previously returned from tryAllocate()
     */
    void release(ByteBuffer previouslyAllocated);

    boolean isLowOnMemory();

    boolean isOutOfMemory();
}
