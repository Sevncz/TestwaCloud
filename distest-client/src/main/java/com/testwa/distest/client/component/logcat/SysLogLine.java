package com.testwa.distest.client.component.logcat;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wen
 * @create 2019-06-19 13:33
 */
@Data
public class SysLogLine {

    private Date date;
    private String process;
    private int pid;
    private String level;
    private String message;
    private String original;

    SysLogLine(String line) throws LogParsingException {
        original = line;
        // Date
        try {
            String d = Calendar.getInstance().get(Calendar.YEAR) + " " + line.substring(0, 15);
            SimpleDateFormat parser = new SimpleDateFormat("yyyy MMM d HH:mm:ss");
            date = parser.parse(d);

            // process and its pid
            String rest = line.substring(15);
            int openingBracketPid = rest.indexOf("[");
            int closingBracketPid = rest.indexOf("]");
            // removing the 2 spaces
            process = rest.substring(2, openingBracketPid);
            pid = Integer.parseInt(rest.substring(openingBracketPid + 1, closingBracketPid));

            rest = rest.substring(closingBracketPid + 2);

            // level
            int index = rest.indexOf('>');
            level = rest.substring(1, index);

            // removing the leading ':'
            rest = rest.substring(index + 2);

            // and trim leading space
            message = rest.trim();
        } catch (Exception e) {
            throw new LogParsingException("Cannot parse line (is it a full line ?)" + original);
        }
    }


}
