package com.koolew.mars.downloadmanager;

import com.koolew.mars.copied.disklrucache.DiskLruCache;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jinchangzhu on 12/30/15.
 */
public interface DownloadDestination {

    public OutputStream getOutputStream();

    public static class FileDestination implements DownloadDestination {

        private String filePath;

        public FileDestination(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        @Override
        public OutputStream getOutputStream() {
            try {
                return new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class OutputStreamDestination implements DownloadDestination {

        private OutputStream outputStream;

        public OutputStreamDestination(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }
    }

    public static class DiskLruCacheEditorDestination extends OutputStreamDestination {

        private DiskLruCache.Editor editor;

        public DiskLruCacheEditorDestination(DiskLruCache.Editor editor) throws IOException {
            super(editor.newOutputStream(0));
            this.editor = editor;
        }

        public DiskLruCache.Editor getEditor() {
            return editor;
        }
    }
}
