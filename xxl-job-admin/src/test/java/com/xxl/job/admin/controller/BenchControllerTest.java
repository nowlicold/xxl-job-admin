package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.bench.request.BenchXxlJobGroupInitRequest;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import org.junit.Test;

/**
 * @className BenchControllerTest
 * @autor cold
 * @DATE 2021/6/29 19:56
 **/

public class BenchControllerTest {
    @Test
    public void testAddJobGroup(){
        BenchXxlJobGroupInitRequest request = new BenchXxlJobGroupInitRequest();
        request.setAppName("test1");
        request.setTitle("test1");
       ReturnT<String> stringReturnT =  XxlJobRemotingUtil.postBody("http://127.0.0.1:9003/xxl-job-admin/bench/jobgroup/init",null,3,request,String.class);
        System.out.println(stringReturnT);
    }
}
