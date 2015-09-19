package com.koolew.mars.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

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
}
