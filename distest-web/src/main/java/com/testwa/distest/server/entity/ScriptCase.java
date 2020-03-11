package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.distest.common.enums.DB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="dis_script_case")
public class ScriptCase extends ProjectBase {

    public static final String PLATFORM_IOS = "iOS";
    public static final String PLATFORM_ANDROID = "Android";

    /**
     * 脚本案例ID
     */
    @Column(name = "script_case_id")
    private String scriptCaseId;
    /**
     * 脚本名称
     */
    @Column(name = "script_case_name")
    private String scriptCaseName;
    /**
     * 脚本描述
     */
    @Column(name = "script_case_desc")
    private String scriptCaseDesc;
    /**
     * android app package name
     */
    @Column(name = "app_base_package")
    private String appBasePackage;
    /**
     * 脚本平台
     */
    @Column(name = "platform")
    private String platform = PLATFORM_ANDROID;

}
