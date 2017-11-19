package com.testwa.core.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ListOutputStream extends OutputStream {
    private final List<OutputStream> streams = new ArrayList();

    ListOutputStream() {
    }

    ListOutputStream add(OutputStream stream) {
        this.streams.add(stream);
        return this;
    }

    public void write(int i) throws IOException {
        Iterator it = this.streams.iterator();

        while(it.hasNext()) {
            OutputStream stream = (OutputStream)it.next();
            stream.write(i);
        }

    }

    public void write(byte[] var1) throws IOException {
        Iterator it = this.streams.iterator();

        while(it.hasNext()) {
            OutputStream stream = (OutputStream)it.next();
            stream.write(var1, 0, var1.length);
        }

    }

    public void write(byte[] var1, int var2, int var3) throws IOException {
        Iterator it = this.streams.iterator();

        while(it.hasNext()) {
            OutputStream stream = (OutputStream)it.next();
            stream.write(var1, var2, var3);
        }

    }

    public void flush() throws IOException {
        Iterator it = this.streams.iterator();

        while(it.hasNext()) {
            OutputStream stream = (OutputStream)it.next();
            stream.flush();
        }

    }

    public void close() throws IOException {
        Iterator it = this.streams.iterator();

        while(it.hasNext()) {
            OutputStream stream = (OutputStream)it.next();
            stream.close();
        }

    }
}
