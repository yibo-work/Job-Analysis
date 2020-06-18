package com.utils;

import java.io.FileOutputStream;

/**
 * @author Clrvn
 * @description 文件读写工具类
 * @className FileUtil
 */
@SuppressWarnings("all")
public class FileUtil {

    //默认的路径是项目布置的static下面的upload中，当然你也可以换成你本地的D:\下面任意一个文件夹下面
    public static final String UPLOAD_PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\upload\\";

    /**
     * 上传文件
     *
     * @param file
     * @param filePath
     * @param fileName
     * @throws Exception
     */
    public static void uploadFile(byte[] file, String fileName) throws Exception {

        FileOutputStream out = new FileOutputStream(fileName);

        out.write(file);
        out.flush();
        out.close();
    }
}
