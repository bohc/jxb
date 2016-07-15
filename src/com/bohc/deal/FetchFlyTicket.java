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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

public class FetchFlyTicket extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	String gurl = "http://flight.qunar.com";
	TabbedPane bt;
	Browser browser;
	List<FetchAirLine> list = null;
	List<Tarea> fromcity = null;
	List<Tarea> tocity = null;
	List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// 抓取的结果
	List<String> flist = new ArrayList<String>();// 过滤了的数据
	boolean loadover = false;
	QlyFlyticket qt;

	public FetchFlyTicket(TabbedPane bt) {
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
					fetchAirLine.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + fcity.getArea().trim() + "&toCity="
							+ tcity.getArea().trim() + "&fromDate=" + df.format(fdate) + "&toDate=" + df.format(tdate));
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
						fetchAirLineb.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + tcity.getArea().trim() + "&toCity="
								+ fcity.getArea().trim() + "&fromDate=" + df.format(fdate) + "&toDate=" + df.format(tdate));
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
								fetchAirLinebo.setUrl(gurl + "/twell/flight/Search.jsp?from=flight_dom_search&searchType=OnewayFlight&fromCity=" + tcity.getArea().trim()
										+ "&toCity=" + fcity.getArea().trim() + "&fromDate=" + df.format(fdate) + "&toDate=" + df.format(tdate));
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

	private void fetchTicket() {
		int intervaltime = BaseIni.fetchCitys.getIntervaltime();
		int ac = 0;
		for (int i = 0; i < list.size(); i++) {
			FetchAirLine fetchAirLine = list.get(i);
			if (ac % 5 == 0) {
				bt.disposeAllTabs();
				Tab tct = TabFactory.createTab("about:blanck ");
				bt.addTab(tct);
				bt.selectTab(tct);
				browser = bt.tabs.get(0).getContent().browserView.getBrowser();
			}
			try {
				browser.loadURL(fetchAirLine.getUrl());
			} catch (Exception e1) {
				bt.disposeAllTabs();
				Tab tct = TabFactory.createTab("about:blanck");
				bt.addTab(tct);
				bt.selectTab(tct);
				browser = bt.tabs.get(0).getContent().browserView.getBrowser();
				browser.loadURL(fetchAirLine.getUrl());
			}
			ac = 1;
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
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String smsg = fetchAirLine.getFromcity() + "－－" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ": 第" + (i + 1) + "条数据：" + "\t";
			smsg += "总条数====" + rlist.size() + "\r\n";
			if (rlist != null && rlist.size() > 0) {
				StringBuffer sbf = new StringBuffer();
				sbf.append("insert into flytickettemp ");
				sbf.append(
						"(fltno,aireline,airtype,startairport,arriveairport,startcity,startcityjm,arrivecity,arrivecityjm,starttime,arrivetime,offsetrate,offsettime,ticketprice,avgticketprice,agent,fetchdate,opttime,typetime,changeflyno,changecity) ");
				sbf.append("values ");
				for (QlyFlyticket qft : rlist) {
					String fno = qft.getFltno();
					if (qft.getChangeflyno() != null && qft.getChangeflyno().length() > 4) {
						fno += "(" + qft.getChangeflyno() + ")";
					} else {
						fno += "\t";
					}
					smsg += "\t" + fno + "\t最低价：" + qft.getTicketprice() + "\t均价：" + qft.getAvgticketprice() + "\t时间：" + df.format(qft.getStarttime()) + "\t分类：" + qft.getTypetime()
							+ "    公司：" + qft.getAireline() + "\r\n";
					sbf.append("\r\n(");
					sbf.append(QlyFlyTicketAction.instance().combineString(fno));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getAireline()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getAirtype()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getStartairport()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getArriveairport()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getStartcity()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getStartcityjm()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getArrivecity()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getArrivecityjm()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineDate(qft.getStarttime()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineDate(qft.getArrivetime()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getOffsetrate()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getOffsettime()));
					sbf.append(",").append(qft.getTicketprice());
					sbf.append(",").append(qft.getAvgticketprice());
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getAgent()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineDate(qft.getFetchdate()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineDate(qft.getOpttime()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getTypetime()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getChangeflyno()));
					sbf.append(",").append(QlyFlyTicketAction.instance().combineString(qft.getChangecity()));
					sbf.append("),");
				}
				if (flist != null && flist.size() > 0) {
					smsg += "被条件过滤的数据：\r\n";
					for (String msg : flist) {
						smsg += "\t" + msg + "\r\n";
					}
				}

				sbf.setLength(sbf.length() - 1);
				smsg += "正在保存数据";
				JxBrowserDemo.jd.updateUi(smsg);
				try {
					QlyFlyTicketAction.instance().insertTickets(sbf.toString());
					smsg = "保存数据成功";
				} catch (Exception e) {
					smsg = "数据保存失败:" + e.getMessage();
				}
				JxBrowserDemo.jd.updateUi(smsg);
			}
			// 上一个数据取完后，根据界面的设定，间隔一段时间再来取数据
			try {
				if (i != list.size() - 1) {
					Thread.sleep(1000 * intervaltime);
				}
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
			long oldtime = System.currentTimeMillis();
			boolean fetching = false;
			LoadAdapter la = this;
			QunarPrice qp = new QunarPrice();

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				if (event instanceof FrameLoadEvent) {
					// 判断有没有抓取到正确的内容，如果已经抓取到，那么后后面的内容将不再执行
					if (fetching) {
						return;
					}
					String content = null, condition = null;
					DOMElement doc = null;

					try {
						Thread.sleep(1000);
						doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
						if (doc != null) {
							content = doc.getInnerHTML();
						}
					} catch (Exception e) {
						JxBrowserDemo.jd.updateUi("com.bohc.deal.FetchFlyTicket$1.onDocumentLoadedInFrame:" + e.getMessage());
						bt.disposeAllTabs();
						Tab tct = TabFactory.createTab("about:blanck");
						bt.addTab(tct);
						bt.selectTab(tct);
						if (browser != null) {
							browser.dispose();
						}
						browser = bt.tabs.get(0).getContent().browserView.getBrowser();
						browser.loadURL(fetchAirLine.getUrl());
						BaseIni.changeCount(false);
						return;
					}

					if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {

						// 如果得到正确的页面，那么移除当前的监听
						browser.removeLoadListener(la);
						// 如果得到正确的页面，那么设置不再抓取页面的内容
						fetching = true;

						try {
							Thread.sleep(4000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

						// 查找时间选择框有哪几个
						boolean f = false, n = false, an = false, ev = false;// 早，中，下午，晚上
						doc = browser.getDocument().findElement(By.id("newFilterXI2"));
						if (doc != null) {
							content = doc.getInnerHTML();
							Document d = Jsoup.parse(content);
							Elements es = d.getElementsByAttributeValue("type", "checkbox");
							if (es != null && es.first() != null) {
								for (int i = 0; i < es.size(); i++) {
									Element e = es.get(i);
									if (e.val().trim().equals("0")) {
										f = true;
									} else if (e.val().trim().equals("1")) {
										n = true;
									} else if (e.val().trim().equals("2")) {
										an = true;
									} else if (e.val().trim().equals("3")) {
										ev = true;
									}
								}
							}
						}

						// 把当前得到的机票页面内容进行解析，得到想要的数据
						StringBuffer js = new StringBuffer();
						if (BaseIni.fetchCitys.isCkmorning() && f) {
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());// 选中

							int i = 0;
							while (true) {
								doc = browser.getDocument().findElement(By.id("filterResultXI2"));
								if (doc != null) {
									condition = doc.getInnerHTML();
									Document d = Jsoup.parse(condition);
									Elements es = d.select("span.result-text");
									if (es != null && es.first() != null && es.first().ownText().trim().equals("起飞时间：上午")) {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
										}
										break;
									}
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								i++;
								if (i == 10) {
									break;
								}
							}

							// 取出数据
							doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
							if (doc != null) {
								content = doc.getInnerHTML();
							}
							qp.getTicket(doc, fetchAirLine, rlist, flist);
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append(
									"for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].checked=true; controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());// 取消
						}

						if (BaseIni.fetchCitys.isCknoon() && n) {
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='1'){ controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());

							int i = 0;
							while (true) {
								doc = browser.getDocument().findElement(By.id("filterResultXI2"));
								if (doc != null) {
									condition = doc.getInnerHTML();
									Document d = Jsoup.parse(condition);
									Elements es = d.select("span.result-text");
									if (es != null && es.first() != null && es.first().ownText().trim().equals("起飞时间：中午")) {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
										}
										break;
									}
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								i++;
								if (i == 10) {
									break;
								}
							}

							// 取出数据
							doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
							if (doc != null) {
								content = doc.getInnerHTML();
							}
							qp.getTicket(doc, fetchAirLine, rlist, flist);
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append(
									"for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='1'){ controls[i].checked=true; controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());
						}

						if (BaseIni.fetchCitys.isCkanoon() && an) {
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='2'){ controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());

							int i = 0;
							while (true) {
								doc = browser.getDocument().findElement(By.id("filterResultXI2"));
								if (doc != null) {
									condition = doc.getInnerHTML();
									Document d = Jsoup.parse(condition);
									Elements es = d.select("span.result-text");
									if (es != null && es.first() != null && es.first().ownText().trim().equals("起飞时间：下午")) {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
										}
										break;
									}
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								i++;
								if (i == 10) {
									break;
								}
							}

							// 取出数据
							doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
							if (doc != null) {
								content = doc.getInnerHTML();
							}
							qp.getTicket(doc, fetchAirLine, rlist, flist);
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append(
									"for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='2'){ controls[i].checked=true; controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());
						}

						if (BaseIni.fetchCitys.isCkevening() && ev) {
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='3'){ controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());

							int i = 0;
							while (true) {
								doc = browser.getDocument().findElement(By.id("filterResultXI2"));
								if (doc != null) {
									condition = doc.getInnerHTML();
									Document d = Jsoup.parse(condition);
									Elements es = d.select("span.result-text");
									if (es != null && es.first() != null && es.first().ownText().trim().equals("起飞时间：晚上")) {
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
										}
										break;
									}
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								i++;
								if (i == 10) {
									break;
								}
							}

							// 取出数据
							doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
							if (doc != null) {
								content = doc.getInnerHTML();
							}
							qp.getTicket(doc, fetchAirLine, rlist, flist);
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append(
									"for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='3'){ controls[i].checked=true; controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());
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

						// }
						// 第一层数据抓取完成后，判断有没有设置抓取第二层数据。
						if (!BaseIni.fetchCitys.isFetchnext()) {
							// 如果没有设置要抓取第二层数据，那么执行插入操作,并且把执行中的任务数减一
							BaseIni.changeCount(false);
						} else {
							// 如果设置了要抓取第二层数据，那么继续执行抓取第二层数据的方法
							dealSecondList(fetchAirLine);
						}

						oldtime = System.currentTimeMillis();
					} else if ((System.currentTimeMillis() - oldtime) / 1000 >= 30) {
						// 如果没有设置抓取第二层数据，那么抓取数据的时间超过了预定的时间，程序将不再等待,结束当前任务
						if (!BaseIni.fetchCitys.isFetchnext()) {
							browser.removeLoadListener(la);
							BaseIni.changeCount(false);
						}
						oldtime = System.currentTimeMillis();
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
				if (qft.getChangeflyno() != null && qft.getChangeflyno().length() > 4) {
					JxBrowserDemo.jd.updateUi(qft.getFltno() + "(" + qft.getChangeflyno() + ")\t中转航班，不抓取第二层");
					continue;
				}
				JxBrowserDemo.jd.updateUi("\r\n" + fetchAirLine.getFromcity() + "－－" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ":"
						+ qft.getFltno() + " 第二层第" + (i + 1) + "条数据：");
				searchFlightSecond(fetchAirLine, qft);
				// 根据第二层数据抓取的任务数，判断当前抓取任务是否结束，每隔一秒检查一次
				while (BaseIni.anasecond > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				// 如果当前任务完成成，那么等待设置的时间，再开始抓取下一条数据
				try {
					if (i != rlist.size() - 1) {
						Thread.sleep(1000 * intervaltime);
					}
				} catch (InterruptedException e) {
				}
				// 如果人为的终止了当前任务，那么结束当前所有任务
				if (!BaseIni.fetchstatus) {
					break;
				}
			}
		}
		// 如果第二层数据也抓取结束，那么设置第一层抓取数据任务数减一，表示当前这条数据抓取完成
		if (BaseIni.browserListenerCount > 0) {
			BaseIni.browserListenerCount--;
		}
	}

	public void searchFlightSecond(final FetchAirLine fetchAirLine, QlyFlyticket qft) {
		qt = qft;
		Calendar cal = Calendar.getInstance();
		cal.setTime(qft.getStarttime());
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String sb = qft.getFltno() + "|" + sdf.format(qft.getStarttime());

		String url = gurl + "/site/oneway_detail.htm?&origional=" + sb.toString() + "&searchDepartureAirport=" + fetchAirLine.getFromcity() + "&searchArrivalAirport="
				+ fetchAirLine.getTocity() + "&searchDepartureTime=" + sdf.format(qft.getStarttime()) + "&searchArrivalTime=" + sdf.format(cal.getTime())
				+ "&nextNDays=0&arrivalTime=" + sdf.format(cal.getTime()) + "&code=" + qft.getFltno() + "&listlp=" + qft.getTicketprice() + "&sortid=&deptcity="
				+ fetchAirLine.getFromcity() + "&arricity=" + fetchAirLine.getTocity() + "&tserver=union.corp.qunar.com&wtype=all&lowflight=true&lowflightpr="
				+ qft.getTicketprice() + "&from=flight_dom_search";

		try {
			browser.loadURL(url);
		} catch (Exception e) {
			try {
				bt.disposeAllTabs();
				Tab tct = TabFactory.createTab("about:blanck");
				bt.addTab(tct);
				bt.selectTab(tct);
				if (browser != null) {
					browser.dispose();
				}
				browser = tct.getContent().browserView.getBrowser();
				browser.loadURL(url);
			} catch (Exception e1) {
				System.out.println("第二层浏览器出错：" + e1.getMessage());
				if (BaseIni.anasecond > 0) {
					BaseIni.anasecond--;
				}
			}
		}
		loadover = false;
		browser.addLoadListener(new LoadAdapter() {
			long oldtime = System.currentTimeMillis();
			boolean fetching = false;
			LoadAdapter sa = this;

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				if (event instanceof FrameLoadEvent) {
					loadover = true;
					if (fetching) {
						return;
					}
					DOMElement doc = null;
					String content = null;
					doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
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
					} else if (content != null && content.indexOf("该航线当前无可售航班") != -1) {
						browser.removeLoadListener(sa);
						if (BaseIni.anasecond > 0) {
							BaseIni.anasecond--;
						}
						oldtime = System.currentTimeMillis();
					} else if ((System.currentTimeMillis() - oldtime) / 1000 >= 20) {
						fetching = true;
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
		new Thread(new Runnable() {
			long waitetime = System.currentTimeMillis();

			@Override
			public void run() {
				while (true) {
					if (loadover) {
						break;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					if ((System.currentTimeMillis() - waitetime) / 1000 > 20) {
						if (BaseIni.anasecond > 0) {
							BaseIni.anasecond--;
						}
						break;
					}
				}
			}
		}).start();
	}

	@Override
	public void run() {
		super.run();
		fetchTicket();
		BaseIni.fetchstatus = false;
		JxBrowserDemo.jd.updateBtn(1, "抓取");
	}

	public static void main(String[] args) {
		FetchFlyTicket fft = new FetchFlyTicket(null);
		fft.initCity();
	}
}
