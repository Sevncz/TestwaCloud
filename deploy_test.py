#!/usr/bin/env python# -*- coding: utf-8 -*-# @Author: sevncz# @Date:   2016-01-08T12:37:41+08:00# @Last modified by:   sevncz# @Last modified time: 2016-04-12T15:34:27+08:00"""进入文件所在目录，执行 fab -f deploy_test.py go"""from fabric.api import *# 任务并行# env.parallel = True# 目标服务器env.hosts = ['cloud.test.testwa.com']env.user = "wadmin"env.key_filename = "/Users/wen/Documents/Testwa/aliyunpem/user/test_evn.pem"remote_dir = '/data/www/wacenter'jar = 'distest-web-1.0-SNAPSHOT.jar'""" 打包 """@task@runs_oncedef package():    local('mvn package -Dmaven.test.skip=true -Djavacpp.platform=windows-x86_64 ')@taskdef stop():    # 停止远程服务    with cd(remote_dir):        run('sh stop.sh')""" 部署web-server """@taskdef deploy_web():    with cd(remote_dir):        # 删除远程服务器的文件:        run('rm -f %s' % jar)    # 上传jar文件至远程服务器    with lcd('./distest-web/target'):        put('distest-web-1.0-SNAPSHOT.jar', remote_dir)@taskdef start():    # 启动远程服务    with cd(remote_dir):        run('sh start.sh')@taskdef go():    package()    stop()    deploy_web()    start()