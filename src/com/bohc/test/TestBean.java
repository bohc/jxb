package com.bohc.test;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.bohc.bean.BaseIni;
import com.bohc.sh.dao.FlyTicketDao;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.sh.entities.Tarea;
import com.bohc.sh.service.QlyFlyticketService;
import com.bohc.sh.service.TareaService;
import com.bohc.util.FileManger;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TestBean {

	private ApplicationContext ctx = null;
	private QlyFlyticketService qlyflyticketservice;
	private TareaService tareaService;
	// private NamedParameterJdbcTemplate namedParameterJdbcTemplate = null;

	{
		ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		// qlyflyticketservice = (QlyFlyticketService)
		// ctx.getBean("qlyflyticketservice");
		tareaService = (TareaService) ctx.getBean("tareaService");
		// namedParameterJdbcTemplate = (NamedParameterJdbcTemplate)
		// ctx.getBean("namedParameterJdbcTemplate");
	}

	@Test
	public void testSql() throws Exception {
		String sql = "insert into flyticket(fltno,aireline,startairport,arriveairport,starttime,arrivetime,ticketprice,fetchdate,opttime) values(:fltno,:aireline,:startaireport,:arriceaireport,:starttime,:arrivetime,:ticketprice,:fetchdate,:opttime)";
		QlyFlyticket ft = new QlyFlyticket();
		ft.setFltno("65447");
		ft.setAireline("东方航空");
		ft.setStartairport("北就机场");
		ft.setArriveairport("昆明机场");
		ft.setStarttime(new Date());
		ft.setArrivetime(new Date());
		ft.setFetchdate(new Date());
		ft.setOpttime(new Date());
		ft.setTicketprice(1890);
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ft);
		// namedParameterJdbcTemplate.update(sql, paramSource);
	}

	@Test
	public void testDataSource() throws SQLException {
		DataSource dataSource = (DataSource) ctx.getBean("dataSource");
		System.out.println(dataSource.getConnection());
	}

	@Test
	public void testFlyTicketDao() throws SQLException {
		FlyTicketDao flyTicketDao = (FlyTicketDao) ctx.getBean("flyTicketDao");
		System.out.println(flyTicketDao);
	}

	@Test
	public void testQlyFlyticketService() {
		QlyFlyticket ft = new QlyFlyticket();
		ft.setFltno("65447");
		ft.setAireline("东方航空");
		ft.setStartairport("北就机场");
		ft.setArriveairport("昆明机场");
		ft.setStarttime(new Date());
		ft.setArrivetime(new Date());
		ft.setFetchdate(new Date());
		ft.setOpttime(new Date());
		ft.setTicketprice(1890);
		System.out.println(qlyflyticketservice);
		qlyflyticketservice.save(ft);
	}

	@Test
	public void testGetArea() {
		Tarea tarea = new Tarea();
		tarea.setPid("86000000");
		List<Tarea> tlist = tareaService.list(tarea);
		for (Tarea ta : tlist) {
			System.out.println(ta);
		}
	}

	@Test
	public void listFile() {
		File f = new File("D:/wb/cwb/q/qunar_lib");
		File[] files = f.listFiles();
		for (File file : files) {
			System.out.print("qunar_lib/" + file.getName() + ";");
		}
	}

	@Test
	public void updateArea() {
		StringBuffer sb = new StringBuffer();
		String str = FileManger.readFile("d:/aliyArea.txt", "UTF-8");
		try {
			JSONObject json = JSONObject.fromObject(str);
			JSONArray jsons = json.getJSONArray("results");
			for (int i = 0; i < jsons.size(); i++) {
				JSONObject jo = jsons.getJSONObject(i);
				JSONArray jsons2 = jo.getJSONArray("tabdata");
				for (int j = 0; j < jsons2.size(); j++) {
					JSONObject jo2 = jsons2.getJSONObject(j);
					JSONArray jsons3 = jo2.getJSONArray("dd");
					for (int f = 0; f < jsons3.size(); f++) {
						JSONObject jo3 = jsons3.getJSONObject(f);
						sb.append("update tarea set ircode=");
						sb.append("\"").append(jo3.getString("cityCode")).append("\"");
						sb.append(" where area = \"").append(jo3.getString("cityName")).append("\";\r\n");
					}
				}
			}
			System.out.println(sb);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main1(String[] args) {
		String str = "起飞时段：21:05 ~ 21:50";
		Pattern p = Pattern.compile("[0-9]?[0-9]:[0-9]?[0-9]");

		Matcher match = p.matcher(str);
		if (match.find()) {
			System.out.println(match.group());
		}
	}

	@Test
	public void testRandom() {
		int curfetchnum = 3;// 默认从3条开始
		while (true) {
			// 判断是否是第一个，如果不是，那么给出随机间隔，如是不是第一个，那么判断，添加合理的抓取地址
			if (curfetchnum == 0) {
				int max = 8;
				int min = 3;
				Random random = new Random();
				curfetchnum = random.nextInt(max) % (max - min + 1) + min;
				System.out.println("当前生成的随机数是："+curfetchnum);
			} else {
				curfetchnum--;
				if (curfetchnum < 0) {
					curfetchnum = 0;
				}
			}
			System.out.println("当前数是："+curfetchnum);
		}
	}
	
	/**
	 * 取字串中的数字
	 * 
	 * @param str
	 * @return
	 */
	public static String fetchNumberFromString(String str) {
		String regEx = "([^0-9][\\d+])";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}
	
	public static void main(String[] args){
		System.out.println(fetchNumberFromString("1.02克"));
	}
}
