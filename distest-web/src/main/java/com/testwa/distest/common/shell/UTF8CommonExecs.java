package com.testwa.distest.common.shell;import com.github.cosysoft.device.shell.AndroidSdk;import org.apache.commons.exec.CommandLine;import org.apache.commons.exec.DefaultExecutor;import org.apache.commons.exec.ExecuteWatchdog;import org.apache.commons.exec.PumpStreamHandler;import java.io.ByteArrayOutputStream;import java.io.IOException;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: 支持字符编码设置 * @Author: wen * @Create: 2018-04-17 11:30 **/public class UTF8CommonExecs extends AbstractCommonExecs{    private CommandLine commandLine;    /**     *@Description:     *@Param: [commandLine]     *@Return:     *@Author: wen     *@Date:     */    public UTF8CommonExecs(CommandLine commandLine) {        super(commandLine);        this.commandLine = commandLine;    }    /* (non-Javadoc)     * @see com.bingosoft.proxy.helper.AbstractCommonExecs#supportWatchdog()     */    @Override    public boolean supportWatchdog() {        return true;    }    /* (non-Javadoc)     * @see com.bingosoft.proxy.helper.AbstractCommonExecs#getWatchdog()     */    @Override    public ExecuteWatchdog getWatchdog() {        return new ExecuteWatchdog(20000l);    }    //提供这个编码即可    public String getEncoding() {        return "UTF-8";    }    public String exec() throws IOException {        DefaultExecutor executor = new DefaultExecutor();        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);        executor.setStreamHandler(streamHandler);        executor.execute(this.commandLine);        String out = outputStream.toString(getEncoding());  //设置编码        return out;    }    public static void main(String[] args) {        CommandLine line = new CommandLine(AndroidSdk.aapt());        line.addArgument("dump", false);        line.addArgument("badging", false);        line.addArgument("/Users/wen/Documents/Testwa/测试app和脚本/ofo-local-15560.apk", false);        UTF8CommonExecs executable = new UTF8CommonExecs(line);        String output = "";        try {            output = executable.exec();        } catch (IOException e) {            output = e.getCause().getMessage();        }        System.out.println(output);    }}