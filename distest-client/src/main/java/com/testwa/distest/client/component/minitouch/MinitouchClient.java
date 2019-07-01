package com.testwa.distest.client.component.minitouch;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.android.ADBTools;import com.testwa.distest.client.android.PhysicalSize;import com.testwa.distest.client.exception.CommandFailureException;import com.testwa.distest.client.util.PortUtil;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import org.apache.commons.lang3.StringUtils;import java.io.Closeable;import java.io.IOException;import java.io.InputStream;import java.io.OutputStream;import java.net.InetSocketAddress;import java.net.Socket;import java.net.SocketAddress;import java.nio.charset.StandardCharsets;import java.util.StringTokenizer;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:13 **/@Slf4jpublic class MinitouchClient extends Thread implements Closeable {    private static final String AB_NAME = "minitouch";    private String host = "127.0.0.1";    private Integer port;    private String deviceId;    private Socket socket;    private OutputStream outputStream;    /** 屏幕尺寸 */    private PhysicalSize size;    private double PercentX;    private double PercentY;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    public MinitouchClient(String deviceId) {        super("minitouch-client");        this.deviceId = deviceId;    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    @Override    public void close() throws IOException {        this.isRunning.set(false);        IOUtils.closeQuietly(this.socket);        this.interrupt();    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("Minitouch 客户端已运行");        } else {            this.isRunning.set(true);        }        // 获取设备屏幕的实际尺寸        this.size = getSize();        super.start();    }    @Override    public void run() {        // 连接minicap服务        log.info("[{}] Minitouch client starting port: {}", deviceId, port);        InputStream inputStream = null;        try {            // forward port            this.port = PortUtil.getAvailablePort();            boolean success = ADBTools.forward(deviceId, this.port, AB_NAME);            log.info("[{}] 端口转发 {} tcp:{} localabstract:minitouch", deviceId, success, port);            socket = new Socket(host, port);            socket.setKeepAlive(true);        } catch (IOException e) {            log.error("[{}] Minitouch client connect to {} error", deviceId, port, e);        }        log.info("[{}] {}与{}服务连接成功", deviceId, port);        while (isRunning.get()) {            if(socket.isConnected() && !socket.isClosed()) {                try {                    inputStream = socket.getInputStream();                    this.outputStream = socket.getOutputStream();                    handleServerResponse(inputStream);                    log.debug("[{}] Minitouch client disconnect {}", deviceId, port);                } catch (Exception e) {                    if(isRunning.get()) {                        log.error("[{}] Minitouch client error {}", deviceId, port, e);                        try {                            TimeUnit.SECONDS.sleep(1);                        } catch (InterruptedException e1) {                        }                    }                }            }        }        // 执行完成之后关闭        this.isRunning.set(false);        IOUtils.closeQuietly(this.socket);    }    private void handleServerResponse(InputStream inputStream) throws IOException {        // 缓存        byte[] chunk = new byte[1024*100];        // 读取的长度        int len = 0;        while (len >= 0) {            if(inputStream.available() <= 0) {                try {                    TimeUnit.MILLISECONDS.sleep(1);                } catch (InterruptedException e) {                }                continue;            }            len = inputStream.read(chunk);            String minitouchInfo = new String(chunk, StandardCharsets.UTF_8);            // v <version>            // ^ <max-contacts> <max-x> <max-y> <max-pressure>            // $ <pid>            StringTokenizer minitouchInfoTokenizer = new StringTokenizer(minitouchInfo);            log.info("[{}] Minitouch client out: {}", deviceId, StringUtils.trim(minitouchInfo));            int maxX = this.size.getWidth();            int maxY = this.size.getHeight();            while(minitouchInfoTokenizer.hasMoreTokens()){                String token = minitouchInfoTokenizer.nextToken();                if("^".equals(token)) {                    // 去掉第一个                    minitouchInfoTokenizer.nextToken();                    maxX = Integer.parseInt(minitouchInfoTokenizer.nextToken());                    maxY = Integer.parseInt(minitouchInfoTokenizer.nextToken());                }            }            this.PercentX = (double)this.size.getWidth() / maxX;            this.PercentY = (double)this.size.getHeight() / maxY;        }    }    public void sendEvent(String str) throws IOException {        if (this.outputStream != null) {            try {                log.debug("old cmd is {}", str);                String newcmd = pointConvert(str);                log.debug("new cmd is {}", newcmd);                outputStream.write(newcmd.getBytes());            } catch (CommandFailureException e) {                log.error("[{}] Minitouch client error", deviceId, e);            }        }    }    /**     *@Description: 通过minitouch的可点击范围获得真实点击位置     *@Param: [cmd]     *@Return: java.lang.String     *@Author: wen     *@Date: 2018/5/3     */    private String pointConvert(String cmd) throws CommandFailureException {        int x;        int y;        // m <contact> <x> <y> <pressure>        // d <contact> <x> <y> <pressure>        if(cmd.startsWith("d") || cmd.startsWith("m")){            String[] m = cmd.trim().split("\\s+");            try{                x = Integer.parseInt(m[2]);                y = Integer.parseInt(m[3]);                return String.format("%s %s %s %s %s\nc\n", m[0], m[1], (int)(x/PercentX), (int)(y/PercentY), m[4]);            }catch (NumberFormatException e){                log.error("point str error, {}", cmd);            }            throw new CommandFailureException("点击命令解析失败");        }else{            return cmd;        }    }    /**     * 获取屏幕支持     * @return PhysicalSize     * @throws Exception 获取失败     */    protected PhysicalSize getSize() {        if (size == null) {            size = ADBCommandUtils.getPhysicalSize(deviceId);        }        return size;    }    /**     * 检查是否关闭     */    protected void checkClosed() {        if (!this.isRunning.get()) {            throw new IllegalStateException("Minitouch 客户端已关闭");        }    }    public static void main(String[] args) {        MinitouchClient s = new MinitouchClient("8c2b6aee");        s.start();    }}