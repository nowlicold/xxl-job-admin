package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Controller;
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


	@RequestMapping("/save")
	@ResponseBody
	@PermissionLimit(limit = false) //不进行拦截
	public ReturnT<String> save(XxlJobGroup xxlJobGroup) {

		// va'lid
		if (xxlJobGroup.getAppname() == null || xxlJobGroup.getAppname().trim().length() == 0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
		}
		if (xxlJobGroup.getAppname().length() < 4 || xxlJobGroup.getAppname().length() > 64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_length"));
		}
		if (xxlJobGroup.getAppname().contains(">") || xxlJobGroup.getAppname().contains("<")) {
			return new ReturnT<String>(500, "AppName" + I18nUtil.getString("system_unvalid"));
		}
		if (xxlJobGroup.getTitle() == null || xxlJobGroup.getTitle().trim().length() == 0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
		}
		if (xxlJobGroup.getTitle().contains(">") || xxlJobGroup.getTitle().contains("<")) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_unvalid"));
		}
		if (xxlJobGroup.getAddressType() != 0) {
			if (xxlJobGroup.getAddressList() == null || xxlJobGroup.getAddressList().trim().length() == 0) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
			}
			if (xxlJobGroup.getAddressList().contains(">") || xxlJobGroup.getAddressList().contains("<")) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList") + I18nUtil.getString("system_unvalid"));
			}

			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item : addresss) {
				if (item == null || item.trim().length() == 0) {
					return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
				}
			}
		}
		// 一个应用一个执行器，存在则更新，不存在则插入
		XxlJobGroup oldXxlJobGroup = xxlJobGroupDao.benchFindByAppName(xxlJobGroup.getAppname());
		// 如果不为空则直接返回
		if (oldXxlJobGroup != null) {
			return ReturnT.SUCCESS;
		}

		// 如果存在则更新
		// process
		xxlJobGroup.setUpdateTime(new Date());
		// 首先看是否存在
		int ret = xxlJobGroupDao.save(xxlJobGroup);
		return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;

	}

}
