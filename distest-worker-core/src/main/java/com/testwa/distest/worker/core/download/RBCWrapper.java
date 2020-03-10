package com.testwa.distest.worker.core.download;/** * @Program: distest * @Description: RBCWrapper * @Author: wen * @Create: 2018-04-19 11:30 **/import java.io.IOException;import java.nio.ByteBuffer;import java.nio.channels.ReadableByteChannel;public final class RBCWrapper implements ReadableByteChannel {    private RBCWrapperDelegate delegate;    private long expectedSize;    private ReadableByteChannel rbc;    private long readSoFar;    private String fromUrl;    RBCWrapper(ReadableByteChannel rbc, long expectedSize, RBCWrapperDelegate delegate, String fromUrl) {        this.delegate = delegate;        this.expectedSize = expectedSize;        this.rbc = rbc;        this.fromUrl = fromUrl;    }    public void close() throws IOException {        rbc.close();    }    public long getReadSoFar() {        return readSoFar;    }    public boolean isOpen() {        return rbc.isOpen();    }    public int read(ByteBuffer bb) throws IOException {        int n;        double progress;        if ((n = rbc.read(bb)) > 0) {            readSoFar += n;            progress = expectedSize > 0 ? (double) readSoFar / (double) expectedSize * 100.0 : -1.0;            delegate.rbcProgressCallback(this, progress);        }        return n;    }    public String getFromUrl(){        return fromUrl;    }}