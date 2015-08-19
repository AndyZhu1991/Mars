package com.koolew.mars.utils;

import java.io.File;
import java.io.FileFilter;

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
}
