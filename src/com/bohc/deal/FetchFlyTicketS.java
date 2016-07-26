package com.bohc.deal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.beanutils.BeanUtils;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchAirLine;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.sh.entities.Tarea;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.demo.Tab;
import com.teamdev.jxbrowser.chromium.demo.TabFactory;
import com.teamdev.jxbrowser.chromium.demo.TabbedPane;

public class FetchFlyTicketS extends Thread {

	String gurl = "http://flight.qunar.com";
	TabbedPane bt;
	Browser browser;
	List<FetchAirLine> list = null;
	List<Tarea> fromcity = null;
	List<Tarea> tocity = null;
	boolean loadover = false;
	QlyFlyticket qt;

	public FetchFlyTicketS(TabbedPane bt) {
		super();
		this.bt = bt;
	}

	// 初始化航线堆栈
	/**
	 * @wbp.parser.entryPoint
	 */
	private void initStack() {
		BaseIni.satckAirline.clear();
		if (list != null && list.size() > 0) {
			for (int i = list.size() - 1; i > 0; i--) {
				FetchAirLine fal = list.get(i);
				BaseIni.satckAirline.push(fal);
			}
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void initCity() {
		list = new ArrayList<FetchAirLine>();
		Calendar c = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		fromcity = BaseIni.fetchCitys.getFromcitys();
		tocity = BaseIni.fetchCitys.getTocitys();

		int fetchday = 0;
		try {
			fetchday = (int) ((BaseIni.fetchCitys.getEnddate().getTime() - BaseIni.fetchCitys.getStartdate().getTime()) / (1000 * 60 * 60 * 24));
		} catch (Exception e) {
			fetchday = 0;
		}
		for (Tarea fcity : fromcity) {
			if (fcity == null || fcity.getArea().trim().equals("") || fcity.getArea().trim().equalsIgnoreCase("null")) {
				continue;
			}
			for (Tarea tcity : tocity) {
				if (tcity == null || tcity.getArea().trim().equals("") || fcity.getArea().trim().equalsIgnoreCase("null")) {
					continue;
				}
				c.setTime(BaseIni.fetchCitys.getStartdate());

				for (int i = 0; i < fetchday; i++) {
					Date fdate = c.getTime();
					c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
					Date tdate = c.getTime();

					FetchAirLine fetchAirLine = new FetchAirLine();
					fetchAirLine.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + fcity.getArea().trim() + "&toCity=" + tcity.getArea().trim() + "&fromDate="
							+ df.format(fdate) + "&toDate=" + df.format(tdate));
					fetchAirLine.setAliyurl("https://sjipiao.alitrip.com/homeow/trip_flight_search.htm?searchBy=1280&tripType=0&depCityName=" + fcity.getArea().trim() + "&depCity=" + fcity.getIrcode() + "&arrCityName="
							+ tcity.getArea().trim() + "&arrCity=" + tcity.getIrcode() + "&depDate=" + df.format(fdate) + "&arrDate=");
					fetchAirLine.setFlydate(fdate);
					fetchAirLine.setFromcity(fcity.getArea());
					fetchAirLine.setFromcityjm(fcity.getAcode());
					fetchAirLine.setTocity(tcity.getArea());
					fetchAirLine.setTocityjm(tcity.getAcode());
					list.add(fetchAirLine);

					FetchAirLine fetchAirLineb = null;
					try {
						fetchAirLineb = (FetchAirLine) BeanUtils.cloneBean(fetchAirLine);
						fetchAirLineb.setFromcity(tcity.getArea());
						fetchAirLineb.setFromcityjm(tcity.getAcode());
						fetchAirLineb.setTocity(fcity.getArea());
						fetchAirLineb.setTocityjm(fcity.getAcode());
						fetchAirLineb.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + tcity.getArea().trim() + "&toCity=" + fcity.getArea().trim() + "&fromDate="
								+ df.format(fdate) + "&toDate=" + df.format(tdate));
						fetchAirLineb.setAliyurl("https://sjipiao.alitrip.com/homeow/trip_flight_search.htm?searchBy=1280&tripType=0&depCityName=" + fcity.getArea().trim() + "&depCity=" + fcity.getIrcode()
								+ "&arrCityName=" + tcity.getArea().trim() + "&arrCity=" + tcity.getIrcode() + "&depDate=" + df.format(fdate) + "&arrDate=");
						list.add(fetchAirLineb);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (i == (fetchday - 1)) {
						int bnum = BaseIni.fetchCitys.getOverupnum();
						for (int b = 0; b < bnum; b++) {
							if (fetchAirLineb == null)
								break;
							fdate = c.getTime();
							c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
							tdate = c.getTime();
							try {
								FetchAirLine fetchAirLinebo = (FetchAirLine) BeanUtils.cloneBean(fetchAirLineb);
								fetchAirLinebo.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + tcity.getArea().trim() + "&toCity=" + fcity.getArea().trim()
										+ "&fromDate=" + df.format(fdate) + "&toDate=" + df.format(tdate));
								fetchAirLinebo.setAliyurl("https://sjipiao.alitrip.com/homeow/trip_flight_search.htm?searchBy=1280&tripType=0&depCityName=" + fcity.getArea().trim() + "&depCity=" + fcity.getIrcode()
										+ "&arrCityName=" + tcity.getArea().trim() + "&arrCity=" + tcity.getIrcode() + "&depDate=" + df.format(fdate) + "&arrDate=");
								fetchAirLinebo.setFlydate(fdate);
								list.add(fetchAirLinebo);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void fetchTicket() {
		if (BaseIni.fetchCitys.isCk_fetch_qunar()) {
			BaseIni.fetchCitys.setCururl(1);
		} else {
			BaseIni.fetchCitys.setCururl(2);
		}

		if (BaseIni.fetchCitys.isCk_fetch_qunar() || BaseIni.fetchCitys.isCk_fetch_aliy()) {
			int intervaltime = BaseIni.fetchCitys.getIntervaltime();
			int i = 0;
			long oldtime = System.currentTimeMillis();
			int curfetchnum = 3;// 默认从3条开始
			while (true) {
				// 判断是否是第一个，如果不是，那么给出随机间隔，如是不是第一个，那么判断，添加合理的抓取地址
				if (BaseIni.fetchCitys.isCk_fetch_qunar() && BaseIni.fetchCitys.isCk_fetch_aliy()) {
					if (curfetchnum == 0) {
						int max = BaseIni.fetchCitys.getFlagfetchrandmax();
						int min = BaseIni.fetchCitys.getFlagfetchrandmin();
						Random random = new Random();
						curfetchnum = random.nextInt(max) % (max - min + 1) + min;
						JxBrowserDemo.jd.updateUi("当前间隔 " + curfetchnum + " 条");
						if (BaseIni.fetchCitys.getCururl() == 2) {
							BaseIni.fetchCitys.setCururl(1);
						} else {
							BaseIni.fetchCitys.setCururl(2);
						}
					} else {
						curfetchnum--;
						if (curfetchnum < 0) {
							curfetchnum = 0;
						}
					}
				}

				FetchAirLine fetchaireline = BaseIni.fetchAirLine;
				if (BaseIni.satckAirline.isEmpty()) {
					CheckHtmlIsLoad.instance().saveData(i, fetchaireline);
					JxBrowserDemo.jd.updateUi("共用时：" + (System.currentTimeMillis() - oldtime) / 1000 + "秒");
					JxBrowserDemo.jd.updateUi("没有可抓取的数据了");
					break;
				}
				BaseIni.fetchAirLine = BaseIni.satckAirline.pop();

				// 只有第一层的整条数据抓取完成，才保存上一条数据,线路完成的标识是 level 为 0.
				if (fetchaireline != null && BaseIni.fetchAirLine.getLevel() == 0) {
					CheckHtmlIsLoad.instance().saveData(i, fetchaireline);
					JxBrowserDemo.jd.updateUi("共用时：" + (System.currentTimeMillis() - oldtime) / 1000 + "秒");
					oldtime = System.currentTimeMillis();
				}

				// 上一个数据取完后，根据界面的设定，间隔一段时间再来取数据
				try {
					if (i > 0)
						Thread.sleep(1000 * intervaltime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 判断任务有没有人为的终止，如果终止那么终止执行
				if (!BaseIni.fetchstatus) {
					break;
				}

				bt.disposeAllTabs();
				Tab tct = TabFactory.createTab("about:blanck");
				bt.addTab(tct);
				bt.selectTab(tct);
				browser = bt.tabs.get(1).getContent().browserView.getBrowser();
				if (BaseIni.fetchCitys.getCururl() == 1) {
					browser.loadURL(BaseIni.fetchAirLine.getUrl());
				} else if (BaseIni.fetchCitys.getCururl() == 2) {
					browser.loadURL(BaseIni.fetchAirLine.getAliyurl());
				}
				i++;

				// 如果是抓取一条新的数据，那么清空上一次取的数据，清空的标识就是现在取出来的线路是第一层的线路 level ＝ 0
				if (BaseIni.fetchAirLine.getLevel() == 0) {
					BaseIni.rlist.clear();
					BaseIni.flist.clear();
				}
				// 对抓取的页面做时间判断，如果超时，那么自动跳到下一个,当前先注释了
				searchFlight();
				
				// 判断当前有没有数据在取，如果有大于１的任务，那么等待
				while (BaseIni.browserListenerCount > 0) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
				}
			}
		} else {
			JxBrowserDemo.jd.updateUi("<span color=\"red\">没有选择抓取地址。</span>");
		}
		BaseIni.changeCount(false);
		JxBrowserDemo.jd.updateBtn(1, null);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void searchFlight() {
		BaseIni.changeCount(true);

		// 对抓取的页面做一个进程守护，如果 30 秒后还没有完成，那么跳到下一个
		new Thread(new Runnable() {
			long oldtime = System.currentTimeMillis();

			@Override
			public void run() {
				while (true) {
					if (BaseIni.fetchAirLine.isFetching()) {
						break;
					}
					if ((System.currentTimeMillis() - oldtime) / 1000 >= (60*10)) {
						BaseIni.changeCount(false);
						break;
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void run() {
		super.run();
		if (BaseIni.fetchCitys.isCkbcg()) {// 这儿是抓取不成功的
			list = BaseIni.fetchFailList;
			BaseIni.fetchFailList.clear();
		} else {
			initCity();
		}
		// writerList();
		initStack();
		fetchTicket();
		BaseIni.fetchstatus = false;
		JxBrowserDemo.jd.updateBtn(1, "抓取");
	}

	private void writerList() {
		if (list != null) {
			for (FetchAirLine fal : list) {
				System.out.println(fal.getAliyurl());
			}
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {
		FetchFlyTicketS fft = new FetchFlyTicketS(null);
		fft.initCity();
	}
}
