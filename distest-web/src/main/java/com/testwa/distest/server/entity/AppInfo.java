package com.testwa.distest.server.entity;import com.testwa.core.base.mybatis.annotation.Column;import com.testwa.core.base.mybatis.annotation.Table;import com.testwa.core.base.mybatis.annotation.Transient;import com.testwa.distest.common.enums.DB;import lombok.Data;import java.util.Date;/** * @Program: distest * @Description: app的基本信息及app最新版 * @Author: wen * @Create: 2018-06-25 19:16 **/@Data@Table(name="app_info")public class AppInfo extends ProjectBaseEntity {    @Column(name = "packageName")    private String packageName;    @Column(name = "name")    private String name;    @Column(name = "latestUploadTime")    private Date latestUploadTime;    @Column(name = "latestAppId")    private Long latestAppId;    @Transient    private App latestApp;    @Column(name = "platform")    private DB.PhoneOS platform;}