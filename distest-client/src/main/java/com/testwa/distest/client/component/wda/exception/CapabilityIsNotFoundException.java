/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.testwa.distest.client.component.wda.exception;

public class CapabilityIsNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -7125019401443628508L;

    private static final String MESSAGE_FORMAT = "Capability is not found: %s";

    public CapabilityIsNotFoundException(String capabilityName) {
        super(String.format(MESSAGE_FORMAT, capabilityName));
    }
}
