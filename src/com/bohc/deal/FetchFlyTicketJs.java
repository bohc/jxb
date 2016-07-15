package com.bohc.deal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.BeanUtils;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchAirLine;
import com.bohc.bean.action.QlyFlyTicketAction;
import com.bohc.parsehtml.QunarPrice;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.sh.entities.Tarea;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.demo.Tab;
import com.teamdev.jxbrowser.chromium.demo.TabFactory;
import com.teamdev.jxbrowser.chromium.demo.TabbedPane;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

public class FetchFlyTicketJs extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	String gurl = "http://flight.qunar.com";
	TabbedPane bt;
	Browser browser;
	List<FetchAirLine> list = null;
	List<Tarea> fromcity = null;
	List<Tarea> tocity = null;
	List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// 抓取的结果
	List<String> flist = new ArrayList<String>();// 过滤了的数据

	QlyFlyticket qt;

	public FetchFlyTicketJs(TabbedPane bt) {
		super();
		this.bt = bt;
		initCity();
	}

	public void initCity() {
		list = new ArrayList<FetchAirLine>();
		Calendar c = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		fromcity = BaseIni.fetchCitys.getFromcitys();
		tocity = BaseIni.fetchCitys.getTocitys();

		for (Tarea fcity : fromcity) {
			if (fcity == null || fcity.getArea().trim().equals("") || fcity.getArea().trim().equalsIgnoreCase("null")) {
				continue;
			}
			for (Tarea tcity : tocity) {
				if (tcity == null || tcity.getArea().trim().equals("") || fcity.getArea().trim().equalsIgnoreCase("null")) {
					continue;
				}
				c.setTime(BaseIni.fetchCitys.getStartdate());
				Date fdate = c.getTime();
				c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
				Date tdate = c.getTime();

				FetchAirLine fetchAirLine = new FetchAirLine();
				fetchAirLine.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + fcity.getArea().trim() + "&toCity=" + tcity.getArea().trim() + "&fromDate=" + df.format(fdate) + "&toDate="
						+ df.format(tdate));
				fetchAirLine.setFlydate(fdate);
				fetchAirLine.setFromcity(fcity.getArea());
				fetchAirLine.setFromcityjm(fcity.getAcode());
				fetchAirLine.setTocity(tcity.getArea());
				fetchAirLine.setTocityjm(tcity.getAcode());
				list.add(fetchAirLine);
			}
		}
	}

	private void fetchTicket() {
		int intervaltime = BaseIni.fetchCitys.getIntervaltime();
		for (int i = 0; i < list.size(); i++) {
			FetchAirLine fetchAirLine = list.get(i);
			browser.loadURL(fetchAirLine.getUrl());
			// 清空上一次取的数据
			rlist.clear();
			flist.clear();
			searchFlight(fetchAirLine);
			// 判断当前有没有数据在取，如果有大于１的任务，那么等待
			while (BaseIni.browserListenerCount > 0) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
			try {
				Thread.sleep(1000 * intervaltime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 判断任务有没有人为的终止，如果终止那么终止执行
			if (!BaseIni.fetchstatus) {
				break;
			}
		}
		BaseIni.changeCount(false);
		JxBrowserDemo.jd.updateBtn(1, null);
	}

	public void searchFlight(final FetchAirLine fetchAirLine) {
		browser.addLoadListener(new LoadAdapter() {
			boolean fetching = false;
			LoadAdapter la = this;

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				if (event instanceof FrameLoadEvent) {
					// 判断有没有抓取到正确的内容，如果已经抓取到，那么后后面的内容将不再执行
					if (fetching) {
						return;
					}
					
					QunarPrice qp = new QunarPrice();
					String content = null;
					DOMElement doc = null;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
					if (doc != null) {
						content = doc.getInnerHTML();
					}
					if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {
						// 如果得到正确的页面，那么移除当前的监听
						browser.removeLoadListener(la);
						
						// 如果得到正确的页面，那么设置不再抓取页面的内容
						fetching = true;

						// 把当前得到的机票页面内容进行解析，得到想要的数据
						StringBuffer js = new StringBuffer();

						// 翻页
						// js.append("var dom=document.getElementById('p1XI4');");
						// js.append("dom.click();");
						// browser.executeJavaScript(js.toString());

						int fetchday = 0;
						try {
							fetchday = (int) ((BaseIni.fetchCitys.getEnddate().getTime() - BaseIni.fetchCitys.getStartdate().getTime()) / (1000 * 60 * 60 * 24));
						} catch (Exception e) {
							fetchday = 0;
						}

						SimpleDateFormat ssd = new SimpleDateFormat("yyyy-MM-dd");
						Calendar cad = Calendar.getInstance();
						cad.setTime(BaseIni.fetchCitys.getStartdate());

						for (int i = 0; i < fetchday; i++) {
							rlist.clear();
							flist.clear();
							cad.set(Calendar.DAY_OF_MONTH, cad.get(Calendar.DAY_OF_MONTH) + 1);
							String fdate = ssd.format(cad.getTime());
							js.setLength(0);
							js.append("var fromDate = document.getElementsByName('fromDate');");
							js.append("fromDate[0].value='" + fdate + "';");
							browser.executeJavaScript(js.toString());// 选中
							JxBrowserDemo.jd.updateUi(fdate);

							js.setLength(0);
							js.append("var js_btn_sch = document.getElementById('js_btn_sch');");
							js.append("js_btn_sch.click();");
							browser.executeJavaScript(js.toString());// 选中
							while (true) {
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {
									break;
								}
							}
							
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if (BaseIni.fetchCitys.isCkmorning()) {
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());// 选中
								try {
									Thread.sleep(4000);
									// 取出数据
									doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
									if (doc != null) {
										content = doc.getInnerHTML();
									}
									qp.getTicket(doc, fetchAirLine, rlist, flist);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());//取消
								JxBrowserDemo.jd.updateUi("早上数据采集完毕");
							}

							if (BaseIni.fetchCitys.isCknoon()) {
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='1'){ controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								try {
									Thread.sleep(4000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// 取出数据
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='1'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("中午数据采集完毕");
							}

							if (BaseIni.fetchCitys.isCkanoon()) {
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='2'){ controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								try {
									Thread.sleep(4000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// 取出数据
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='2'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("下午数据采集完毕");
							}

							if (BaseIni.fetchCitys.isCkevening()) {
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='3'){ controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								try {
									Thread.sleep(4000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// 取出数据
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='3'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("晚上数据采集完毕");
							}

							Collections.sort(rlist, new Comparator<QlyFlyticket>() {
								public int compare(QlyFlyticket arg0, QlyFlyticket arg1) {
									if (arg0.getTicketprice() > arg1.getTicketprice()) {
										return 1;
									} else if (arg0.getTicketprice() < arg1.getTicketprice()) {
										return -1;
									}
									return 0;
								}
							});

							// 重新选择抓取的数据
							List<QlyFlyticket> temlist = new ArrayList<QlyFlyticket>();
							int a = 0, b = 0, c = 0, d = 0, fcount = 4;
							for (QlyFlyticket q : rlist) {
								if (q.getTypetime().equals("上午")) {
									if (a < fcount) {
										temlist.add(q);
										a++;
									}
								} else if (q.getTypetime().equals("中午")) {
									if (b < fcount) {
										temlist.add(q);
										b++;
									}
								} else if (q.getTypetime().equals("下午")) {
									if (c < fcount) {
										temlist.add(q);
										c++;
									}
								} else if (q.getTypetime().equals("晚上")) {
									if (d < fcount) {
										temlist.add(q);
										d++;
									}
								}
							}
							rlist.clear();
							rlist.addAll(temlist);

							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							String smsg = fetchAirLine.getFromcity() + "－－" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ": 第" + (i + 1) + "条数据：" + "\t";
							smsg += "总条数=" + rlist.size() + "\r\n";
							if (rlist != null && rlist.size() > 0) {
								StringBuffer sbf = new StringBuffer();
								sbf.append("insert into flytickettemp ");
								sbf.append("(fltno,aireline,airtype,startairport,arriveairport,startcity,startcityjm,arrivecity,arrivecityjm,starttime,arrivetime,offsetrate,offsettime,ticketprice,avgticketprice,agent,fetchdate,opttime,typetime) ");
								sbf.append("values ");
								for (QlyFlyticket qft : rlist) {
									smsg += "\t" + qft.getFltno() + "\t低价：" + qft.getTicketprice() + "\t均价：" + qft.getAvgticketprice() + "\t时间：" + df.format(qft.getStarttime()) + "\t分类：" + qft.getTypetime() + "\t公司：" + qft.getAireline() + "\r\n";
								}
								if (flist != null && flist.size() > 0) {
									smsg += "被条件过滤的数据：\r\n";
									for (String msg : flist) {
										smsg += "\t" + msg + "\r\n";
									}
								}

								sbf.setLength(sbf.length() - 1);
								JxBrowserDemo.jd.updateUi(smsg + "\r\n");
							}
						}
						// 第一层数据抓取完成后，判断有没有设置抓取第二层数据。
						if (!BaseIni.fetchCitys.isFetchnext()) {
							// 如果没有设置要抓取第二层数据，那么执行插入操作,并且把执行中的任务数减一
							BaseIni.changeCount(false);
						} else {
							// 如果设置了要抓取第二层数据，那么继续执行抓取第二层数据的方法
							//dealSecondList(fetchAirLine);
						}
					}
				}
			}
		});
		BaseIni.changeCount(true);
	}

	private void dealSecondList(FetchAirLine fetchAirLine) {
		if (rlist != null && rlist.size() > 0) {
			bt.disposeAllTabs();
			Tab tct = TabFactory.createTab("about:blanck");
			bt.addTab(tct);
			bt.selectTab(tct);
			if (browser != null) {
				browser.dispose();
			}
			browser = tct.getContent().browserView.getBrowser();
			int intervaltime = BaseIni.fetchCitys.getIntervaltime();

			for (int i = 0; i < rlist.size(); i++) {
				QlyFlyticket qft = (QlyFlyticket) rlist.get(i);
				JxBrowserDemo.jd.updateUi("\r\n" + fetchAirLine.getFromcity() + "－－" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ":" + qft.getFltno() + " 第二层第" + (i + 1) + "条数据：");
				searchFlightSecond(fetchAirLine, qft);
				// 根据第二层数据抓取的任务数，判断当前抓取任务是否结束，第隔一秒检查一次
				while (BaseIni.anasecond > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				// 如果当前任务完成成，那么等待设置的时间，再开始抓取下一条数据
				try {
					Thread.sleep(1000 * intervaltime);
				} catch (InterruptedException e) {
				}
				// 如果人为的终止了当前任务，那么结束当前所有任务
				if (!BaseIni.fetchstatus) {
					break;
				}
			}
		}
		// 如果第二层数据也抓取结束，那么设置第一层抓取数据任务数减一，表示当前这条数据抓取完成
		BaseIni.changeCount(false);
	}

	public void searchFlightSecond(final FetchAirLine fetchAirLine, QlyFlyticket qft) {
		qt = qft;
		Calendar cal = Calendar.getInstance();
		cal.setTime(qft.getStarttime());
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String sb = qft.getFltno() + "|" + sdf.format(qft.getStarttime());
		String url = gurl + "/site/oneway_detail.htm?&origional=" + sb.toString() + "&searchDepartureAirport=" + fetchAirLine.getFromcity() + "&searchArrivalAirport=" + fetchAirLine.getTocity() + "&searchDepartureTime="
				+ sdf.format(qft.getStarttime()) + "&searchArrivalTime=" + sdf.format(cal.getTime()) + "&nextNDays=0&arrivalTime=" + sdf.format(cal.getTime()) + "&code=" + qft.getFltno() + "&listlp=" + qft.getTicketprice() + "&sortid=&deptcity="
				+ fetchAirLine.getFromcity() + "&arricity=" + fetchAirLine.getTocity() + "&tserver=union.corp.qunar.com&wtype=all&lowflight=true&lowflightpr=" + qft.getTicketprice() + "&from=fi_re_search";
		browser.loadURL(url);
		browser.addLoadListener(new LoadAdapter() {
			long oldtime = System.currentTimeMillis();
			boolean fetching = false;
			LoadAdapter sa = this;

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				if (event instanceof FrameLoadEvent) {
					if (fetching) {
						return;
					}
					DOMElement doc = null;
					String content = null;
					doc = browser.getDocument().findElement(By.id("j-pagecontainer"));
					if (doc != null) {
						content = doc.getInnerHTML();
					}
					if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {
						browser.removeLoadListener(sa);
						fetching = true;
						QunarPrice qp = new QunarPrice();
						try {
							List<Integer> tlist = new ArrayList<Integer>();
							qp.getTicketSecond(doc, fetchAirLine, tlist);
							Collections.sort(tlist, new Comparator<Integer>() {
								public int compare(Integer arg0, Integer arg1) {
									if (arg0 > arg1) {
										return 1;
									} else if (arg0 < arg1) {
										return -1;
									}
									return 0;
								}
							});
							if (tlist != null && tlist.size() > 0) {
								int sumprice = 0;
								for (int i = 0; i < tlist.size(); i++) {
									if (i == 4) {
										break;
									}
									int p = tlist.get(i);
									sumprice += p;
								}
								int avgprice = 0;
								if (tlist.size() >= 4) {
									avgprice = sumprice / 4;
								} else {
									avgprice = sumprice / tlist.size();
								}
								for (int i = 0; i < rlist.size(); i++) {
									QlyFlyticket t = rlist.get(i);
									if (qt.equals(t)) {
										rlist.get(i).setAvgticketprice(avgprice);
									}
								}
							}
							if (tlist.size() > 4) {
								JxBrowserDemo.jd.updateUi("第二层抓取到的价格：" + Arrays.asList(tlist.subList(0, 4)));
							} else {
								JxBrowserDemo.jd.updateUi("第二层抓取到的价格：" + Arrays.asList(tlist));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (BaseIni.anasecond > 0) {
							BaseIni.anasecond--;
						}
						oldtime = System.currentTimeMillis();
					} else if ((System.currentTimeMillis() - oldtime) / 1000 >= 20) {
						browser.removeLoadListener(sa);
						if (BaseIni.anasecond > 0) {
							BaseIni.anasecond--;
						}
						oldtime = System.currentTimeMillis();
					}
				}
			}
		});
		BaseIni.anasecond++;
	}

	@Override
	public void run() {
		super.run();
		if (!bt.tabs.isEmpty()) {
			browser = bt.tabs.get(0).getContent().browserView.getBrowser();
			fetchTicket();
			BaseIni.fetchstatus = false;
			JxBrowserDemo.jd.updateBtn(1, "抓取");
		}
	}

	public static void main(String[] args) {
		FetchFlyTicketJs fft = new FetchFlyTicketJs(null);
		fft.initCity();
	}
}
