package com.koolew.mars.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by jinchangzhu on 8/1/15.
 */
public class FileUtil {

    public static void deleteFilesFromDir(File file, FileFilter filter) {
        if (file.isFile()) {
            if (filter.accept(file)) {
                file.delete();
            }
        }
        else if (file.isDirectory()) {
            File subFiles[] = file.listFiles();
            for (File f: subFiles) {
                deleteFilesFromDir(f, filter);
            }
        }
    }
}
