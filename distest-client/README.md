# distest-client
分布式测试管理客户端模块

## adb通过无线局域网连接
- 使用USB数据线连接设备
- adb tcpip 5555
- 断开 USB数据线
- adb connect <设备的IP地址>:5555
- adb devices
- 如果需要恢复到USB数据线，可以在命令行输入adb usb
