package com.testwa.distest.server.web.app.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppInfoVersionsDetailVO {
    private AppInfoVO appInfo;
    private List<AppVO> versions;
}
