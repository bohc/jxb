package com.bohc.bean.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bohc.sh.service.QlyFlyticketService;
import com.bohc.sh.service.TareaService;

public class QlyFlyTicketAction {
	private static ApplicationContext ctx = null;
	private static QlyFlyticketService qlyflyticketservice = null;
	private static QlyFlyTicketAction qlyflyticketaction;
	static {
		if (ctx == null) {
			ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		}
		if (qlyflyticketaction == null) {
			qlyflyticketaction = new QlyFlyTicketAction();
		}
		qlyflyticketservice = (QlyFlyticketService) ctx.getBean("qlyFlyticketService");
	}

	public static QlyFlyTicketAction instance() {
		return qlyflyticketaction;
	}

	public void insertTickets(String cond) {
		qlyflyticketservice.save(cond);
	}

	public void deleteBeforeYesterday() {
		qlyflyticketservice.deleteBeforeYesterday();
	}

	public TareaService getTareaservice() {
		return (TareaService) ctx.getBean("tareaService");
	}

	public String combineString(String str) {
		if (str == null) {
			return null;
		}
		return "'" + str.trim() + "'";
	}

	public String combineDate(Date date) {
		if (date == null) {
			return null;
		}
		return "'" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)) + "'";
	}
}
