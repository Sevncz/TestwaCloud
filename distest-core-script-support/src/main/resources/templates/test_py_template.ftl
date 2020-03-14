import sys
from selenium.common.exceptions import InvalidSessionIdException
from datetime import datetime
import pytest
import os
import copy
import allure
import time
from appium.webdriver.common.touch_action import TouchAction

from appium import webdriver

<#include "test_py_header_template.ftl">

def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


def take_screenshot_and_logcat(driver, calling_request):
    __save_log_type(driver, calling_request, "logcat")


def take_screenshot_and_syslog(driver, calling_request):
    __save_log_type(driver, calling_request, "syslog")


def __save_log_type(driver, calling_request, type):
    try:
        logcat_data = driver.get_log(type)
    except InvalidSessionIdException:
        logcat_data = ""

    data_string = ""
    for data in logcat_data:
        data_string = data_string + "%s:  %s\n" % (data["timestamp"], data["message"].encode("utf-8"))
    allure.attach(data_string, "日志", allure.attachment_type.TEXT)
    allure.attach(driver.get_screenshot_as_png(), "操作截图", allure.attachment_type.PNG)


class Singleton(object):
    driver = None

    def __new__(cls, *args, **kw):
        if not hasattr(cls, "_instance"):
        <#if type??>
            <#if type = "iOS">
            caps = copy.copy(IOS_BASE_CAPS)
            </#if>
            <#if type = "Android">
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

<#include "test_py_alter_process.ftl">

<#include "test_py_class_template.ftl">