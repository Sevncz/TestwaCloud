package com.testwa.distest.common.shell;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: 系统输出回调 * @Author: wen * @Create: 2018-04-17 11:23 **/public class StdOutputCallback implements OutputCallback {    private List<String> lines = new ArrayList<>();    @Override    public void parse(String line) {        lines.add(line);    }    public List<String> getLines() {        return lines;    }}