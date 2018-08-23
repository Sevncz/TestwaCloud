package com.testwa.core.shell;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.*;import java.io.*;import java.util.List;@Slf4jpublic abstract class AbstractCommonExecs {    private static final String DEFAULT_ENCODING = "UTF-8";    private String encoding = DEFAULT_ENCODING;        private CommandLine commandLine;    public AbstractCommonExecs(CommandLine commandLine) {        this.commandLine = commandLine;    }    /**     *@Description: 这个方法目前有问题，执行命令时会卡死     *@Param: []     *@Return: com.testwa.distest.common.shell.ExecResult     *@Author: wen     *@Date: 2018/4/17     */    public ExecResult exec_() throws IOException {        ExecResult er = new ExecResult();        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();        PipedOutputStream outputStream = new PipedOutputStream();        PipedInputStream pis = new PipedInputStream();        pis.connect(outputStream);        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();        CodeInfoCallback codeInfoCb = new CodeInfoCallback();        StdOutputCallback stdoutCb = new StdOutputCallback();        ErrorOutputCallback stderrCb = new ErrorOutputCallback();        String stdout = null;        String stderr = null;        try {            DefaultExecutor executor = new DefaultExecutor();            log.info("Executing script {}", this.commandLine.toString().replace(",", ""));            if(supportWatchdog()) {                executor.setWatchdog(getWatchdog());            }            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);            executor.setStreamHandler(streamHandler);            int ret = executor.execute(commandLine);            readInputStream(pis, stdoutCb, codeInfoCb);            pis.close();            readErrorStream(errorStream, stderrCb);            stdout = join(stdoutCb.getLines());            stderr = stderrCb.getErrors();            log.info("output from script {} is {}", this.commandLine.toString().replace(",", ""), stdout);            log.info("error output from script {} is {}", this.commandLine.toString().replace(",", ""), stderr);            log.info("exit code from script {} is {}", this.commandLine.toString().replace(",", ""), ret);            er.setStdout(stdout);            er.setStderr(stderr);            er.setCodeInfo(codeInfoCb.getCodeInfo());            er.setExitCode(ret);            return er;        } catch (ExecuteException e) {            if(pis != null) {                readInputStream(pis, stdoutCb, codeInfoCb);                pis.close();            }            if(errorStream != null) {                readErrorStream(errorStream, stderrCb);            }            stdout = join(stdoutCb.getLines());            stderr = stderrCb.getErrors();            int ret = e.getExitValue();            log.info("output from script {} is {}", this.commandLine.toString().replace(",", ""), stdout);            log.info("error output from script {} is {}", this.commandLine.toString().replace(",", ""), stderr);            log.info("exit code from script {} is {}", this.commandLine.toString().replace(",", ""), ret);            er.setStdout(stdout);            er.setStderr(stderr);            er.setCodeInfo(codeInfoCb.getCodeInfo());            er.setExitCode(ret);            return er;        }    }    /**     * 接口回调的方式解析脚本的错误输出     * @param baos     * @param cbs     * @throws IOException     */    private void readErrorStream(ByteArrayOutputStream baos, OutputCallback ...cbs) throws IOException {        String err =  baos.toString(getEncoding());        for(OutputCallback cb : cbs) {            cb.parse(err);        }    }    /**     * 接口回调的方式解析脚本的标准输出     * @param pis     * @param cbs     * @throws IOException     */    private void readInputStream(PipedInputStream pis, OutputCallback ...cbs) throws IOException {        BufferedReader br = new BufferedReader(new InputStreamReader(pis, getEncoding()));        String line = null;        while((line = br.readLine()) != null) {            for(OutputCallback cb : cbs) {                cb.parse(line);            }        }    }    protected String join(List<String> arguments) {        if(arguments == null || arguments.isEmpty()) {            return "";        }        StringBuilder sb = new StringBuilder();        for(String arg : arguments) {            sb.append(" ").append(arg);        }        return sb.toString();    }    /**     * @return the encoding     */    protected String getEncoding() {        return encoding;    }    /**     * @param encoding the encoding to set     */    public void setEncoding(String encoding) {        this.encoding = encoding;    }    public abstract boolean supportWatchdog();    public abstract ExecuteWatchdog getWatchdog();}