package com.testwa.distest.server.web.app.vo;

import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import lombok.Data;

import java.util.List;

@Data
public class AppInfoVersionsDetailVO {
    private AppInfo appInfo;
    private List<App> versions;
}
