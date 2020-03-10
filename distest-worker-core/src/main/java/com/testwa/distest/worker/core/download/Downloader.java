package com.testwa.distest.worker.core.download;import com.testwa.distest.worker.core.exception.DownloadFailException;import lombok.extern.slf4j.Slf4j;import java.io.FileOutputStream;import java.io.IOException;import java.net.HttpURLConnection;import java.net.URL;import java.nio.channels.Channels;import java.nio.channels.FileChannel;import java.nio.channels.ReadableByteChannel;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.Collections;import java.util.HashSet;import java.util.Set;import java.util.concurrent.TimeUnit;/** * @Program: distest * @Description: 文件下载器 * @Author: wen * @Create: 2018-04-19 11:30 **/@Slf4jpublic class Downloader implements RBCWrapperDelegate {    private static final Set<String> LOCK_HELD = Collections.synchronizedSet(new HashSet<>());    public void start(String fromUrl, String savepath) throws DownloadFailException, IOException {        while(!LOCK_HELD.add(fromUrl)){            try {                log.debug("等待中........");                TimeUnit.MILLISECONDS.sleep(1000);            } catch (InterruptedException e) {                e.printStackTrace();            }        }        ReadableByteChannel rbc = null;        try{            URL website = new URL(fromUrl);            Path toLocal = Paths.get(savepath);            if(!Files.exists(toLocal.getParent())){                Files.createDirectories(toLocal.getParent());            }else{                log.debug("下载文件存在! {} ", savepath);                Files.delete(toLocal);            }            FileChannel channel = new FileOutputStream(savepath).getChannel();            log.info("[Downloader] from url: {} ", fromUrl);            rbc = new RBCWrapper(Channels.newChannel(website.openStream()), contentLength(website), this, fromUrl);            channel.transferFrom(rbc, 0, Long.MAX_VALUE);        } catch (Exception e) {            log.error("[Downloader error] from url: {}!", fromUrl, e);            throw new DownloadFailException(e.getMessage());        } finally {            if(rbc != null){                rbc.close();            }            LOCK_HELD.remove(fromUrl);        }    }    @Override    public void rbcProgressCallback(RBCWrapper rbc, double progress) {        if((int)progress % 10 == 0){            log.debug(String.format("download from %s %d bytes received, %.02f%%", rbc.getFromUrl(), rbc.getReadSoFar(), progress));        }    }    private int contentLength(URL url) {        HttpURLConnection connection;        int contentLength = -1;        try {            HttpURLConnection.setFollowRedirects(false);            connection = (HttpURLConnection) url.openConnection();            connection.setRequestMethod("HEAD");            contentLength = connection.getContentLength();        } catch (Exception e) {        }        return contentLength;    }}