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

package com.testwa.distest.client.component.wda.support;


import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.SystemProcess;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandLineExecutor {

    private static final int DEFAULT_TIMEOUT = 30;

    public static String execute(String[] command) {
        return execute(command, DEFAULT_TIMEOUT);
    }

    public static String execute(String[] command, int timeout) {
        try {
            return new ProcessExecutor()
                        .command(command)
                        .readOutput(true)
                        .timeout(timeout, TimeUnit.SECONDS)
                        .execute().outputUTF8();
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new WebDriverAgentException(e);
        }
    }

    public static StartedProcess asyncExecute(String[] command) {
        try {
            return new ProcessExecutor()
                        .command(command)
                        .readOutput(true)
                        .start();
        } catch (IOException e) {
            throw new WebDriverAgentException(e);
        }
    }


    public static void processQuit(StartedProcess process) {
        if (process != null) {
            process.getProcess().destroy();
        }
    }
}
