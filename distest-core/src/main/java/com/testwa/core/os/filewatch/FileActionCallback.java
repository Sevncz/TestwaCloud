package com.testwa.core.os.filewatch;

import java.io.File;

public abstract class FileActionCallback {

    public void delete(File file) {}

    public void modify(File file) {}

    public void create(File file) {}

}