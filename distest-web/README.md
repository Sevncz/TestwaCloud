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
        "data": {
            "totalRecords": 1,
            "records": [
                {
                    "id": "583056ef142e9f19ba87c696",
                    "name": "test00001",
                    "createDate": "2016-11-19 21:43:11",
                    "userId": "5826b50d142e9f79a3e8a481",
                    "userName": "wen01",
                    "members": null,
                    "description": null
                }
            ]
        },
        "code": 0,
        "url": null
    }

### message说明
    出现错误或者异常的时候，message会有错误信息，如果是未被逻辑捕获处理的系统异常，message则会放异常信息

### code说明
    SUCCESS(0),
    PARAM_ERROR(1),
    ILLEGAL_OP(1000), // 非法操作，通常是进行了不被授权的操作。
    SERVER_ERROR(1001), // 服务器内部错误。
    NO_LOGIN(1002), // 没有登录。
    ACCOUNT_FREEZE(1001), //账户冻结。
    INVALID_PARAM(-1), //无效参数。
    NO_AUTH(-2), //无API访问权限。
    INVALID_IP(-3), //IP没有权限。
    OVERLOCK(-4), //超过访问频率。
    ILLEGAL_TOKEN(-5), //非法token。
    NO_API(-5), //API不存在。

