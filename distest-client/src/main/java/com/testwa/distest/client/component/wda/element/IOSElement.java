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

import com.google.common.collect.ImmutableMap;
import com.testwa.distest.client.component.wda.driver.CommandExecutor;
import com.testwa.distest.client.component.wda.driver.OutputImageType;
import com.testwa.distest.client.component.wda.remote.RemoteResponse;
import com.testwa.distest.client.component.wda.remote.WDACommand;
import com.testwa.distest.client.component.wda.support.ResponseValueConverter;

import java.awt.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IOSElement implements Element {

    private CommandExecutor commandExecutor;
    private String sessionId;
    private String uid;

    public IOSElement(String sessionId, String uid, CommandExecutor commandExecutor) {
        this.sessionId = sessionId;
        this.uid = uid;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void select() {
        execute(WDACommand.ELEMENT_SELECT);
    }

    @Override
    public void focuse() {
        execute(WDACommand.ELEMENT_FOCUSE);
    }

    @Override
    public void setValue(String value) {
        execute(WDACommand.SET_VALUE, new EnumMap<>(WDACommand.Wildcard.class),
                ImmutableMap.of(WDACommand.Parameter.VALUE.getKey(), value));
    }

    @Override
    public void clear() {
        execute(WDACommand.CLEAR_ELEMENT);
    }

    @Override
    public String getAttribute(String name) {
        Map<WDACommand.Wildcard, String> wildcards = new EnumMap<>(WDACommand.Wildcard.class);
        wildcards.put(WDACommand.Wildcard.NAME, name);
        return (String) execute(WDACommand.GET_ELEMENT_ATTRIBUTE, wildcards).getValue();
    }

    @Override
    public boolean isEnabled() {
        return (Boolean) execute(WDACommand.IS_ELEMENT_ENABLED).getValue();
    }

    @Override
    public boolean isFocused() {
        return (Boolean) execute(WDACommand.IS_ELEMENT_FOCUSED).getValue();
    }

    @Override
    public String getText() {
        return (String) execute(WDACommand.GET_ELEMENT_TEXT).getValue();
    }

    @Override
    public List<Element> findElements(IOSLocator locator) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.USING.getKey(), locator.getSelector().getKey());
        parameters.put(WDACommand.Parameter.VALUE.getKey(), locator.getValue());
        RemoteResponse response = execute(WDACommand.FIND_CHILD_ELEMENTS, new EnumMap<>(WDACommand.Wildcard.class), parameters);
        return new ResponseValueConverter(response).toElementsList(commandExecutor);
    }

    @Override
    public Element findElement(IOSLocator locator) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.USING.getKey(), locator.getSelector().getKey());
        parameters.put(WDACommand.Parameter.VALUE.getKey(), locator.getValue());
        RemoteResponse response = execute(WDACommand.FIND_CHILD_ELEMENT, new EnumMap<>(WDACommand.Wildcard.class), parameters);
        return new ResponseValueConverter(response).toElement(commandExecutor);
    }

    @Override
    public boolean isDisplayed() {
        return (Boolean) execute(WDACommand.IS_ELEMENT_DISPLAYED).getValue();
    }

    @Override
    public Point getLocation() {
        return new ResponseValueConverter(execute(WDACommand.GET_ELEMENT_RECT)).toPoint();
    }

    @Override
    public Dimension getSize() {
        return new ResponseValueConverter(execute(WDACommand.GET_ELEMENT_RECT)).toDimension();
    }

    @Override
    public Rectangle getRect() {
        return new ResponseValueConverter(execute(WDACommand.GET_ELEMENT_RECT)).toRect();
    }

    @Override
    public <T> T getScreenshot(OutputImageType<T> outputType) {
        return outputType.convertFromBase64Png((String) execute(WDACommand.GET_ELEMENT_SCREENSHOT).getValue());
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards, Map<String, ?> parameters) {
        wildcards.put(WDACommand.Wildcard.SESSION_ID, sessionId);
        wildcards.put(WDACommand.Wildcard.ELEMENT_UI_ID, uid);
        return commandExecutor.execute(command, wildcards, parameters);
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards) {
        return this.execute(command, wildcards, new HashMap<>());
    }

    @Override
    public RemoteResponse execute(String command) {
        return this.execute(command, new EnumMap<>(WDACommand.Wildcard.class), new HashMap<>());
    }
}
