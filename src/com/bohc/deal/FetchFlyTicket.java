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
	List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// ץȡ�Ľ��
	List<String> flist = new ArrayList<String>();// �����˵�����
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
			// �����һ��ȡ������
			rlist.clear();
			flist.clear();
			searchFlight(fetchAirLine);
			// �жϵ�ǰ��û��������ȡ������д��ڣ���������ô�ȴ�
			while (BaseIni.browserListenerCount > 0) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String smsg = fetchAirLine.getFromcity() + "����" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ": ��" + (i + 1) + "�����ݣ�" + "\t";
			smsg += "������====" + rlist.size() + "\r\n";
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
					smsg += "\t" + fno + "\t��ͼۣ�" + qft.getTicketprice() + "\t���ۣ�" + qft.getAvgticketprice() + "\tʱ�䣺" + df.format(qft.getStarttime()) + "\t���ࣺ" + qft.getTypetime()
							+ "    ��˾��" + qft.getAireline() + "\r\n";
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
					smsg += "���������˵����ݣ�\r\n";
					for (String msg : flist) {
						smsg += "\t" + msg + "\r\n";
					}
				}

				sbf.setLength(sbf.length() - 1);
				smsg += "���ڱ�������";
				JxBrowserDemo.jd.updateUi(smsg);
				try {
					QlyFlyTicketAction.instance().insertTickets(sbf.toString());
					smsg = "�������ݳɹ�";
				} catch (Exception e) {
					smsg = "���ݱ���ʧ��:" + e.getMessage();
				}
				JxBrowserDemo.jd.updateUi(smsg);
			}
			// ��һ������ȡ��󣬸��ݽ�����趨�����һ��ʱ������ȡ����
			try {
				if (i != list.size() - 1) {
					Thread.sleep(1000 * intervaltime);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// �ж�������û����Ϊ����ֹ�������ֹ��ô��ִֹ��
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
					// �ж���û��ץȡ����ȷ�����ݣ�����Ѿ�ץȡ������ô���������ݽ�����ִ��
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

					if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {

						// ����õ���ȷ��ҳ�棬��ô�Ƴ���ǰ�ļ���
						browser.removeLoadListener(la);
						// ����õ���ȷ��ҳ�棬��ô���ò���ץȡҳ�������
						fetching = true;

						try {
							Thread.sleep(4000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

						// ����ʱ��ѡ������ļ���
						boolean f = false, n = false, an = false, ev = false;// �磬�У����磬����
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

						// �ѵ�ǰ�õ��Ļ�Ʊҳ�����ݽ��н������õ���Ҫ������
						StringBuffer js = new StringBuffer();
						if (BaseIni.fetchCitys.isCkmorning() && f) {
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());// ѡ��

							int i = 0;
							while (true) {
								doc = browser.getDocument().findElement(By.id("filterResultXI2"));
								if (doc != null) {
									condition = doc.getInnerHTML();
									Document d = Jsoup.parse(condition);
									Elements es = d.select("span.result-text");
									if (es != null && es.first() != null && es.first().ownText().trim().equals("���ʱ�䣺����")) {
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

							// ȡ������
							doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
							if (doc != null) {
								content = doc.getInnerHTML();
							}
							qp.getTicket(doc, fetchAirLine, rlist, flist);
							js.setLength(0);
							js.append("var controls = document.getElementsByTagName('input');");
							js.append(
									"for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='0'){ controls[i].checked=true; controls[i].click(); }}}");
							browser.executeJavaScript(js.toString());// ȡ��
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
									if (es != null && es.first() != null && es.first().ownText().trim().equals("���ʱ�䣺����")) {
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

							// ȡ������
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
									if (es != null && es.first() != null && es.first().ownText().trim().equals("���ʱ�䣺����")) {
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

							// ȡ������
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
									if (es != null && es.first() != null && es.first().ownText().trim().equals("���ʱ�䣺����")) {
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

							// ȡ������
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

						// ����ѡ��ץȡ������
						List<QlyFlyticket> temlist = new ArrayList<QlyFlyticket>();
						int a = 0, b = 0, c = 0, d = 0, fcount = 4;
						for (QlyFlyticket q : rlist) {
							if (q.getTypetime().equals("����")) {
								if (a < fcount) {
									temlist.add(q);
									a++;
								}
							} else if (q.getTypetime().equals("����")) {
								if (b < fcount) {
									temlist.add(q);
									b++;
								}
							} else if (q.getTypetime().equals("����")) {
								if (c < fcount) {
									temlist.add(q);
									c++;
								}
							} else if (q.getTypetime().equals("����")) {
								if (d < fcount) {
									temlist.add(q);
									d++;
								}
							}
						}
						rlist.clear();
						rlist.addAll(temlist);

						// }
						// ��һ������ץȡ��ɺ��ж���û������ץȡ�ڶ������ݡ�
						if (!BaseIni.fetchCitys.isFetchnext()) {
							// ���û������Ҫץȡ�ڶ������ݣ���ôִ�в������,���Ұ�ִ���е���������һ
							BaseIni.changeCount(false);
						} else {
							// ���������Ҫץȡ�ڶ������ݣ���ô����ִ��ץȡ�ڶ������ݵķ���
							dealSecondList(fetchAirLine);
						}

						oldtime = System.currentTimeMillis();
					} else if ((System.currentTimeMillis() - oldtime) / 1000 >= 30) {
						// ���û������ץȡ�ڶ������ݣ���ôץȡ���ݵ�ʱ�䳬����Ԥ����ʱ�䣬���򽫲��ٵȴ�,������ǰ����
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
					JxBrowserDemo.jd.updateUi(qft.getFltno() + "(" + qft.getChangeflyno() + ")\t��ת���࣬��ץȡ�ڶ���");
					continue;
				}
				JxBrowserDemo.jd.updateUi("\r\n" + fetchAirLine.getFromcity() + "����" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ":"
						+ qft.getFltno() + " �ڶ����" + (i + 1) + "�����ݣ�");
				searchFlightSecond(fetchAirLine, qft);
				// ���ݵڶ�������ץȡ�����������жϵ�ǰץȡ�����Ƿ������ÿ��һ����һ��
				while (BaseIni.anasecond > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				// �����ǰ������ɳɣ���ô�ȴ����õ�ʱ�䣬�ٿ�ʼץȡ��һ������
				try {
					if (i != rlist.size() - 1) {
						Thread.sleep(1000 * intervaltime);
					}
				} catch (InterruptedException e) {
				}
				// �����Ϊ����ֹ�˵�ǰ������ô������ǰ��������
				if (!BaseIni.fetchstatus) {
					break;
				}
			}
		}
		// ����ڶ�������Ҳץȡ��������ô���õ�һ��ץȡ������������һ����ʾ��ǰ��������ץȡ���
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
				System.out.println("�ڶ������������" + e1.getMessage());
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
					if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {
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
								JxBrowserDemo.jd.updateUi("�ڶ���ץȡ���ļ۸�" + Arrays.asList(tlist.subList(0, 4)));
							} else {
								JxBrowserDemo.jd.updateUi("�ڶ���ץȡ���ļ۸�" + Arrays.asList(tlist));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (BaseIni.anasecond > 0) {
							BaseIni.anasecond--;
						}
						oldtime = System.currentTimeMillis();
					} else if (content != null && content.indexOf("�ú��ߵ�ǰ�޿��ۺ���") != -1) {
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
		JxBrowserDemo.jd.updateBtn(1, "ץȡ");
	}

	public static void main(String[] args) {
		FetchFlyTicket fft = new FetchFlyTicket(null);
		fft.initCity();
	}
}
