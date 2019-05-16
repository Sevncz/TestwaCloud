package com.testwa.distest.jadb;

import java.io.IOException;

public class Subprocess {
    public Process execute(String[] command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
