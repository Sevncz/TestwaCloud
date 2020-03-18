<#list cases as functions>
class TestWaBasic_${functions_index}():

    def setup_class(cls):
        cls.client = DriverClient().driver
        start_case()

    def teardown_class(cls):
        cls.client.close_app()

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
    @allure.feature("${function.feature}")
    <#else >
    @allure.feature("${function.scriptCaseName}")
    </#if>
    <#if function.title??>
    @allure.story("${function.title}")
    </#if>
    <#if function.severity??>
    @allure.severity("${function.severity}")
    <#else >
    @allure.severity("critical")
    </#if>
    def test_action_${function_index}(self, driver):
    <#list function.actions as action>
        ${action}
    </#list>

</#list>
</#list>