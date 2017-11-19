# 处理进程异步执行

目前实现的主要功能包括
* python 异步执行
* ADB shell 异步执行 (包括 logcat 、minicap，如有其他进程，再自行实现)

如果需要实现其他进行处理，请继承 CommonRunnerDriverService，实现对应的 Builder 即可
