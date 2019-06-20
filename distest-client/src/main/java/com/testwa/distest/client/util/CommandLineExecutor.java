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

package com.testwa.distest.client.util;


import com.testwa.distest.client.exception.CommandFailureException;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class CommandLineExecutor {

    private static final int DEFAULT_TIMEOUT = 30;

    public static String execute(String[] command) {
        return execute(command, DEFAULT_TIMEOUT);
    }

    public static String execute(String[] command, int timeoutSeconds) {
        try {
            return new ProcessExecutor()
                        .command(command)
                        .readOutput(true)
                        .timeout(timeoutSeconds, TimeUnit.SECONDS)
                        .execute().outputUTF8();
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new CommandFailureException(e);
        }
    }

    public static StartedProcess asyncExecute(String[] command) {
        try {
            return new ProcessExecutor()
                        .command(command)
                        .readOutput(true)
                        .addListener(new ProcessListener() {
                            @Override
                            public void beforeStart(ProcessExecutor executor) {
                                super.beforeStart(executor);
                                log.info("[{}] ready start", commandArraysToString(command));
                            }

                            @Override
                            public void afterStart(Process process, ProcessExecutor executor) {
                                super.afterStart(process, executor);
                                log.info("[{}] started", commandArraysToString(command));
                            }

                            @Override
                            public void afterFinish(Process process, ProcessResult result) {
                                super.afterFinish(process, result);
                                log.info("[{}] finish, {}", commandArraysToString(command), result.getOutput().getUTF8());
                            }

                            @Override
                            public void afterStop(Process process) {
                                super.afterStop(process);
                                log.info("[{}] stop", commandArraysToString(command));
                            }
                        })
                        .start();
        } catch (IOException e) {
            throw new CommandFailureException(e);
        }
    }

    private static String commandArraysToString(String[] command) {
        return String.join(" ", command);
    }


    public static void processQuit(StartedProcess process) {
        if (process != null) {
            process.getProcess().destroy();
        }
    }
}
