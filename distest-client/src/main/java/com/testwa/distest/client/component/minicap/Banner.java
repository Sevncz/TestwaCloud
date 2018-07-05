package com.testwa.distest.client.component.minicap;

import lombok.Data;

/**
 * Banner
 *
 * @author cuiyang
 */
@Data
public class Banner {
    /** 版本 */
    private int version;
    /** 该Banner信息的长度 */
    private int length;
    /** 进程ID */
    private int pid;
    /** 设备真实宽度 */
    private int realWidth;
    /** 设备真实高度 */
    private int realHeight;
    /** 设备的虚拟宽度 */
    private int virtualWidth;
    /** 设备的虚拟高度 */
    private int virtualHeight;
    /** 设备的方向 */
    private int orientation;
    /** 设备信息获取策略 */
    private int quirks;
}
