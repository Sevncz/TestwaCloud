import sys
from selenium.common.exceptions import InvalidSessionIdException
from datetime import datetime
import pytest
import os
import copy
import allure

from appium import webdriver


ANDROID_BASE_CAPS = {
    'app': os.path.abspath('../apps/ApiDemos-debug.apk'),
    'automationName': 'UIAutomator2',
    'platformName': 'Android',
    'platformVersion': os.getenv('ANDROID_PLATFORM_VERSION') or '8.0',
    'deviceName': os.getenv('ANDROID_DEVICE_VERSION') or 'Android Emulator',
}

IOS_BASE_CAPS = {
    'app': os.path.abspath('/Users/wen/dev/TestApp.zip'),
    'automationName': 'xcuitest',
    'platformName': 'iOS',
    'platformVersion': '13.3',
    'deviceName': 'iPhone',
    'udid': '5a94a4eefd68f77083a73e4ca73079ce0eebdcf7',
    "xcodeOrgId": "UNW569G4GD",
    "xcodeSigningId": "iPhone Developer"
    # 'showIOSLog': False,
}

EXECUTOR = 'http://127.0.0.1:4723/wd/hub'


def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


def take_screenshot_and_logcat(driver, device_logger, calling_request):
    __save_log_type(driver, device_logger, calling_request, 'logcat')


def take_screenshot_and_syslog(driver, device_logger, calling_request):
    __save_log_type(driver, device_logger, calling_request, 'syslog')


def __save_log_type(driver, device_logger, calling_request, type):
    logcat_dir = device_logger.logcat_dir
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
            caps = copy.copy(IOS_BASE_CAPS)
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


class TestIOSBasicInteractions():
    
    def setup_class(cls):
        cls.client = DriverClient().driver
        
    def teardown_class(cls):
        cls.client.quit()

    @pytest.fixture(scope='function')
    def driver(self, request, device_logger):
        calling_request = request._pyfuncitem.name
        driver = DriverClient().driver
        def fin():
            take_screenshot_and_syslog(driver, device_logger, calling_request)
        request.addfinalizer(fin)
        return driver

    <#list actions as action>
    @allure.feature(${action.feature}) # 模块名称
    @allure.title(${action.title})
    @allure.severity(${action.severity})# 用例等级
    def test_should_send_keys_to_inputs(self, driver):
        text_field_el = driver.find_element_by_id('TextField1')
        text_field_el.send_keys('Hello World!')
        assert 'Hello World!' == text_field_el.get_attribute('value')
    </#list>