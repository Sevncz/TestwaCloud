package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskNewStartCrawlerForm",
        description = "创建一个遍历测试任务"
)
@Data
public class TaskNewStartCrawlerForm extends RequestFormBase {

    private Long projectId;
    private List<String> deviceIds;
    private Long appId;

//    private List<String> blackList;
//    private List<String> whiteList;
//    private List<String> urlBlackList;
//    private List<String> urlWhiteList;
//    private List<String> firstList;
//    private List<String> lastList;
//    private List<String> backButton;
//    private List<Map<String, Object>> trigger;
    private Integer maxDepth;
    // 运行时间限制
    private Integer runningTimeMinutes;
    // 当遇到下列文字时会触发Back Key
    private List<String> backKeyTriggerList;
    // 待输入文本的控制类型
    private List<String> inputClassList;
    // 待输入的文本（随机选中后选列表中的一个）
    private List<String> inputTextList;
    // 不点击包含以下文本的控件
    private List<String> itemBlackList;
    // 白名单，遇到包含以下文本的控件，会多次点击（默认所有控件只点一次）
    private List<String> itemWhiteList;
    // Android 登录相关元素及操作
    //    - ANDOIRD_LOGIN_CHANGE:
    //        XPATH: '//*[@resource-id="com.xes.jazhanghui.activity:id/login_change"]'
    //        ACTION: click
    //    - ANDROID_USERNAME:
    //        XPATH: '//*[@resource-id="com.xes.jazhanghui.activity:id/xes_login_username"]'
    //        ACTION: input
    //        VALUE: '13691034108'
    //    - ANDROID_PASSWORD:
    //        XPATH: '//*[@resource-id="com.xes.jazhanghui.activity:id/xes_login_password"]'
    //        ACTION: input
    //        VALUE: '123456'
    //    - ANDROID_LOGIN_BUTTON:
    //        XPATH: '//*[@resource-id="com.xes.jazhanghui.activity:id/xes_login_button"]'
    //        ACTION: click
    private List<Map<String, Object>> loginElementAndroid;



}
