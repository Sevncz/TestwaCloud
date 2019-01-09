package com.testwa.distest.server.web.app.vo;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * @author wen
 * @create 2019-01-09 15:23
 */
@Data
public class AppInfoVO {
    private Long id;
    private String packageName;
    private String name;
    private Date latestUploadTime;
    private DB.PhoneOS platform;

    private AppVO latestApp;
}
