# distest-web
分布式测试管理API模块

## 依赖工具
1. mongodb
2. redis
3. maven
4. proto
5. mysql

## 开发环境搭建

- 如果系统找不到adb，请同时增加环境变量的配置:
    ANDROID_HOME=/your/android/sdk/path/


## api返回结果
    {
        "message": null,
        "data": object or [object],
        "code": 0,
        "url": null
    }

### message说明
    出现错误或者异常的时候，message会有错误信息，如果是未被逻辑捕获处理的系统异常，message则会放异常信息

## 编译打包部署
    sh deploy.sh


