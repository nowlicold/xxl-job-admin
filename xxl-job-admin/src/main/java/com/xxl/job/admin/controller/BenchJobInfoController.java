package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.cron.CronExpression;
import com.xxl.job.admin.core.model.BenchXxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.core.scheduler.MisfireStrategyEnum;
import com.xxl.job.admin.core.scheduler.ScheduleTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @className BenchJobInfoController
 * @autor cold
 * @DATE 2021/6/29 1:27
 **/
@Controller
@RequestMapping("/bench/jobInfo")
public class BenchJobInfoController {
    private static Logger logger = LoggerFactory.getLogger(BenchJobInfoController.class);
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;

    @Autowired
    private XxlJobService xxlJobService;

    private BeanCopier convert = BeanCopier.create(BenchXxlJobInfo.class,XxlJobInfo.class,false);
    @RequestMapping("/init")
    @ResponseBody
    @PermissionLimit(limit = false) //不进行拦截
    public ReturnT<String> init(@RequestBody BenchXxlJobInfo benchXxlJobInfo){

        XxlJobGroup group = xxlJobGroupDao.benchFindByAppName(benchXxlJobInfo.getJobGroupAppName());
        if (group == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
        }
        if (benchXxlJobInfo.getJobDesc()==null || benchXxlJobInfo.getJobDesc().trim().length()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
        }
        if (benchXxlJobInfo.getAuthor()==null || benchXxlJobInfo.getAuthor().trim().length()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
        }

        // valid trigger
        ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(benchXxlJobInfo.getScheduleType(), null);
        if (scheduleTypeEnum == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
        }
        if (scheduleTypeEnum == ScheduleTypeEnum.CRON) {
            if (benchXxlJobInfo.getScheduleConf()==null || !CronExpression.isValidExpression(benchXxlJobInfo.getScheduleConf())) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "Cron"+I18nUtil.getString("system_unvalid"));
            }
        } else if (scheduleTypeEnum == ScheduleTypeEnum.FIX_RATE/* || scheduleTypeEnum == ScheduleTypeEnum.FIX_DELAY*/) {
            if (benchXxlJobInfo.getScheduleConf() == null) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")) );
            }
            try {
                int fixSecond = Integer.valueOf(benchXxlJobInfo.getScheduleConf());
                if (fixSecond < 1) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
                }
            } catch (Exception e) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
            }
        }

        // valid job
        if (GlueTypeEnum.match(benchXxlJobInfo.getGlueType()) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_gluetype")+I18nUtil.getString("system_unvalid")) );
        }
        if (GlueTypeEnum.BEAN==GlueTypeEnum.match(benchXxlJobInfo.getGlueType()) && (benchXxlJobInfo.getExecutorHandler()==null || benchXxlJobInfo.getExecutorHandler().trim().length()==0) ) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+"JobHandler") );
        }
        // 》fix "\r" in shell
        if (GlueTypeEnum.GLUE_SHELL==GlueTypeEnum.match(benchXxlJobInfo.getGlueType()) && benchXxlJobInfo.getGlueSource()!=null) {
            benchXxlJobInfo.setGlueSource(benchXxlJobInfo.getGlueSource().replaceAll("\r", ""));
        }

        // valid advanced
        if (ExecutorRouteStrategyEnum.match(benchXxlJobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_unvalid")) );
        }
        if (MisfireStrategyEnum.match(benchXxlJobInfo.getMisfireStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("misfire_strategy")+I18nUtil.getString("system_unvalid")) );
        }
        if (ExecutorBlockStrategyEnum.match(benchXxlJobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_unvalid")) );
        }
        //如果任务已存在 则不处理
        XxlJobInfo oldXxlJobInfo = xxlJobInfoDao.benchFindByExecutorHandler(benchXxlJobInfo.getExecutorHandler());
        //如果已存在则不作处理
        if(oldXxlJobInfo != null){
            return ReturnT.SUCCESS;
        }
        // add in db
        benchXxlJobInfo.setAddTime(new Date());
        benchXxlJobInfo.setUpdateTime(new Date());
        benchXxlJobInfo.setGlueUpdatetime(new Date());
        //默认增加即运行
        benchXxlJobInfo.setTriggerStatus(1);
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        convert.copy(benchXxlJobInfo,xxlJobInfo,null);
        xxlJobInfo.setJobGroup(group.getId());

        xxlJobInfoDao.save(xxlJobInfo);
        if (xxlJobInfo.getId() < 1) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
        }
        //保存成功即执行
        return xxlJobService.start(xxlJobInfo.getId());

    }

}
