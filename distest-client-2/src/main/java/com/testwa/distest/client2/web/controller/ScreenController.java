package com.testwa.distest.client2.web.controller;

import com.testwa.distest.client2.web.service.ScreenService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 投屏接口
 * @author wen
 * @create 2019-05-08 14:12
 */
@Data
@Slf4j
@Controller
public class ScreenController {
    @Autowired
    private ScreenService screenService;

    @GetMapping("/scrcpy/{deviceId}")
    public void scrcpy(@PathVariable(value = "deviceId")  String deviceId) {
        screenService.scrcpyStart(deviceId);
    }

}
