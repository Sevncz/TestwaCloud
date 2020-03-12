import sys
from selenium.common.exceptions import InvalidSessionIdException
from datetime import datetime
import pytest
import os
import copy
import allure

from appium import webdriver

<#if type??>
    <#if type = 'iOS'>
IOS_BASE_CAPS = {
    'app': os.path.abspath('${appPath}'),
    'automationName': 'xcuitest',
    'platformName': 'iOS',
    'platformVersion': '${platformVersion}',
    'deviceName': 'iPhone',
    'udid': '${udid}',
    "xcodeOrgId": '${xcodeOrgId}',
    "xcodeSigningId": "iPhone Developer",
    "wdaEventloopIdleDelay": "1",
    "wdaLocalPort": "${wdaLocalPort}",
    "mjpegServerPort": "${mjpegServerPort}"
    # 'showIOSLog': False,
}
    </#if>
    <#if type = 'Android'>
ANDROID_BASE_CAPS = {
    'app': os.path.abspath('${appPath}'),
    'automationName': 'UIAutomator2',
    'platformName': 'Android',
    'platformVersion': '${platformVersion}',
    'deviceName': '${deviceName}',
}
    </#if>
<#else>
ANDROID_BASE_CAPS = {
    'app': os.path.abspath('${appPath}'),
    'automationName': 'UIAutomator2',
    'platformName': 'Android',
    'platformVersion': '${platformVersion}',
    'deviceName': '${deviceName}',
}
</#if>

EXECUTOR = 'http://127.0.0.1:${port}/wd/hub'

def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


def take_screenshot_and_logcat(driver, calling_request):
    __save_log_type(driver, calling_request, 'logcat')


def take_screenshot_and_syslog(driver, calling_request):
    __save_log_type(driver, calling_request, 'syslog')


def __save_log_type(driver, calling_request, type):
    try:
        logcat_data = driver.get_log(type)
    except InvalidSessionIdException:
        logcat_data = ''

    data_string = ''
    for data in logcat_data:
        data_string = data_string + '%s:  %s\n' % (data['timestamp'], data['message'].encode('utf-8'))
    allure.attach(data_string, '日志', allure.attachment_type.TEXT)
    allure.attach(driver.get_screenshot_as_png(), '操作截图', allure.attachment_type.PNG)


class Singleton(object):
    driver = None

    def __new__(cls, *args, **kw):
        if not hasattr(cls, '_instance'):
        <#if type??>
            <#if type = 'iOS'>
            caps = copy.copy(IOS_BASE_CAPS)
            </#if>
            <#if type = 'Android'>
            caps = copy.copy(ANDROID_BASE_CAPS)
            </#if>
        <#else>
            caps = copy.copy(ANDROID_BASE_CAPS)
        </#if>
            driver = webdriver.Remote(
                command_executor=EXECUTOR,
                desired_capabilities=caps
            )
            orig = super(Singleton, cls)
            cls._instance = orig.__new__(cls, *args, **kw)
            cls._instance.driver = driver
        return cls._instance

class DriverClient(Singleton):
    pass


class TestWaBasic():

    def setup_class(cls):
        cls.client = DriverClient().driver

    def teardown_class(cls):
        cls.client.quit()

    @pytest.fixture(scope='function')
    def driver(self, request):
        calling_request = request._pyfuncitem.name
        driver = DriverClient().driver
        def fin():
    <#if type??>
        <#if type = 'iOS'>
            take_screenshot_and_syslog(driver, calling_request)
        </#if>
        <#if type = 'Android'>
            take_screenshot_and_logcat(driver, calling_request)
        </#if>
    <#else>
            take_screenshot_and_logcat(driver, calling_request)
    </#if>
        request.addfinalizer(fin)
        return driver

<#list functions as function>
    <#if function.feature??>
    @allure.feature("${function.feature}") # 模块名称
    </#if>
    <#if function.title??>
    @allure.title("${function.title}") # 用例标题
    </#if>
    <#if function.severity??>
    @allure.severity("${function.severity}")# 用例等级
    </#if>
    def test_action_${function_index}(self, driver):
    <#list function.actions as action>
        ${action}
    </#list>

</#list>