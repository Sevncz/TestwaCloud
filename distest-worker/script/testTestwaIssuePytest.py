from selenium import webdriver
from  selenium import webdriver
import pytest
import time
import allure
import datetime
from time import sleep
from utils.screenshot import TestScreenShot
from selenium.webdriver.support.ui import WebDriverWait
import random

# 模块级（setup_module/teardown_module）开始于模块始末，全局的
# 函数级（setup_function/teardown_function）只对函数用例生效（不在类中）
# 类级（setup_class/teardown_class）只在类中前后运行一次(在类中)
# 方法级（setup_method/teardown_method）开始于方法始末（在类中）
# 类里面的（setup/teardown）运行在调用方法的前后
from utils.shellcmd import TestShellCmd


class Test_CloudTestwa_com:

    options = webdriver.ChromeOptions()
    options.add_argument('lang=zh_CN.UTF-8')
    browser = webdriver.Chrome(chrome_options=options)

    def setup_class(self):
        self.browser.get('http://cloud.testwa.com/')
        self.browser.maximize_window()
        wait = WebDriverWait(self.browser, 25)
        waitPopWindow = WebDriverWait(self.browser, 25)

    def teardown_class(self):
        print("this test is finished!")

    @allure.severity('blocker')
    def test_login_input_name(self):
        self.browser.find_element_by_xpath("//*[@id=\"mat-input-0\"]").send_keys("wen01")
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('blocker')
    def test_login_input_pwd(self):
        self.browser.find_element_by_xpath("//*[@id=\"mat-input-1\"]").send_keys("12345^")
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")

    @allure.severity('blocker')
    def test_login_click_login(self):
        self.browser.find_element_by_xpath("/html/body/app-root/app-login/div/div[2]/div/div[3]/form/button/span").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")

    @allure.severity('trivial')
    def test_project_click(self):
        self.browser.find_element_by_xpath("/html/body/app-root/app-index/div/div[2]/div[2]/div[2]/div/div[2]/div[1]/p").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('trivial')
    def test_issue_click(self):
        self.browser.find_element_by_xpath("/html/body/app-root/app-index/div/div[2]/div[2]/div[2]"
                                           "/div/div[2]/div[2]/div/div[2]/div[4]/mat-nav-list"
                                           "/mat-list-item/div/div[2]/h3").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")
        # print(self.browser.current_url)

    @allure.severity('trivial')
    def test_issue_new_button(self):
        self.browser.find_element_by_xpath("/html/body/app-root/app-project/mat-sidenav-container"
                                           "/mat-sidenav-content/div/div[2]/app-issues/div/div[2]"
                                           "/app-issues-category/div/button/span").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('trivial')
    def test_issue_input_issue_title(self):
        self.browser.find_element_by_xpath("//*[@id=\"mat-input-2\"]").send_keys("this is seleium and pytest demo for add issue!"+"-["+ str(random.randint(1,10)) +"]")

        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('trivial')
    def test_issue_input_issue_comments(self):
        self.browser.find_element_by_xpath("/html/body/app-root/"
                                           "app-project/mat-sidenav-container/mat-sidenav-content/div/div[2]"
                                           "/app-issues/div/div[2]/app-issues-new/form/div[1]/quill-editor"
                                           "/div[2]/div[1]/p").send_keys("this is seleium and pytest demo for add issue!")
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('trivial')
    def test_issue_input_issue_submit(self):
        #指派处理
        self.browser.find_element_by_xpath("/html/body/app-root/app-project/mat-sidenav-container"
                                           "/mat-sidenav-content/div/div[2]/app-issues/div/div[2]"
                                           "/app-issues-new/form/div[2]/div[1]/div/app-search-select"
                                           "/button/span/mat-icon").click()

        self.browser.find_element_by_xpath("//*[@id=\"cdk-overlay-0\"]/div/div/div[2]/button[4]").click()
        sleep(1)
        #标签处理
        self.browser.find_element_by_xpath("/html/body/app-root/app-project/mat-sidenav-container"
                                           "/mat-sidenav-content/div/div[2]/app-issues/div/div[2]"
                                           "/app-issues-new/form/div[2]/div[3]/div/app-tags/button"
                                           "/span/mat-icon").click()
        sleep(1)
        self.browser.find_element_by_xpath("//*[@id=\"cdk-overlay-1\"]/div/div/div/div[2]/div[1]/button").click()
        #优先级处理
        sleep(1)
        self.browser.find_element_by_xpath("/html/body/app-root/app-project/mat-sidenav-container/mat-sidenav-content"
                                           "/div/div[2]/app-issues/div/div[2]/app-issues-new/form/div[2]/div[6]"
                                           "/mat-slider/div/div[3]/div[3]/span").click()
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作前")
        #提交创建
        self.browser.find_element_by_xpath("/html/body/app-root/app-project/mat-sidenav-container"
                                           "/mat-sidenav-content/div/div[2]/app-issues/div/div[2]"
                                           "/app-issues-new/form/div[1]/div/a/div"
                                           "/button").click()
        sleep(2)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    @allure.severity('trivial')
    def test_issue_delete_issue(self):
        self.browser.find_element_by_partial_link_text("this is seleium and pytest demo for add issue")
        sleep(1)
        TestScreenShot(self.browser).screenshot("操作后")
        print("this is first testcase")

    def testCollectionReport(self):
        sleep(5)
        TestShellCmd("E:").executeShell()
        TestShellCmd("cd E:\\ProjectPy\\test").executeShell()
        TestShellCmd("allure generate .\\report\\raw\\ -o .\\report\\html\\ --clean").executeShell()


if __name__ == "__main__":
    pytest.main(["-s", "-q", "--alluredir", "./report/raw"])


