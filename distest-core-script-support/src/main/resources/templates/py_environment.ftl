<environment>
    <parameter>
        <key>应用名称</key>
        <value>${app.displayName}</value>
    </parameter>
    <parameter>
        <key>应用平台</key>
        <value>${app.platform}</value>
    </parameter>
<#list devices as device>
    <parameter>
        <key>${device.brand} ${device.model} 品牌 </key>
        <value>${device.deviceId}</value>
    </parameter>
    <parameter>
        <key>${device.brand} ${device.model} 版本 </key>
        <value>${device.osVersion}</value>
    </parameter>
    <parameter>
        <key>${device.brand} ${device.model} SDK </key>
        <value>${device.sdk}</value>
    </parameter>
    <parameter>
        <key>${device.brand} ${device.model} cpu </key>
        <value>${device.cpuabi}</value>
    </parameter>
    <parameter>
        <key>${device.brand} ${device.model} 尺寸 </key>
        <value>${device.width} x ${device.height}</value>
    </parameter>
    <parameter>
        <key>${device.brand} ${device.model} 客户端环境 </key>
        <value>
        <#if taskEnvMap??>
            <#if taskEnvMap[device.deviceId]??>
                ${taskEnvMap[device.deviceId]}
            </#if>
        </#if>
        </value>
    </parameter>
</#list>
</environment>