package com.game.sdk.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lucky on 2018/2/28.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WebHandler {
    public String url();
    public String description();
}
