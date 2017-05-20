package com.testwa.distest.client.android.util;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.testwa.core.utils.Identities;
import com.testwa.distest.client.appium.manager.AvailabelPorts;
import com.testwa.distest.client.appium.utils.Config;
import com.testwa.distest.client.rpc.client.ScreenCaptureClient;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.MinicapServiceBuilder;
import com.testwa.distest.client.util.Constant;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceUnixSocketNamespace;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import io.grpc.testwa.device.ScreenCaptureRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniCapUtil {
	private static Logger LOG = LoggerFactory.getLogger(MiniCapUtil.class);
	// CPU架构的种类
	public static final String ABIS_ARM64_V8A = "arm64-v8a";
	public static final String ABIS_ARMEABI_V7A = "armeabi-v7a";
	public static final String ABIS_X86 = "x86";
	public static final String ABIS_X86_64 = "x86_64";

	private Queue<byte[]> dataQueue = new LinkedBlockingQueue<>();
    private static int PORT;

	private Banner banner = new Banner();
	private Socket socket;
	private IDevice device;
	private static final String ABI_COMMAND = "ro.product.cpu.abi";
	private static final String SDK_COMMAND = "ro.build.version.sdk";
	private static final String REL_COMMAND = "ro.build.version.release";
	private static final String MINICAP_BIN = "minicap";
	private static final String MINICAP_SO = "minicap.so";
	private static final String MINICAP_NOPIE = "minicap-nopie";
    private String BIN = "";
	private String MINICAP_CHMOD_COMMAND = "chmod 777 %s/%s";
	private String MINICAP_WM_SIZE_COMMAND = "wm size";
    private static final String MINICAP_DIR = "/data/local/tmp/minicap-devel";
    private String MINICAP_DIR_COMMAND = String.format("mkdir %s 2>/dev/null || true", MINICAP_DIR);
//	private String MINICAP_START_COMMAND = "LD_LIBRARY_PATH=%s %s/%s -P %s@%s/0";
	private String MINICAP_TAKESCREENSHOT_COMMAND = "LD_LIBRARY_PATH=%s %s/%s -P %s@%s/0 -s > %s";
	private boolean isRunning = false;
	private String size;
    private ScreenCaptureClient screenCaptureClient;
    private AdbDriverService service;

    public MiniCapUtil(IDevice device, String host, Integer port) {
		this.device = device;
        this.screenCaptureClient = new ScreenCaptureClient(host, port);
        init();
	}

    //判断是否支持minicap
	public boolean isSupoort(){
		String supportCommand = String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -t", size,size);
		String output = executeShellCommand(supportCommand);
		if(output.trim().endsWith("OK")){
			return true;
		}
		return false;
	}
	
	/**
	 * 将minicap的二进制和.so文件push到/data/local/tmp文件夹下，启动minicap服务
	 */
	private void init() {

		String abi = device.getProperty(ABI_COMMAND);
		String sdk = device.getProperty(SDK_COMMAND);
		String rel = device.getProperty(REL_COMMAND);

        if(StringUtils.isNotBlank(sdk)){
            Integer sdkvalue = Integer.parseInt(sdk);
            if(sdkvalue >= 16){
                BIN = MINICAP_BIN;
            }else{
                BIN = MINICAP_NOPIE;
            }
        }else{
            LOG.error("SDK is null");
        }
        String minicapBinPath = Paths.get(Constant.getMinicapBin(), abi, BIN).toAbsolutePath().toString();
        String minicapSoPath = Paths.get(Constant.getMinicapSo(), "android-" + sdk, abi, MINICAP_SO).toAbsolutePath().toString();

        // Create a directory for minicap resources
        executeShellCommand(MINICAP_DIR_COMMAND);

		try {
            AvailabelPorts ap = new AvailabelPorts();
			PORT = ap.getPort();
            // 将minicap的可执行文件和.so文件一起push到设备中
			String minicapCheckOut = executeShellCommand(String.format("ls %s | grep %s", MINICAP_DIR, BIN));
            // Upload the binary
			if(!minicapCheckOut.contains(BIN)){
                device.pushFile(minicapBinPath, MINICAP_DIR + "/" + BIN);
			}
            // Upload the shared library
			if(!minicapCheckOut.contains(MINICAP_SO)){
				device.pushFile(minicapSoPath, MINICAP_DIR + "/" + MINICAP_SO);
			}
			executeShellCommand(String.format(MINICAP_CHMOD_COMMAND, MINICAP_DIR, BIN));
			// 端口转发
			device.createForward(PORT, "minicap", DeviceUnixSocketNamespace.ABSTRACT);
			// 获取设备屏幕的尺寸
			String output = executeShellCommand(MINICAP_WM_SIZE_COMMAND);
			size = output.split(":")[1].trim();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String takeScreenShotOnce() {
        String filename = Identities.randomLong() + ".jpg";
        String savePath = Config.getString("minicap.base.path") + "/" + filename;
        String localPath = Paths.get(Constant.localScreenshotPath, filename).toString();
		String takeScreenShotCommand = String.format(MINICAP_TAKESCREENSHOT_COMMAND, MINICAP_DIR, MINICAP_DIR, BIN, size, size, savePath);
        executeShellCommand(takeScreenShotCommand);
        try {
            device.pullFile(savePath, localPath);
            // TODO 删除手机上的截图
        } catch (IOException e) {
			LOG.error("IOException, savePath: {}, localPath: {}", savePath, localPath, e);
        } catch (AdbCommandRejectedException e) {
			LOG.error("AdbCommandRejectedException, savePath: {}, localPath: {}", savePath, localPath, e);
        } catch (TimeoutException e) {
			LOG.error("TimeoutException, savePath: {}, localPath: {}", savePath, localPath, e);
        } catch (SyncException e) {
			LOG.error("SyncException, savePath: {}, localPath: {}", savePath, localPath, e);
        }

        return filename;
    }

	private String executeShellCommand(String command) {
		CollectingOutputReceiver output = new CollectingOutputReceiver();
        try {
            device.executeShellCommand(command, output);
        } catch (TimeoutException e) {
            LOG.error("TimeoutException, command: {}", command, e);
        } catch (AdbCommandRejectedException e) {
            LOG.error("AdbCommandRejectedException, command: {}", command, e);
        } catch (ShellCommandUnresponsiveException e) {
            LOG.error("ShellCommandUnresponsiveException, command: {}", command, e);
        } catch (IOException e) {
            LOG.error("IOException, command: {}", command, e);
        }
		return output.getOutput();
	}

	public void startScreenListener() {
		isRunning = true;
        // 启动minicap服务
        service = new MinicapServiceBuilder()
                .whithBin(BIN)
                .whithDeviceId(device.getSerialNumber())
                .whithLibPath(MINICAP_DIR)
                .whithSize(size)
                .build();
        service.start();
        while(true){
            if(service.isRunning()){
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e) {
                throw new IllegalStateException("service interrupted", e);
            }
        }
		Thread frame = new Thread(new ImageBinaryFrameCollector());
		frame.start();
		Thread convert = new Thread(new ImageConverter());
		convert.start();

	}

	public void stopScreenListener() {
		isRunning = false;
        if(service != null && service.isRunning()){
            service.stop();
        }
	}

	// java合并两个byte数组
	private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	private byte[] subByteArray(byte[] byte1, int start, int end) {
		byte[] byte2 = new byte[0];
        int length = end - start;
        if(length < 0){
            length = 0;
        }
		try {
			byte2 = new byte[length];
		} catch (NegativeArraySizeException e) {
			e.printStackTrace();
		}
		System.arraycopy(byte1, start, byte2, 0, length);
		return byte2;
	}

	class ImageBinaryFrameCollector implements Runnable {
		private InputStream stream = null;

		public void run() {
			LOG.debug("图片二进制数据收集器已经开启");
			try {
				socket = new Socket("localhost", PORT);
				stream = socket.getInputStream();
				int len = 4096;
				while (isRunning) {
					byte[] buffer;
					buffer = new byte[len];
					int realLen = stream.read(buffer);
					if (buffer.length != realLen) {
						buffer = subByteArray(buffer, 0, realLen);
					}
					dataQueue.add(buffer);
					if(buffer.length == 0){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
                }
			} catch (IOException e) {
				LOG.error("ImageBinaryFrameCollector error", e);
			} finally {
				if (socket != null && socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			LOG.debug("图片二进制数据收集器已关闭");
		}

	}

	class ImageConverter implements Runnable {
		protected int readBannerBytes = 0;
        protected int bannerLength = 2;
        protected int readFrameBytes = 0;
        protected int frameBodyLength = 0;
        protected byte[] frameBody = new byte[0];

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			long start = System.currentTimeMillis();
			while (isRunning) {
                if (dataQueue.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
					continue;
				}
				byte[] buffer = dataQueue.poll();
				int len = buffer.length;
				for (int cursor = 0; cursor < len;) {
					int byte10 = buffer[cursor] & 0xff;
					if (readBannerBytes < bannerLength) {
						cursor = parserBanner(cursor, byte10);
					} else if (readFrameBytes < 4) {
						// 第二次的缓冲区中前4位数字和为frame的缓冲区大小
						frameBodyLength += (byte10 << (readFrameBytes * 8));
						cursor += 1;
						readFrameBytes += 1;
						// LOG.debug("解析图片大小 = " + readFrameBytes);
					} else {
						if (len - cursor >= frameBodyLength) {
							LOG.debug("frameBodyLength = " + frameBodyLength);
							byte[] subByte = subByteArray(buffer, cursor,
									cursor + frameBodyLength);
							frameBody = byteMerger(frameBody, subByte);
							if ((frameBody[0] != -1) || frameBody[1] != -40) {
								LOG.error("Frame body does not start with JPG header");
								return;
							}
							final byte[] finalBytes = subByteArray(frameBody,
									0, frameBody.length);
							new Thread(() -> {
								ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()
										.setImg(ByteString.copyFrom(finalBytes))
										.setName("xxx")
										.setSerial(device.getSerialNumber())
										.build();
								screenCaptureClient.sender(request);
							}).start();
							long current = System.currentTimeMillis();
							start = current;
							cursor += frameBodyLength;
							restore();
						} else {
							LOG.debug("所需数据大小 : " + frameBodyLength);
							byte[] subByte = subByteArray(buffer, cursor, len);
							frameBody = byteMerger(frameBody, subByte);
							frameBodyLength -= (len - cursor);
							readFrameBytes += (len - cursor);
							cursor = len;
						}
					}
				}
			}

		}

        protected void restore() {
			frameBodyLength = 0;
			readFrameBytes = 0;
			frameBody = new byte[0];
		}

        protected int parserBanner(int cursor, int byte10) {
			switch (readBannerBytes) {
			case 0:
				// version
				banner.setVersion(byte10);
				break;
			case 1:
				// length
				bannerLength = byte10;
				banner.setLength(byte10);
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				// pid
				int pid = banner.getPid();
				pid += (byte10 << ((readBannerBytes - 2) * 8));
				banner.setPid(pid);
				break;
			case 6:
			case 7:
			case 8:
			case 9:
				// real width
				int realWidth = banner.getReadWidth();
				realWidth += (byte10 << ((readBannerBytes - 6) * 8));
				banner.setReadWidth(realWidth);
				break;
			case 10:
			case 11:
			case 12:
			case 13:
				// real height
				int realHeight = banner.getReadHeight();
				realHeight += (byte10 << ((readBannerBytes - 10) * 8));
				banner.setReadHeight(realHeight);
				break;
			case 14:
			case 15:
			case 16:
			case 17:
				// virtual width
				int virtualWidth = banner.getVirtualWidth();
				virtualWidth += (byte10 << ((readBannerBytes - 14) * 8));
				banner.setVirtualWidth(virtualWidth);

				break;
			case 18:
			case 19:
			case 20:
			case 21:
				// virtual height
				int virtualHeight = banner.getVirtualHeight();
				virtualHeight += (byte10 << ((readBannerBytes - 18) * 8));
				banner.setVirtualHeight(virtualHeight);
				break;
			case 22:
				// orientation
				banner.setOrientation(byte10 * 90);
				break;
			case 23:
				// quirks
				banner.setQuirks(byte10);
				break;
			}

			cursor += 1;
			readBannerBytes += 1;

			if (readBannerBytes == bannerLength) {
				LOG.debug(banner.toString());
			}
			return cursor;
		}

	}

}