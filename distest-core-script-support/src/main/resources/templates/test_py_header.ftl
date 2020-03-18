
<#if type??>
    <#if type = 'iOS'>
IOS_BASE_CAPS = {
    'app': os.path.abspath(r'${appPath}'),
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
    'app': os.path.abspath(r'${appPath}'),
    'automationName': 'UIAutomator2',
    'platformName': 'Android',
    'platformVersion': '${platformVersion}',
    'deviceName': '${deviceName}',
    'autoGrantPermissions': 'true',
    'systemPort': '${systemPort}',
    'autoLaunch': 'false',
}
    </#if>
<#else>
ANDROID_BASE_CAPS = {
    'app': os.path.abspath(r'${appPath}'),
    'automationName': 'UIAutomator2',
    'platformName': 'Android',
    'platformVersion': '${platformVersion}',
    'deviceName': '${deviceName}',
    'autoGrantPermissions': 'true',
    'systemPort': '${systemPort}',
    'autoLaunch': 'false',
}
</#if>

EXECUTOR = 'http://127.0.0.1:${port}/wd/hub'
