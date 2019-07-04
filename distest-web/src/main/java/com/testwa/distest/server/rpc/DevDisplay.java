package com.testwa.distest.server.rpc;

import io.rpc.testwa.agent.DisplayEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DevDisplay {
    private int width;
    private int height;
    private float xdpi;
    private float ydpi ;
    private float fps ;
    private float density ;
    private int rotation ;

    public DevDisplay(DisplayEvent displayEvent) {
        this.width = displayEvent.getWidth();
        this.height = displayEvent.getHeight();
        this.xdpi = displayEvent.getXdpi();
        this.ydpi = displayEvent.getYdpi();
        this.fps = displayEvent.getFps();
        this.density = displayEvent.getDensity();
        this.rotation = displayEvent.getRotation();
    }

}
