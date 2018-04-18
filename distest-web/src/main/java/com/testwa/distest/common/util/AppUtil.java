package com.testwa.distest.common.util;import com.dd.plist.NSArray;import com.dd.plist.NSDictionary;import com.dd.plist.NSString;import com.dd.plist.PropertyListParser;import com.google.common.io.Files;import com.testwa.distest.common.shell.UTF8CommonExecs;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.tools.zip.ZipEntry;import org.apache.tools.zip.ZipFile;import java.io.*;import java.nio.file.Paths;import java.util.Enumeration;import java.util.HashMap;import java.util.Map;import java.util.zip.ZipInputStream;/** * @Program: distest * @Description: 从APK里解压除icon图片并存放在磁盘上 * @Author: wen * @Create: 2018-04-11 15:24 **/@Slf4jpublic class AppUtil {    /**     *@Description: 从apk中提取图标的输入流     *@Param: [apkpath, fileName]     *@Return: java.io.InputStream     *@Author: wen     *@Date: 2018/4/17     */    public static InputStream extractFileFromApk(String apkpath, String fileName) {        try {            ZipFile zFile = new ZipFile(apkpath);            ZipEntry entry = zFile.getEntry(fileName);            entry.getComment();            entry.getCompressedSize();            entry.getCrc();            entry.isDirectory();            entry.getSize();            entry.getMethod();            InputStream stream = zFile.getInputStream(entry);            return stream;        } catch (IOException e) {            e.printStackTrace();        }        return null;    }    /**     *@Description: 从apk中提取图标并保存     *@Param: [apkpath, applicationIcon, outputPath]     *@Return: void     *@Author: wen     *@Date: 2018/4/17     */    public static void extractFileFromApk(String apkpath, String applicationIcon, String outputPath) throws Exception {        InputStream is = extractFileFromApk(apkpath, applicationIcon);        File file = new File(outputPath);        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), 1024);        byte[] b = new byte[1024];        BufferedInputStream bis = new BufferedInputStream(is, 1024);        while(bis.read(b) != -1){            bos.write(b);        }        bos.flush();        is.close();        bis.close();        bos.close();    }    private static File getZipInfo(File file, String unzipDirectory) throws Exception {        // 定义输入输出流对象        InputStream input = null;        OutputStream output = null;        File result = null;        File unzipFile = null;        ZipFile zipFile = null;        try {            // 创建zip文件对象            zipFile = new ZipFile(file);            // 创建本zip文件解压目录            String name = file.getName().substring(0,file.getName().lastIndexOf("."));            unzipFile = new File(unzipDirectory + "/" + name);            if (unzipFile.exists()){                unzipFile.delete();            }            unzipFile.mkdir();            // 得到zip文件条目枚举对象            Enumeration<ZipEntry> zipEnum = zipFile.getEntries();            // 定义对象            ZipEntry entry = null;            String entryName = null;            String names[] = null;            int length;            // 循环读取条目            while (zipEnum.hasMoreElements()) {                // 得到当前条目                entry = zipEnum.nextElement();                entryName = new String(entry.getName());                // 用/分隔条目名称                names = entryName.split("\\/");                length = names.length;                for (int v = 0; v < length; v++) {                    if(entryName.endsWith(".app/Info.plist")){ // 为Info.plist文件,则输出到文件                        input = zipFile.getInputStream(entry);                        result = new File(unzipFile.getAbsolutePath()+ "/Info.plist");                        output = new FileOutputStream(result);                        byte[] buffer = new byte[1024 * 8];                        int readLen = 0;                        while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1){                            output.write(buffer, 0, readLen);                        }                        break;                    }                }            }        } catch (Exception ex) {            ex.printStackTrace();        } finally {            if (input != null)                input.close();            if (output != null) {                output.flush();                output.close();            }            // 必须关流，否则文件无法删除            if(zipFile != null){                zipFile.close();            }        }        return result;    }    public static Map<String, String> getIpaInfo(File ipaFile) {        InputStream inStream = null;        FileOutputStream fs = null;        try{            int byteread = 0;            String filename = ipaFile.getAbsolutePath().replaceAll(".ipa", ".zip");            File zipfile = new File(filename);            if (ipaFile.exists()){                // 创建一个Zip文件//                inStream = new FileInputStream(ipaFile);//                fs = new FileOutputStream(zipfile);//                byte[] buffer = new byte[1444];//                while ((byteread = inStream.read(buffer)) != -1){//                    fs.write(buffer,0,byteread);//                }                Files.copy(ipaFile, zipfile);                File plistFile = getZipInfo(zipfile, zipfile.getParent());                Map<String, String> properties = getIpaInfoMap(plistFile);                String icon = properties.get("icon");                String newIconName = extractIconFromZip(zipfile, icon, ipaFile.getParent());                properties.put("icon", newIconName);                // 如果有必要，应该删除解压的结果文件                plistFile.delete();                plistFile.getParentFile().delete();                zipfile.delete();                return properties;            }        }catch(Exception e){            e.printStackTrace();        }finally {            if(inStream != null){                try {                    inStream.close();                } catch (IOException e) {                    e.printStackTrace();                }            }            if(fs != null){                try {                    fs.close();                } catch (IOException e) {                    e.printStackTrace();                }            }        }        return null;    }    private static String extractIconFromZip(File zipFile, String icon, String outputPath){        InputStream is = null;        ZipInputStream zipIns = null;        try {            is = new FileInputStream(zipFile);            zipIns = new ZipInputStream(is);            java.util.zip.ZipEntry ze;            String newIconName = "";            while ((ze = zipIns.getNextEntry()) != null) {                if (!ze.isDirectory()) {                    String name = ze.getName();                    if(name.contains(icon.trim())){                        newIconName = icon + ".png";                        FileOutputStream fos = new FileOutputStream(new File(outputPath + File.separator + newIconName ));                        int chunk = 0;                        byte[] data = new byte[1024];                        while(-1!=(chunk=zipIns.read(data))){                            fos.write(data, 0, chunk);                        }                        fos.close();                        break;                    }                }            }            return newIconName;        } catch (FileNotFoundException e) {            e.printStackTrace();        } catch (IOException e) {            e.printStackTrace();        }finally {            if(is != null){                try {                    is.close();                } catch (IOException e) {                    e.printStackTrace();                }            }            if(zipIns != null){                try {                    zipIns.close();                } catch (IOException e) {                    e.printStackTrace();                }            }        }        return null;    }    private static String getFileNameNoEx(String filename) {        if ((filename != null) && (filename.length() > 0)) {            int dot = filename.lastIndexOf('.');            if ((dot >-1) && (dot < (filename.length()))) {                return filename.substring(0, dot);            }        }        return filename;    }    public static Map<String,String> getIpaInfoMap(File plist) throws Exception{        Map<String,String> map = new HashMap<>();        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plist);        // 应用包名        NSString parameters = (NSString) rootDict.objectForKey("CFBundleIdentifier");        map.put("CFBundleIdentifier", parameters.toString());        // 应用名称        parameters = (NSString) rootDict.objectForKey("CFBundleName");        map.put("CFBundleName", parameters.toString());        // 应用版本        parameters = (NSString) rootDict.objectForKey("CFBundleVersion");        map.put("CFBundleVersion", parameters.toString());        // itunes展示的版本号        parameters = (NSString) rootDict.objectForKey("CFBundleShortVersionString");        map.put("CFBundleShortVersionString", parameters.toString());        // 应用展示的名称        parameters = (NSString) rootDict.objectForKey("CFBundleDisplayName");        map.put("CFBundleDisplayName", parameters.toString());        // 应用所需IOS最低版本        parameters = (NSString) rootDict.objectForKey("MinimumOSVersion");        map.put("MinimumOSVersion", parameters.toString());        // 平台版本        parameters = (NSString) rootDict.objectForKey("DTPlatformVersion");        map.put("DTPlatformVersion", parameters.toString());        // 平台名称        parameters = (NSString) rootDict.objectForKey("DTPlatformName");        map.put("DTPlatformName", parameters.toString());        // DTXcodeBuild        parameters = (NSString) rootDict.objectForKey("DTXcodeBuild");        map.put("DTXcodeBuild", parameters.toString());        // DTSDKBuild        parameters = (NSString) rootDict.objectForKey("DTSDKBuild");        map.put("DTSDKBuild", parameters.toString());        NSDictionary iconDict = (NSDictionary) rootDict.get("CFBundleIcons");        //获取图标名称        String icon = "";        while (null != iconDict) {            if(iconDict.containsKey("CFBundlePrimaryIcon")){                NSDictionary CFBundlePrimaryIcon = (NSDictionary) iconDict.get("CFBundlePrimaryIcon");                if(CFBundlePrimaryIcon.containsKey("CFBundleIconFiles")){                    NSArray CFBundleIconFiles = (NSArray) CFBundlePrimaryIcon.get("CFBundleIconFiles");                    icon = CFBundleIconFiles.getArray()[0].toString();                    if(icon.contains(".png")){                        icon = icon.replace(".png", "");                    }                    log.info("获取ipa icon名称:" + icon);                    break;                }            }        }        map.put("icon", icon);        return map;    }    public static void main(String[] args) throws Exception {        File file = new File("/Users/wen/Documents/Testwa/测试app和脚本/ofo共享单车 2.16.1.ipa");//        Map<String,String> map = getIpaInfoMap(file);        Map<String,String> map = getIpaInfo(file);        for(String key : map.keySet()){            System.out.println(key+" : "+map.get(key));        }    }}