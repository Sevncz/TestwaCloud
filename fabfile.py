#!/usr/bin/env python# -*- coding: utf-8 -*-# @Author: sevncz# @Date:   2016-01-08T12:37:41+08:00# @Last modified by:   sevncz# @Last modified time: 2016-04-12T15:34:27+08:00"""进入文件所在目录，执行fab deploy"""from datetime import datetimefrom util import date_now_string_formatfrom fabric.api import *# ssh 尝试连接的次数env.connection_attempts = 3# 任务并行# env.parallel = True# 跳板机env.gateway = 'CKRadmin@ss1.chakonger.net.cn:10022'# 目标服务器env.hosts = ['CKRadmin@10.10.75.47']env.passwords = {    ('CKRadmin@ss1.chakonger.net.cn:10022', ): 'Lvadmin2575959',  # 跳板机密码    ('CKRadmin@10.10.75.47:22', ): 'Lvadmin25729'  # 目标服务器密码}@task# @parallel(pool_size=5)def deploy():    """ 部署到ss4 """    tar_files = ['*', 'controllers/*.py', 'db_transfer/*']    local('rm -f fauth.tar.gz')    local(        'tar -czvf fauth.tar.gz --exclude=\'*.tar.gz\' --exclude=\'logs/*\' '        '--exclude=\'fabfile.py\' --exclude=\'restore\' --exclude=\'fauth.py\''        ' --exclude=\'setting.py\' --exclude=\'*.pyc\' --exclude=\'.idea/*\' --exclude=\'.svn/*\' %s' %        ' '.join(tar_files))    # 远程服务器的文件:    remote_tar = '/data/CKR/src/fauth/fauth.tar.gz'    run('rm -f %s' % remote_tar)    # 备份一下    backup_path = '/data/CKR/src/fauth_backup/%s.tar.gz' % date_now_string_format()    backup_file = '/data/CKR/src/fauth'    run('tar -czvf %s %s' % (backup_path, backup_file))    # 上传tar文件至远程服务器    put('fauth.tar.gz', remote_tar)    # 解压    remote_dist_dir = '/data/CKR/src/fauth'    with cd(remote_dist_dir):        run('tar -xzvf %s' % remote_tar)