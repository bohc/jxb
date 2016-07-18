package com.bohc.deal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchAirLine;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.sh.entities.Tarea;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.demo.Tab;
import com.teamdev.jxbrowser.chromium.demo.TabFactory;
import com.teamdev.jxbrowser.chromium.demo.TabbedPane;

public class FetchFlyTicketTest extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	String gurl = "http://flight.qunar.com";
	TabbedPane bt;
	Browser browser;
	List<FetchAirLine> list = null;
	List<Tarea> fromcity = null;
	List<Tarea> tocity = null;
	boolean loadover = false;
	QlyFlyticket qt;

	public FetchFlyTicketTest(TabbedPane bt) {
		super();
		this.bt = bt;
	}

	// ��ʼ�����߶�ջ
	private void initStack() {
		BaseIni.satckAirline.clear();
		if (list != null && list.size() > 0) {
			for (int i = list.size(); i > 0; i--) {
				FetchAirLine fal = list.get(i - 1);
				BaseIni.satckAirline.push(fal);
			}
		}
	}

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
				// ����ǲ��ԣ�����ֻץȡһ��
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
					break;
				}
			}
		}
	}

	private void fetchTicket() {
		if(BaseIni.fetchCitys.isCk_fetch_qunar()){
			BaseIni.fetchCitys.setCururl(1);
		}else{
			BaseIni.fetchCitys.setCururl(2);
		}
		
		int intervaltime = BaseIni.fetchCitys.getIntervaltime();
		int i = 0;
		long oldtime = System.currentTimeMillis();
		while (true) {
			FetchAirLine fetchaireline = BaseIni.fetchAirLine;
			if (BaseIni.satckAirline.isEmpty()) {
				CheckHtmlIsLoad.instance().saveData(i, fetchaireline);
				JxBrowserDemo.jd.updateUi("����ʱ��" + (System.currentTimeMillis() - oldtime) / 1000 + "��");
				JxBrowserDemo.jd.updateUi("û�п�ץȡ��������");
				break;
			}
			BaseIni.fetchAirLine = BaseIni.satckAirline.pop();

			// ֻ�е�һ�����������ץȡ��ɣ��ű�����һ������,��·��ɵı�ʶ�� level Ϊ 0.
			if (fetchaireline != null && BaseIni.fetchAirLine.getLevel() == 0) {
				CheckHtmlIsLoad.instance().saveData(i, fetchaireline);
				JxBrowserDemo.jd.updateUi("����ʱ��" + (System.currentTimeMillis() - oldtime) / 1000 + "��");
				oldtime = System.currentTimeMillis();
			}

			// ��һ������ȡ��󣬸��ݽ�����趨�����һ��ʱ������ȡ����
			try {
				if (i > 0)
					Thread.sleep(1000 * intervaltime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// �ж�������û����Ϊ����ֹ�������ֹ��ô��ִֹ��
			if (!BaseIni.fetchstatus) {
				break;
			}

			bt.disposeAllTabs();
			Tab tct = TabFactory.createTab("about:blanck");
			bt.addTab(tct);
			bt.selectTab(tct);
			browser = bt.tabs.get(0).getContent().browserView.getBrowser();
			if (BaseIni.fetchCitys.getCururl() == 1) {
				browser.loadURL(BaseIni.fetchAirLine.getUrl());
			} else if (BaseIni.fetchCitys.getCururl() == 2) {
				browser.loadURL(BaseIni.fetchAirLine.getAliyurl());
			}
			i++;

			// �����ץȡһ���µ����ݣ���ô�����һ��ȡ�����ݣ���յı�ʶ��������ȡ��������·�ǵ�һ�����· level �� 0
			if (BaseIni.fetchAirLine.getLevel() == 0) {
				BaseIni.rlist.clear();
				BaseIni.flist.clear();
			}
			searchFlight();
			// �жϵ�ǰ��û��������ȡ������д��ڣ���������ô�ȴ�
			while (BaseIni.browserListenerCount > 0) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}

		}
		BaseIni.changeCount(false);
		JxBrowserDemo.jd.updateBtn(1, null);
	}

	public void searchFlight() {
		BaseIni.changeCount(true);

		// ��ץȡ��ҳ����һ�������ػ������ 30 ���û����ɣ���ô������һ��
		new Thread(new Runnable() {
			long oldtime = System.currentTimeMillis();

			@Override
			public void run() {
				while (true) {
					if (BaseIni.fetchAirLine.isFetching()) {
						break;
					}
					if ((System.currentTimeMillis() - oldtime) / 1000 >= 30) {
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

	@Override
	public void run() {
		super.run();
		initCity();
		initStack();
		fetchTicket();
		BaseIni.fetchstatus = false;
		JxBrowserDemo.jd.updateBtn(1, "ץȡ");
	}

	public static void main(String[] args) {
		FetchFlyTicketTest fft = new FetchFlyTicketTest(null);
		fft.initCity();
	}
}