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

@SuppressWarnings("unused")
public class UnserializableData {

    private String thread;
    private String uuid;
    private int result;

    public UnserializableData(String string, String uuid, int i) {
        this.thread = string;
        this.uuid = uuid;
        this.result = i;
    }
    
    public int getResult() {
        return this.result;
    }    
}
