package com.testwa.distest.client.device.driver;

import com.testwa.distest.client.component.wda.exception.CapabilityIsNotFoundException;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wen
 * @create 2019-05-23 20:45
 */
@Data
public class IDeviceRemoteControlDriverCapabilities {

    public enum IDeviceKey {
        HOST("host"),
        PORT("port"),
        DEVICE_ID("deviceId"),
        WDA_PATH("wdaPath"),
        RESOURCE_PATH("resourcePath"),
        ;

        private String key;

        IDeviceKey(String key) {
            this.key = key;
        }

        String getKey() {
            return key;
        }
    }


    private Map<String, String> capabilities = new HashMap<>();

    public void setCapability(IDeviceKey capabilityKey, String value) {
        this.addToCapabilities(capabilityKey.getKey(), value);
    }

    public void setCapability(String name, String value) {
        this.addToCapabilities(name, value);
    }

    public void setCapabilities(String name, Boolean value) {
        this.addToCapabilities(name, value);
    }

    public void setDeviceId(String value) {
        this.setCapability(IDeviceKey.DEVICE_ID, value);
    }

    public void setResourcePath(String value) {
        this.setCapability(IDeviceKey.RESOURCE_PATH, value);
    }

    public void setHost(String value) {
        this.setCapability(IDeviceKey.HOST, value);
    }

    public void setPort(String value) {
        this.setCapability(IDeviceKey.PORT, value);
    }

    public String getCapability(String name) {
        return capabilities.get(name);
    }

    public String getCapability(IDeviceKey capabilityKey) {
        return capabilities.get(capabilityKey.getKey());
    }

    private void addToCapabilities(String name, Object value) {
        if (Arrays.stream(IDeviceKey.values()).map(IDeviceKey::getKey).anyMatch(k -> k.equals(name))) {
            capabilities.put(name, (String) value);
        } else {
            throw new CapabilityIsNotFoundException(name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IDeviceRemoteControlDriverCapabilities that = (IDeviceRemoteControlDriverCapabilities) o;

        return new EqualsBuilder()
                .append(capabilities, that.capabilities)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(capabilities)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("capabilities", capabilities)
                .toString();
    }
}
