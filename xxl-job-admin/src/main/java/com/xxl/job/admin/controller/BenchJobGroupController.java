package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.bench.request.BenchXxlJobGroupInitRequest;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

/**
 * bench对执行器的扩展api，用来配合分布式task的自注册
 * 
 * @className BenchJobGroupController
 * @autor cold
 * @DATE 2021/6/29 0:32
 **/
@Controller
@RequestMapping("/bench/jobgroup")
public class BenchJobGroupController {
	private static Logger logger = LoggerFactory.getLogger(BenchJobGroupController.class);

	@Resource
	public XxlJobGroupDao xxlJobGroupDao;

	/**
	 * 创建初始化XxlJobGropu
	 * @param request
	 * @return
	 */
	@RequestMapping("/init")
	@ResponseBody
	@PermissionLimit(limit = false) //不进行拦截
	public ReturnT<String> init(@RequestBody BenchXxlJobGroupInitRequest request) {
		// va'lid
		if (request.getAppName() == null || request.getAppName().trim().length() == 0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
		}
		if (request.getAppName().length() < 4 || request.getAppName().length() > 64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_length"));
		}
		if (request.getAppName().contains(">") || request.getAppName().contains("<")) {
			return new ReturnT<String>(500, "AppName" + I18nUtil.getString("system_unvalid"));
		}
		if (request.getTitle() == null || request.getTitle().trim().length() == 0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
		}
		if (request.getTitle().contains(">") || request.getTitle().contains("<")) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_unvalid"));
		}

		// 一个应用一个执行器，存在则更新，不存在则插入
		XxlJobGroup oldXxlJobGroup = xxlJobGroupDao.benchFindByAppName(request.getAppName());
		// 如果不为空则直接返回
		if (oldXxlJobGroup != null) {
			return ReturnT.SUCCESS;
		}
		XxlJobGroup createXxlJobGroup = new XxlJobGroup();
		createXxlJobGroup.setAppname(request.getAppName());
		createXxlJobGroup.setTitle(request.getTitle());
		//默认都为自动注册
		createXxlJobGroup.setAddressType(0);
		createXxlJobGroup.setUpdateTime(new Date());
		// 如果存在则更新
		// process
		// 首先看是否存在
		int ret = xxlJobGroupDao.save(createXxlJobGroup);
		return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;

	}

}
