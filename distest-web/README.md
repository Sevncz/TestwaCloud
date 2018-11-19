# distest-web 开发文档

## 主要技术及工具
java版本：
 - java 1.8
 
第三方环境：
 - android adb
 - redis
 - rabbitMQ
 - mongodb
 - python 2.7

技术组件：
 - springboot
 - spring security
 - mybatis
 - socketIO
 - gRPC
 - quartz

其他：
 - fabric 

## 配置修改
application-dev.properties

## 启动
IDEA启动配置
 1. 增加一个 springboot 配置
 2. 指定启动类：com.testwa.distest.DistestWebApplication
 3. 配置好启动参数：-Xmx512m -Xms512m --spring.profiles.active=dev

## 打包及部署
使用fabric进行自动化打包、部署：
 1. 进入distest项目根目录
 2. fab -f deploy.py go
