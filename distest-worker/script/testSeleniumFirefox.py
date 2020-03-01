from selenium import webdriver
import pytest
import time
import allure
import datetime
from time import sleep
from utils.screenshot import TestScreenShot
from selenium.webdriver.support.ui import WebDriverWait

# 模块级（setup_module/teardown_module）开始于模块始末，全局的
# 函数级（setup_function/teardown_function）只对函数用例生效（不在类中）
# 类级（setup_class/teardown_class）只在类中前后运行一次(在类中)
# 方法级（setup_method/teardown_method）开始于方法始末（在类中）
# 类里面的（setup/teardown）运行在调用方法的前后

class TestSelenium:

    # options = webdriver.FirefoxOptions()
    # options.add_argument('lang=zh_CN.UTF-8')
    # browser = webdriver.firefox(firefox_options=options)
    browser = webdriver.Firefox()

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def setup_class(self):
        self.browser.get('http://www.baidu.com/')
        # self.browser.maximize_window()
        # wait = WebDriverWait(self.browser, 25)
        # waitPopWindow = WebDriverWait(self.browser, 25)
        TestScreenShot(self.browser).screenshot("操作后")

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def teardown_class(self):
        print(self.browser.page_source)

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def test01(self):
        t = time.time()
        self.browser.find_element_by_xpath("//input[@id='kw']").send_keys("北京肺炎疫情")
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")

        print("this is first testcase")
        pass

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def test02(self):
        self.browser.find_element_by_xpath("//input[@value='百度一下']").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        pass

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def test03(self):
        self.browser.find_element_by_xpath("/html/body/div/div[2]/div/a[1]").click()
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")
        pass

    @allure.feature("百度搜索-firefox")
    @allure.severity('blocker')
    def test04(self):
        self.browser.find_element_by_xpath("//*[@id=\"s_tab\"]/div/a[2]").click()
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")
        # print(self.browser.current_url)
        pass

if __name__ == "__main__":
    pytest.main(["-s", "-q", "--alluredir", "./report/raw"])


