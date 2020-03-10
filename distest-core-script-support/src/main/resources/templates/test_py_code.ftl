<#if isArray??>
    <#if isArray>
el = driver.find_elements_by_${strategy}(${locator})
    <#else >
el = driver.find_element_by_${strategy}(${locator})
    </#if>
</#if>
el.click()
el.clear()
el.send_keys(${text})

