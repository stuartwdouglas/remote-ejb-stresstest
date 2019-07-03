/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.server;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ByteArrayUtilities {

    Map<Integer, ByteBuffer> byteBuffers = new ConcurrentHashMap<>();

    private ByteArrayUtilities() {
        // here we could pre-initialize common sizes...
    }
    
    private static class StaticHolder {
        static final ByteArrayUtilities INSTANCE = new ByteArrayUtilities();
    }
    
    public static ByteArrayUtilities getInstance() {
        return StaticHolder.INSTANCE;
    }

    /**
     * Get a byte[] with a size of <i>size</i> kilobyte
     * @param size in kb
     * @return
     */
    public byte[] getByteArray(int size) {

        int kbSize = size * 1024;

        ByteBuffer bb = byteBuffers.get(Integer.valueOf(size));
        
        if(bb == null) {
            byte[] bytes = (byte[])Array.newInstance(byte.class, kbSize);
            ThreadLocalRandom.current().nextBytes(bytes);
            byteBuffers.put(Integer.valueOf(size), ByteBuffer.wrap(bytes));
            
            return bytes;
        }
        
        else return bb.array();
    }
}
