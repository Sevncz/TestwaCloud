package com.testwa.distest.server.rpc;

import io.rpc.testwa.agent.BrowserAppEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Data
@NoArgsConstructor
public class DevBrowserAppPackage {

    private Boolean success;
    private Boolean selected;
    private List<DevBrowserApp> browsers;


    public DevBrowserAppPackage(BrowserAppEvent request) {
        this.selected = request.getSelected();
        this.success = request.getSuccess();
        List<DevBrowserApp> browsers = new ArrayList<>();
        request.getAppsList().forEach( t -> {
            DevBrowserApp browser = new DevBrowserApp();
            browser.setName(t.getName());
            StringTokenizer st = new StringTokenizer(t.getComponent(), "/");
            if(st.hasMoreTokens()) {
                browser.setPackageName(st.nextToken());
                browser.setActive(st.nextToken());
            }
            browser.setSystem(t.getSystem());
            browser.setSelected(t.getSelected());
            browsers.add(browser);
        });
        this.browsers = browsers;
    }

    @Data
    private class DevBrowserApp {
        private String name;
        private String packageName;
        private String active;
        private Boolean system;
        private Boolean selected;
    }
}
