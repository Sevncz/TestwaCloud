<#if isArray??>
    <#if isArray>
${localVar} = driver.find_elements_by_${strategy}(${locator})
    <#else >
${localVar} = driver.find_element_by_${strategy}(${locator})
    </#if>
</#if>