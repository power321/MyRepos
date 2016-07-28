package com.shaman.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.web.service.activity.doctoronline.DoctorOnlineService;
import com.web.utils.SpringContextUtil;

@Service
public class ActivityNotifyJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap jobMap = jobDetail.getJobDataMap();

		System.out.println(jobDetail.getKey());
		System.out.println("notify: " + jobMap.get("notify"));
		System.out.println("id: " + jobMap.get("id"));
		System.out.println("time: " + jobMap.get("time"));
		System.out.println("url: " + jobMap.get("url"));


		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
		String time = null;
		if(jobMap.get("time") != null)
			time = sdf.format((Date)jobMap.get("time"));
		//System.out.println("After format: " + time);
		ApplicationContext springContext =null;
		springContext =SpringContextUtil.applicationContext;
		System.out.println("springContext:"+springContext);
		DoctorOnlineService doctorOnlineService = (DoctorOnlineService) springContext.getBean("doctorOnlineService");
		doctorOnlineService.push((String)jobMap.get("id"), (String)jobMap.get("notify"), time, (String)jobMap.get("url"));
	}
}