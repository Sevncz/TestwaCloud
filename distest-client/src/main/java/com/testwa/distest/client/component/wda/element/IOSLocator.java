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

package com.testwa.distest.client.component.wda.element;

public class IOSLocator {

    private Selector selector;
    private String value;

    private IOSLocator(Selector selector, String value) {
        this.selector = selector;
        this.value = value;
    }

    public static IOSLocator id(String value) {
        return new IOSLocator(Selector.ID, value);
    }

    public static IOSLocator name(String value) {
        return new IOSLocator(Selector.NAME, value);
    }

    public static IOSLocator accessibilityId(String value) {
        return new IOSLocator(Selector.ACCESSIBILITY_ID, value);
    }

    public static IOSLocator predicate(String value) {
        return new IOSLocator(Selector.NS_PREDICATE, value);
    }

    public static IOSLocator classChain(String value) {
        return new IOSLocator(Selector.CLASS_CHAIN, value);
    }

    public static IOSLocator className(String value) {
        return new IOSLocator(Selector.CLASS_NAME, value);
    }

    public static IOSLocator linkText(String value) {
        return new IOSLocator(Selector.LINK_TEXT, value);
    }

    public static IOSLocator xpath(String value) {
        return new IOSLocator(Selector.XPATH, value);
    }

    public Selector getSelector() {
        return this.selector;
    }

    public String getValue() {
        return this.value;
    }

    public enum Selector {
        ID("id"),
        NAME("name"),
        ACCESSIBILITY_ID("accessibility id"),
        NS_PREDICATE("predicate string"),
        CLASS_CHAIN("class chain"),
        CLASS_NAME("class name"),
        LINK_TEXT("link text"),
        XPATH("xpath");

        private String key;

        Selector(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

    }
}
