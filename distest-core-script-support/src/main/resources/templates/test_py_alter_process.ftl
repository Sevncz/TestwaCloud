import threading
FLAG = True

class usb_install_thread(threading.Thread): # 安装确认
    def init(self):
        threading.Thread.init(self)

    def run(self): # 把要执行的代码写到run函数里面 线程在创建后会直接运行run函数
        cls.driver = DriverClient().driver
        usb_install()

    def usb_install():
        while FLAG:
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"继续安装\")")
                if em:
                    em.click()
            except:
                print("not found 1")

            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"继续安装旧版本\")")
                if em:
                    em.click()
            except:
                print("not found 2")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().textContains(\"电脑端未知来源\")")
                if em:
                    em.click()
            except:
                print("not found 3")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"允许\")")
                if em:
                    em.click()
            except:
                print("not found 4")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"确认\")")
                if em:
                    em.click()
            except:
                print("not found 5")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"确定\")")
                if em:
                    em.click()
            except:
                print("not found 6")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"继续\")")
                if em:
                    em.click()
            except:
                print("not found 7")
            try:
                em = cls.driver.find_element_by_android_uiautomator("new UiSelector().text(\"应用权限\")")
                if em:
                    em1 = cls.driver.find_element_by_id("com.android.packageinstaller:id/bottom_button_layout")
                    em1.click()
            except:
                print("not found 8")

# thread1 = usb_install_thread()
# thread1.start()