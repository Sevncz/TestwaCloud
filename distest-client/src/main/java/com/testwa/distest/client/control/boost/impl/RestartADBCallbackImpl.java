package com.testwa.distest.client.control.boost.impl;

import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.control.boost.MessageCallback;
import com.testwa.distest.client.control.boost.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class RestartADBCallbackImpl implements MessageCallback {

    @Override
    public void done(Object o, MessageException e) throws MessageException {
        List<String> adbPids = getPID();
        adbPids.forEach(pid -> {
            try {
                Runtime.getRuntime().exec("taskkill /F /PID " + pid);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private List<String> getPID() {
        List<String> pids = new ArrayList<>();
//        "tasklist /nh /FI \"IMAGENAME eq adb.exe\""
        CommandLine commandLine = new CommandLine("tasklist");
        commandLine.addArgument("/nh");
        commandLine.addArgument("/FI");
        commandLine.addArgument("\"IMAGENAME eq adb.exe\"");
        UTF8CommonExecs execs = new UTF8CommonExecs(commandLine);
        try {
            execs.exec();
            String output = execs.getOutput();
            String[] lines = output.split("\n");
            for(String l : lines){
                if(l.contains("adb.exe")){
                    String[] lineArray = l.split(" ");
                    String pid = lineArray[17].trim();
                    pids.add(pid);
                }
            }
        } catch (IOException e) {
            log.error("Kill adb process error!", e);
        }

        return pids;
    }
}
