package com.unbank.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.unbank.uniq.Similarity;
import com.unbank.utl.publicTools;

public class TimerListener implements ServletContextListener {

	private Timer timer;
	// 日志
	public static Logger logger = Logger.getLogger(TimerListener.class);
	public void contextDestroyed(ServletContextEvent servletcontextevent) {
		if(timer!=null)
		timer.cancel();
		servletcontextevent.getServletContext().log("定时器销毁~~~");
		System.out.println("定时任务结束~~");
	}

	public void contextInitialized(ServletContextEvent servletcontextevent) {
		logger.info("--------------------定时任务开始--------------------");
		try{
			TimerRun.operflag=true;
			TimerRun timerRun=new TimerRun();
			//加载系统参数
			timerRun.loadparameters();
			//加载分类主表
			timerRun.loadclassinfor();
			//加载关键词词典表
			timerRun.loadkeyworddictionary();
			//初始化数据
			timerRun.initdata();
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			TimerRun.operflag=false;
		}
		//去重
		try{
			TimerRun.operuniqflag=true;
			Similarity.loaddata();
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			TimerRun.operuniqflag=false;
		}
		
		timer = new Timer(true);
		Date date = new Date();
		date.setHours(1);
		date.setMinutes(0);
		date.setSeconds(0);
		
		long start = date.getTime() + 86400000 - System.currentTimeMillis();
		
		//定时执行分类数据
		timer.schedule(new TimerRun(servletcontextevent.getServletContext()), start, 86400000);
		
		//定时执行判重
		timer.schedule(new TimerRunUniq(servletcontextevent.getServletContext()), 10000, 300000);
	}

}