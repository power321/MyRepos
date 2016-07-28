package com.shaman.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.terracotta.quartz.wrappers.TriggerFacade;

import com.web.dao.device.DeviceDao;
import com.web.dao.device.WxSessionDao;
import com.web.utils.LogUtils;



@Service
@Scope("singleton")
public class MyScheduler {
	Logger logger = LogUtils.getLogger();
	@Resource
	DeviceDao deviceDao;
	@Resource
	WxSessionDao wxSessionDao;
	
	private StdSchedulerFactory sf = null;
	private Properties props = null;
	private Scheduler scheduler = null;
	
	// 创建、初始化Scheduler实例并启动处理主循环
	public void initScheduler() {
		// 创建scheduler对象, 并配置JobDetail和Trigger对象
		sf = new StdSchedulerFactory();
		
		// 设置线程池属�?
		try {
			props = new Properties();
			props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
			props.put("org.quartz.threadPool.threadCount", "10");
			sf.initialize(props);
		} catch (SchedulerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// 获取shceduler实例并启�?
		try {
			scheduler = sf.getDefaultScheduler();
			// 执行启动、关闭等操作
			scheduler.start();
		} catch (SchedulerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public StdSchedulerFactory getSf() {
		return sf;
	}

	public void setSf(StdSchedulerFactory sf) {
		this.sf = sf;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void addActivityNotify() throws SchedulerException {
		
		String sql = "SELECT id,start_time,live_url,notify_yes_time,notify_to_time FROM db_tangdaifu_activity.t_activity_doctor_info " +
				"WHERE start_time>NOW()";
		List<Map<String, Object>> listActivity = deviceDao.execResultMap(sql);
		
		for (Map<String, Object> data : listActivity) {
			System.out.println(data.get("id") + " " + data.get("start_time"));
			Date startTime = (Date)data.get("start_time");
			Date yestoday = (Date)data.get("notify_yes_time");
			Date today = (Date)data.get("notify_to_time");

			Date now = new Date();
			System.out.println("Now date: " + now);
			System.err.println(data);
			// 发布提前�?��的推送任�?
			if(now.before(yestoday)) {
				System.out.println("yestoday: " + yestoday);
				System.out.println("Before One Day!");
				JobDetail jobDetail = JobBuilder.newJob(ActivityNotifyJob.class)
					.withIdentity((String)data.get("id")+"yestoday", "DoctorOnline")
					.build();
				JobDataMap jobMap = jobDetail.getJobDataMap();
				jobMap.put("notify", "before");
				jobMap.put("id", data.get("id"));
				jobMap.put("time", data.get("start_time"));
				Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity((String)data.get("id")+"yestoday", "DoctorOnline")
					//.startAt(startCalendar.getTime())
					.startNow()
					.build();
				scheduler.scheduleJob(jobDetail, trigger);
			}
			
			// 发布提前15min的推送任�?
			if (now.before(today)) {
				System.out.println("today: " + today);
				System.out.println("Beafore 15 Min!");
				JobDetail jobDetail = JobBuilder.newJob(ActivityNotifyJob.class)
					.withIdentity((String)data.get("id")+"today", "DoctorOnline")
					.build();
				JobDataMap jobMap = jobDetail.getJobDataMap();
				jobMap.put("notify", "today");
				jobMap.put("id", data.get("id"));
				jobMap.put("url", data.get("live_url"));
				Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity((String)data.get("id")+"today", "DoctorOnline")
					//.startAt(startCalendar.getTime())
					.startNow()
					.build();
				/*
				 * 调试环境
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				scheduler.scheduleJob(jobDetail, trigger);
			}
		}
		
		
		//* JUnit测试环境
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
