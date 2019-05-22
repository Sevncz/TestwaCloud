package com.testwa.distest.client.component.wda.support;

import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.stream.LogOutputStream;

/**
 * @author wen
 * @create 2019-05-21 18:38
 */
@Slf4j
public class XCodeLogOutputStream extends LogOutputStream {

    @Override
    protected void processLine(String s) {
        log.info(s);
    }
}
