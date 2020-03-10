#!/usr/bin/env python# -*- coding: utf-8 -*-# @Author: sevncz# @Date:   2016-01-08T12:37:41+08:00# @Last modified by:   sevncz# @Last modified time: 2016-04-12T15:34:27+08:00"""/Users/wen/.pyenv/versions/2.7.17/bin/fab -f deploy go进入文件所在目录，执行 fab -f deploy.py go"""from fabric.api import *import datetime# 任务并行# env.parallel = True# 目标服务器env.hosts = ['cloud.testwa.com']env.user = "TestwaUser"env.key_filename = "/Users/wen/Dropbox/aliyunpem/user/id_rsa"remote_dir = '/data/www/server/web'backup_dir = '/data/www/backup'jar = 'distest-web-1.0-SNAPSHOT.jar'""" 打包 """@task@runs_oncedef package():    local('mvn package -Dmaven.test.skip=true -Djavacpp.platform=windows-x86_64 ')@taskdef stop_web():    # 停止远程服务    with cd(remote_dir):        run('sh stop.sh')""" 部署web-server """@taskdef deploy_web():    with cd(remote_dir):        # 移动远程服务器的文件至 backup 目录:        run('mv %s /data/www/backup/%s' % (jar, jar))    # 上传jar文件至远程服务器    with lcd('./distest-web/target'):        put('distest-web-1.0-SNAPSHOT.jar', remote_dir)@taskdef start_web():    # 启动远程服务    with cd(remote_dir):        run('sh -x start.sh && sleep 2')        run('ps -ef | grep distest-web')@taskdef go():    package()    stop_web()    deploy_web()    start_web()