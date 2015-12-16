package com.koolew.mars.remoteconfig;

import com.koolew.mars.webapi.UrlHelper;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class MovieTagsConfig extends VideoTagsConfig {

    public MovieTagsConfig(RemoteConfigManager manager) {
        super(manager);
    }

    @Override
    String fetchUrl() {
        return UrlHelper.MOVIE_TAG_URL;
    }
}
