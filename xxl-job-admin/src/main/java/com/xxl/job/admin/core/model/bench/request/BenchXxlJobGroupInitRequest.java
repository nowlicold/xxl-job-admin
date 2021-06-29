package com.xxl.job.admin.core.model.bench.request;

import java.util.Date;

/**
 * @className BenchXxlJobGroup
 * @autor cold
 * @DATE 2021/6/29 12:19
 **/

public class BenchXxlJobGroupInitRequest {
    private String appName;
    private String title;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
