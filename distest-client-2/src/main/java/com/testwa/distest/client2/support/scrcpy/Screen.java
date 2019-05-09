package com.testwa.distest.client2.support.scrcpy;

import com.testwa.distest.client2.support.android.ADBTools;
import com.testwa.distest.client2.support.android.AndroidDeviceStore;
import com.testwa.distest.client2.support.android.PhysicalSize;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.in;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import static org.bytedeco.javacpp.avformat.avio_alloc_context;
import static org.bytedeco.javacpp.avutil.av_malloc;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.cvMat;
import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvDecodeImage;

/**
 * @author wen
 * @create 2019-05-09 19:33
 */
@Slf4j
public class Screen extends Thread implements Closeable {
    private FFmpegFrameRecorder recorder;
    private static final int FRAME_RATE = 25;
    private static final int MOTION_FACTOR = 1;

    private static final int QUEUE_SIZE = 100;
    private BlockingQueue<byte[]> frameQueue;

    private final int BUF_SIZE = 1400;

    private float zoom = 0.3f;
    private PhysicalSize size;
    private String outputFile;
    private String formate;
    private int videoCodec;

    boolean recording = false;

    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    /** 是否重启 */
    private AtomicBoolean restart = new AtomicBoolean(false);

    public Screen(String deviceId) {
        this.size = ADBTools.getPhysicalSize(deviceId);
    }

    @Override
    public void close() throws IOException {
        isRunning.set(false);
        restart.set(false);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {

        }
        if(recorder != null && recording){
            try {
                recorder.stop();
            } catch (FrameRecorder.Exception e) {
                log.error("JavaCVMp4Encoder stop error.", e);
            }
            try {
                recorder.release();
            } catch (FrameRecorder.Exception e) {
                log.error("JavaCVMp4Encoder release error.", e);
            }
        }
        recorder = null;
    }

    @Override
    public synchronized void start() {
        if (this.isRunning.get()) {
            throw new IllegalStateException("VideoRecorder服务已运行");
        } else {
            this.isRunning.set(true);
        }
        frameQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        super.start();
    }

    @Override
    public void run() {
        avformat.AVFormatContext formatContext = avformat.avformat_alloc_context();
        if(formatContext == null) {
            return;
        }
        byte[] buffer = new byte[BUF_SIZE];
        BytePointer inputBuffer = new BytePointer(av_malloc(BUF_SIZE));

        avformat.AVIOContext pIoCtx = avio_alloc_context(inputBuffer, BUF_SIZE, 0, null, read_buffer, null,null);
        if(pIoCtx == null){
            log.error("Could not allocate avio context");
            return;
        }
        if(avformat_open_input(formatContext, "0", null, null) < 0) {
            log.error("Could not open video stream");
            return;
        }

        avcodec.AVCodec codec = avcodec.avcodec_find_decoder(avcodec.AV_CODEC_ID_H264);
        if(codec == null) {
            log.error("H.264 decoder not found");
            return;
        }

        initRecorder();

        try {
            recorder.start();
            recording = true;
        } catch (FrameRecorder.Exception e) {
            log.warn("视频录制错误", e);
        }
        while(recording && this.isRunning.get()) {
            try {
                byte[] data = frameQueue.poll(20, TimeUnit.MILLISECONDS);
                if(data != null) {
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    avformat.Read_packet_Pointer_BytePointer_int read_buffer=new avformat.Read_packet_Pointer_BytePointer_int(){

        @Override
        public int call(Pointer opaque, BytePointer buf, int buf_size) {
            byte[] bytebuf=new byte[buf_size];
            int size=-1;
            try {
                size = in.read(bytebuf, 0, buf_size);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buf.position(0);
            buf.put(bytebuf, 0, size);
            return size;
        }
    };

    private void encodeFrame(Frame frame) {
        if(recorder != null) {
            try {
                recorder.record(frame);
            } catch (Exception e) {
                log.warn("abort {} {}", e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * @Description: initialize ffmpeg_recorder
     * @Param: []
     * @Return: void
     * @Author wen
     * @Date 2018/9/3 15:29
     */
    private void initRecorder() {
        int h = (int) (this.size.getHeight() * zoom);
        int w = (int) (this.size.getWidth() * zoom);

        int m_dst_h = (h >> 4) << 4;
        int m_dst_w = (w >> 4) << 4;

        this.recorder = new FFmpegFrameRecorder(outputFile, m_dst_w, m_dst_h, 1);
        this.recorder.setInterleaved(true);
        /**
         * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
         * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
         * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
         */
        recorder.setVideoOption("tune", "zerolatency");
        /**
         * 权衡quality(视频质量)和encode speed(编码速度) values(值)：
         * ultrafast(终极快),superfast(超级快), veryfast(非常快), faster(很快), fast(快),
         * medium(中等), slow(慢), slower(很慢), veryslow(非常慢)
         * ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；而veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
         * 参考：https://trac.ffmpeg.org/wiki/Encode/H.264 官方原文参考：-preset ultrafast
         * as the name implies provides for the fastest possible encoding. If
         * some tradeoff between quality and encode speed, go for the speed.
         * This might be needed if you are going to be transcoding multiple
         * streams on one machine.
         */
        recorder.setVideoOption("preset", "ultrafast");
        /**
         * 参考转流命令: ffmpeg
         * -i'udp://localhost:5000?fifo_size=1000000&overrun_nonfatal=1' -crf 30
         * -preset ultrafast -acodec aac -strict experimental -ar 44100 -ac
         * 2-b:a 96k -vcodec libx264 -r 25 -b:v 500k -f flv 'rtmp://<wowza
         * serverIP>/live/cam0' -crf 30
         * -设置内容速率因子,这是一个x264的动态比特率参数，它能够在复杂场景下(使用不同比特率，即可变比特率)保持视频质量；
         * 可以设置更低的质量(quality)和比特率(bit rate),参考Encode/H.264 -preset ultrafast
         * -参考上面preset参数，与视频压缩率(视频大小)和速度有关,需要根据情况平衡两大点：压缩率(视频大小)，编/解码速度 -acodec
         * aac -设置音频编/解码器 (内部AAC编码) -strict experimental
         * -允许使用一些实验的编解码器(比如上面的内部AAC属于实验编解码器) -ar 44100 设置音频采样率(audio sample
         * rate) -ac 2 指定双通道音频(即立体声) -b:a 96k 设置音频比特率(bit rate) -vcodec libx264
         * 设置视频编解码器(codec) -r 25 -设置帧率(frame rate) -b:v 500k -设置视频比特率(bit
         * rate),比特率越高视频越清晰,视频体积也会变大,需要根据实际选择合理范围 -f flv
         * -提供输出流封装格式(rtmp协议只支持flv封装格式) 'rtmp://<FMS server
         * IP>/live/cam0'-流媒体服务器地址
         */
        recorder.setVideoOption("crf","28");
        recorder.setVideoBitrate((int)((w * h * FRAME_RATE) * MOTION_FACTOR * 0.07));
        // 封装格式
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_NV21);
        recorder.setFormat(this.formate);
        // 编/解码器
        recorder.setVideoCodec(this.videoCodec);
//        recorder.setVideoQuality(0); // lossless
        // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
        recorder.setFrameRate(FRAME_RATE);
    }

    public void offer(byte[] data) {
        frameQueue.offer(data);
    }

}
