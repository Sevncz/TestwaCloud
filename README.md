# distest
Distributed App Test Management Platform(分布式app测试管理平台)

## 打包
会自动编译proto文件
mac:
mvn package -Dmaven.test.skip=true -Djavacpp.platform=${platform}
windows：
mvn package '-Dmaven.test.skip=true' '-Djavacpp.platform=windows-x86_64'
platform: android-arm, linux-x86_64, macosx-x86_64, windows-x86_64, etc