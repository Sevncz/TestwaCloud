package com.testwa.distest.server.rpc;

import io.rpc.testwa.agent.BatteryEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DevBattery {
    private String status;
    private String health;
    private String source ;
    private Integer level;
    private Integer scale;
    private Double temp ;
    private Double voltage;

    public DevBattery(BatteryEvent event) {
        this.status = event.getStatus();
        this.health = event.getHealth();
        this.source = event.getSource();
        this.level = event.getLevel();
        this.scale = event.getScale();
        this.temp = event.getTemp();
        this.voltage = event.getVoltage();
    }

}
