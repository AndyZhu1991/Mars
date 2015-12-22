package com.koolew.mars.remoteconfig;

import android.content.SharedPreferences;

import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by jinchangzhu on 12/16/15.
 */
abstract class BaseRemoteConfigItem<T> {
    private RemoteConfigManager mManager;
    private T mConfig;

    public BaseRemoteConfigItem(RemoteConfigManager manager) {
        mManager = manager;
        mConfig = readFromSp();
    }

    public void tryFetch(boolean forceFetch) {
        if (forceFetch || !isValidate(mConfig)) {
            fetchConfig();
        }
    }

    public T getConfig() {
        return mConfig;
    }

    T readFromSp() {
        return readFromSp(mManager.getSp());
    }

    void fetchConfig() {
        try {
            JSONObject response = ApiWorker.getInstance().standardGetRequestSync(fetchUrl());
            int code = response.getInt("code");
            if (code == 0) {
                JSONObject result = response.getJSONObject("result");
                T config = fromResult(result);
                if (!isEquals(config, mConfig)) {
                    mConfig = config;
                    saveToSp(mConfig);
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    String defaultKey() {
        return getClass().getSimpleName();
    }

    abstract String fetchUrl();

    abstract T fromResult(JSONObject result);

    protected void saveToSp(T config) {
        SharedPreferences sp = mManager.getSp();
        SharedPreferences.Editor editor = sp.edit();
        saveToEditor(editor, config);
        editor.commit();
    }

    abstract void saveToEditor(SharedPreferences.Editor editor, T config);

    abstract T readFromSp(SharedPreferences sp);

    boolean isValidate(T config) {
        return config != null;
    }

    boolean isEquals(T config0, T config1) {
        return config0.equals(config1);
    }
}
