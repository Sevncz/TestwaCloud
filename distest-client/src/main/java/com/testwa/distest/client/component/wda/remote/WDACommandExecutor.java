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

package com.testwa.distest.client.component.wda.remote;


import com.testwa.distest.client.component.wda.driver.CommandExecutor;
import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;
import com.testwa.distest.client.component.wda.support.JsonConverter;
import com.testwa.distest.client.support.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class WDACommandExecutor implements CommandExecutor {

    private URL wdaUrl;
    private ReentrantLock lock = new ReentrantLock();

    public WDACommandExecutor(URL wdaUrl) {
        this.wdaUrl = wdaUrl;
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards, Map<String, ?> parameters) {
        log.debug(String.format("Execute command: %s with wildcards: %s and parameters: %s",
                command, wildcards, parameters));
        RemoteCommandInfo commandInfo = getCommandInfo(command);
        String url = wdaUrl.toString() + commandInfo.getUrl();

        for (WDACommand.Wildcard wildcard : WDACommand.Wildcard.values()) {
            String value = wildcards.get(wildcard);
            if (value != null) {
                url = url.replace(wildcard.getKey(), value);
            }
        }

        String responseContent = null;
        try {
            lock.lock();

            switch (commandInfo.getMethod()) {
                case GET:
                    responseContent = OkHttpUtil.get(url, null);
                    break;
                case POST:
                    String jsonParameters = "";
                    if(parameters != null) {
                        jsonParameters = JsonConverter.toJson(parameters);
                    }
                    responseContent = OkHttpUtil.postJsonParams(url, jsonParameters);
                    break;
                case DELETE:
                    responseContent = OkHttpUtil.delete(url);
                    break;
                default:
                    log.error("不属于任何方法");
                    break;
            }

        }catch (Exception e) {
            log.error("request wda exception {}", e.getMessage());
        }finally {
            lock.unlock();
        }

        RemoteResponse response = JsonConverter.fromJson(responseContent, RemoteResponse.class);

        if (response != null && response.getStatus() != null && response.getStatus() != RemoteDriverStatus.NO_ERROR.getStatus()) {
            RemoteDriverStatus remoteStatus = RemoteDriverStatus.getByStatusCode(response.getStatus());
            String message = RemoteDriverStatus.getMessage(remoteStatus);
            throw new WebDriverAgentException(String.format("%s. Description: %s", message, response.getValue()));
        }

        return response;
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards) {
        return execute(command, wildcards, new HashMap<>());
    }

    @Override
    public RemoteResponse execute(String command) {
        return execute(command, new EnumMap<>(WDACommand.Wildcard.class), new HashMap<>());
    }

    private RemoteCommandInfo getCommandInfo(String command) {
        return Optional.ofNullable(WDACommand.getCommand(command))
                .orElseThrow(() -> new WebDriverAgentException("Unable to find command: " + command));
    }
}
