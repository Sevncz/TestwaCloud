import time
import allure

class TestScreenShot:
    def __init__(self,wd):
        self.wd = wd

    def screenshot(self,step_name):
        t = time.time()
        filepath = "E:\\ProjectPy\\test\\report\\img\\" + str(int(round(t * 1000))) + ".png"
        self.wd.save_screenshot(filepath)
        with open(filepath, "rb") as file:
            file = file.read()
            allure.attach(file, step_name, attachment_type=allure.attachment_type.JPG)

    def get_sceenshot_file(self, step_name):
        t = time.time()
        filepath = "E:\\ProjectPy\\test\\report\\img\\" + str(int(round(t * 1000))) + ".png"
        self.wd.get_screenshot_as_file(filepath)
        with open(filepath, "rb") as file:
            file = file.read()
            allure.attach(file, step_name, attachment_type=allure.attachment_type.JPG)
