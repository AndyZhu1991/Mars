package com.koolew.mars.webapi;

/**
 * Created by jinchangzhu on 7/13/15.
 */
public class GetUrlBuilder {

    private StringBuilder builder;
    private boolean isFirstParameter;

    public GetUrlBuilder(String baseUrl) {
        builder = new StringBuilder(baseUrl);
        isFirstParameter = true;
    }

    public GetUrlBuilder addParameter(String name, Object value) {
        char paramPrefix;
        if (isFirstParameter) {
            isFirstParameter = false;
            paramPrefix = '?';
        }
        else {
            paramPrefix = '&';
        }

        builder.append(paramPrefix).append(name).append('=').append(value);
        return this;
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
