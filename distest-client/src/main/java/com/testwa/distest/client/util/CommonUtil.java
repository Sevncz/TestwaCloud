package com.testwa.distest.client.util;

import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.StringTokenizer;

@Slf4j
public class CommonUtil {

    public static int resolveProcessID(String content, String processName) {
        if (StringUtils.isEmpty(content)) {
            return -1;
        }
        StringTokenizer st = new StringTokenizer(content);
        int index = 0;
        String processIdString = null;
        while(st.hasMoreTokens()) {
            index++;
            String token = st.nextToken();
            if (index == 2) {
                processIdString = token;
            } else if (index == 9) {
                if (!token.equals(processName)) {
                    return -1;
                } else {
                    break;
                }
            }

        }
        if (index < 9 || processIdString == null) {
            return -1;
        }
        try {
            return Integer.parseInt(processIdString);
        } catch (Exception e) {
            log.error("Returned string is not pid: " + processIdString);
            return -1;
        }
    }

    public static String getOsDir() {
        if (Platform.isWindows()) {
            return "win";
        } else if (Platform.isLinux()) {
            return "linux";
        } else if (Platform.isMac()) {
            return "macos";
        } else {
            throw new RuntimeException("Unknown OS");
        }
    }

}
