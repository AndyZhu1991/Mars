package com.koolew.android.utils;

import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jinchangzhu on 8/1/15.
 */
public class FileUtil {

    public static void deleteFilesFromDir(File file, FileFilter filter) {
        if (file.isDirectory()) {
            File subFiles[] = file.listFiles();
            for (File f: subFiles) {
                deleteFilesFromDir(f, filter);
            }
        }
        if (filter.accept(file)) {
            file.delete();
        }
    }

    public static void deleteFileOrDir(String name) {
        deleteFileOrDir(new File(name));
    }

    public static void deleteFileOrDir(File file) {
        deleteFilesFromDir(file, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        });
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                File newFile = new File(newPath);
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

    public static void copyFromAssets(Context context, String assetsPath, String dstPath) {
        File desFile = new File(dstPath);
        if (!desFile.exists()) {
            try {
                InputStream is = context.getAssets().open(assetsPath);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(desFile);
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /**
     * 生成文件的md5校验值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest messagedigest = MessageDigest.getInstance("MD5");
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}
