# distest-web
分布式测试管理web服务模块

## 依赖工具
1. mongodb
2. redis
3. maven
4. proto

## 开发环境搭建

- 启动时增加参数:
    --spring.config.location=/Users/wen/IdeaProjects/distest/src/main/config/application.properties
- 修改application.properties
    logging.config = /Users/wen/IdeaProjects/webcenter/src/main/config/log4j2.xml

- 如果系统找不到adb，请同时增加环境变量的配置:
    ANDROID_HOME=/your/android/sdk/path/

## 编译打包部署
    sh deploy.sh

## api返回结果
    {
        "message": null,
        "data": object or [object],
        "code": 0,
        "url": null
    }

### message说明
    出现错误或者异常的时候，message会有错误信息，如果是未被逻辑捕获处理的系统异常，message则会放异常信息


