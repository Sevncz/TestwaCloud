<#list cases as functions>
class TestWaBasic_${functions_index}():

    def setup_class(cls):
        cls.client = DriverClient().driver
        cls.client.launchApp();

    def teardown_class(cls):
        cls.client.closeApp();

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
        time.sleep(2)
    <#list function.actions as action>
        ${action}
    </#list>

</#list>
</#list>