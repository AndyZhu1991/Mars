package com.koolew.mars.utils;

import android.content.Context;

import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import cn.jiajixin.nuwa.Nuwa;

/**
 * Created by jinchangzhu on 12/21/15.
 */
public class PatchUtil {

    private static int lastPatchCode;
    private static int lastAppCode;

    private static int loadedPatchCode;

    public static void tryToLoadPatch(Context context) {
        File internalDir = getInternalPatchDir(context);
        File[] files = internalDir.listFiles();
        if (files != null && files.length > 0) {
            // 这个目录下应该只有一个jar文件
            File patchFile = files[0];
            if (getAppVersionCodeByPatchName(patchFile.getName()) == Utils.getCurrentVersionCode(context)) {
                Nuwa.loadPatch(context, patchFile.getAbsolutePath());
                loadedPatchCode = getPatchVersionCodeByPatchName(patchFile.getName());
            }
        }
    }

    public static boolean hasNewPatch() {
        return loadedPatchCode < lastPatchCode;
    }

    public static void checkAndUpdatePatchAsync(final Context context) {
        new Thread() {
            @Override
            public void run() {
                try {
                    checkAndUpdatePatch(context);
                }
                catch (Exception e) {
                    // Ignore it
                }
            }
        }.start();
    }

    public static void checkAndUpdatePatch(Context context) {
        // 同步拉取最新patch信息
        JSONObject response;
        try {
            response = ApiWorker.getInstance().standardGetRequestSync(UrlHelper.CHECK_PATCH_URL);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return;
        }

        // 获取详细patch信息
        String patchUrl;
        String patchMd5;
        try {
            int code = response.getInt("code");
            if (code != 0) {
                return;
            }
            JSONObject result = response.getJSONObject("result");
            patchUrl = result.getString("url");
            lastPatchCode = result.getInt("patch_code");
            lastAppCode = result.getInt("apk_code");
            patchMd5 = result.getString("md5sum");
        } catch (JSONException e) {
            return;
        }

        if (lastAppCode != Utils.getCurrentVersionCode(context)) {
            return;
        }

        final File finalPatch = new File(getInternalPatchDir(context),
                getPatchNameByAppVersionAndPatchVersion(lastAppCode, lastPatchCode));
        try {
            // 如果patch以存在
            if (finalPatch.exists()) {
                // 检查md5
                if (FileUtil.getFileMD5String(finalPatch).equals(patchMd5)) {
                    return;
                }
                else {
                    finalPatch.delete();
                }
            }
            else { // patch不存在
                FileUtil.deleteFilesFromDir(getInternalPatchDir(context), new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        // 删除所有文件
                        return pathname.isFile();
                    }
                });
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            return;
        }

        // 下载至外部存储器
        if (!downloadPatch(patchUrl, new File(getExternalTempPath(context)))) {
            return;
        }

        // copy到内部存储器
        FileUtil.copyFile(getExternalTempPath(context), getInternalTempPath(context));
        new File(getExternalTempPath(context)).delete();

        // 检查文件的md5
        String internalTempJarMd5;
        try {
            internalTempJarMd5 = FileUtil.getFileMD5String(new File(getInternalTempPath(context)));
        } catch (IOException | NoSuchAlgorithmException e) {
            return;
        }
        if (!internalTempJarMd5.equals(patchMd5)) {
            new File(getInternalTempPath(context)).delete();
            return;
        }

        // 重命名为最终patch
        new File(getInternalTempPath(context)).renameTo(finalPatch);
    }

    /**
     *
     * @param urlString
     * @param localFile
     * @return isSuccessed
     */
    private static boolean downloadPatch(String urlString, File localFile) {
        if(localFile.exists()) {
            localFile.delete();
        }
        try {
            // 构造URL
            URL url = new URL(urlString);
            // 打开连接
            URLConnection con = url.openConnection();
            // 输入流
            InputStream is = con.getInputStream();
            // 4K的数据缓冲
            byte[] bs = new byte[4096];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            OutputStream os = new FileOutputStream(localFile);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            // 完毕，关闭所有链接
            os.close();
            is.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getExternalTempPath(Context context) {
        return Utils.getCacheDir(context) + "/temp.jar";
    }

    private static File getInternalPatchDir(Context context) {
        return context.getDir("patch", Context.MODE_PRIVATE);
    }

    private static String getInternalTempPath(Context context) {
        return getInternalPatchDir(context).getAbsolutePath() + "/temp.jar";
    }

    private static String getPatchNameByAppVersionAndPatchVersion(int appVersion, int patchVersion) {
        return appVersion + "-" + patchVersion + ".jar";
    }

    private static int getAppVersionCodeByPatchName(String patchName) {
        try {
            return Integer.valueOf(patchName.substring(0, patchName.indexOf('-')));
        }
        catch (Exception e) {
            return 0;
        }
    }

    private static int getPatchVersionCodeByPatchName(String patchName) {
        try {
            return Integer.valueOf(patchName.substring(patchName.indexOf('-') + 1,
                    patchName.indexOf('.')));
        }
        catch (Exception e) {
            return 0;
        }
    }
}
