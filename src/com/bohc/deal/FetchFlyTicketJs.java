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
	List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// ץȡ�Ľ��
	List<String> flist = new ArrayList<String>();// �����˵�����

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
			try {
				Thread.sleep(1000 * intervaltime);
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
			boolean fetching = false;
			LoadAdapter la = this;

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				if (event instanceof FrameLoadEvent) {
					// �ж���û��ץȡ����ȷ�����ݣ�����Ѿ�ץȡ������ô���������ݽ�����ִ��
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
					if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {
						// ����õ���ȷ��ҳ�棬��ô�Ƴ���ǰ�ļ���
						browser.removeLoadListener(la);
						
						// ����õ���ȷ��ҳ�棬��ô���ò���ץȡҳ�������
						fetching = true;

						// �ѵ�ǰ�õ��Ļ�Ʊҳ�����ݽ��н������õ���Ҫ������
						StringBuffer js = new StringBuffer();

						// ��ҳ
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
							browser.executeJavaScript(js.toString());// ѡ��
							JxBrowserDemo.jd.updateUi(fdate);

							js.setLength(0);
							js.append("var js_btn_sch = document.getElementById('js_btn_sch');");
							js.append("js_btn_sch.click();");
							browser.executeJavaScript(js.toString());// ѡ��
							while (true) {
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {
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
								browser.executeJavaScript(js.toString());// ѡ��
								try {
									Thread.sleep(4000);
									// ȡ������
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
								browser.executeJavaScript(js.toString());//ȡ��
								JxBrowserDemo.jd.updateUi("�������ݲɼ����");
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
								// ȡ������
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='1'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("�������ݲɼ����");
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
								// ȡ������
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='2'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("�������ݲɼ����");
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
								// ȡ������
								doc = browser.getDocument().findElement(By.id("hdivResultPanel"));
								if (doc != null) {
									content = doc.getInnerHTML();
								}
								qp.getTicket(doc, fetchAirLine, rlist, flist);
								js.setLength(0);
								js.append("var controls = document.getElementsByTagName('input');");
								js.append("for(var i=0; i<controls.length; i++){if(controls[i].type=='checkbox'){if(controls[i].value=='3'){ controls[i].checked=true; controls[i].click(); }}}");
								browser.executeJavaScript(js.toString());
								JxBrowserDemo.jd.updateUi("�������ݲɼ����");
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

							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							String smsg = fetchAirLine.getFromcity() + "����" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ": ��" + (i + 1) + "�����ݣ�" + "\t";
							smsg += "������=" + rlist.size() + "\r\n";
							if (rlist != null && rlist.size() > 0) {
								StringBuffer sbf = new StringBuffer();
								sbf.append("insert into flytickettemp ");
								sbf.append("(fltno,aireline,airtype,startairport,arriveairport,startcity,startcityjm,arrivecity,arrivecityjm,starttime,arrivetime,offsetrate,offsettime,ticketprice,avgticketprice,agent,fetchdate,opttime,typetime) ");
								sbf.append("values ");
								for (QlyFlyticket qft : rlist) {
									smsg += "\t" + qft.getFltno() + "\t�ͼۣ�" + qft.getTicketprice() + "\t���ۣ�" + qft.getAvgticketprice() + "\tʱ�䣺" + df.format(qft.getStarttime()) + "\t���ࣺ" + qft.getTypetime() + "\t��˾��" + qft.getAireline() + "\r\n";
								}
								if (flist != null && flist.size() > 0) {
									smsg += "���������˵����ݣ�\r\n";
									for (String msg : flist) {
										smsg += "\t" + msg + "\r\n";
									}
								}

								sbf.setLength(sbf.length() - 1);
								JxBrowserDemo.jd.updateUi(smsg + "\r\n");
							}
						}
						// ��һ������ץȡ��ɺ��ж���û������ץȡ�ڶ������ݡ�
						if (!BaseIni.fetchCitys.isFetchnext()) {
							// ���û������Ҫץȡ�ڶ������ݣ���ôִ�в������,���Ұ�ִ���е���������һ
							BaseIni.changeCount(false);
						} else {
							// ���������Ҫץȡ�ڶ������ݣ���ô����ִ��ץȡ�ڶ������ݵķ���
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
				JxBrowserDemo.jd.updateUi("\r\n" + fetchAirLine.getFromcity() + "����" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + ":" + qft.getFltno() + " �ڶ����" + (i + 1) + "�����ݣ�");
				searchFlightSecond(fetchAirLine, qft);
				// ���ݵڶ�������ץȡ�����������жϵ�ǰץȡ�����Ƿ�������ڸ�һ����һ��
				while (BaseIni.anasecond > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				// �����ǰ������ɳɣ���ô�ȴ����õ�ʱ�䣬�ٿ�ʼץȡ��һ������
				try {
					Thread.sleep(1000 * intervaltime);
				} catch (InterruptedException e) {
				}
				// �����Ϊ����ֹ�˵�ǰ������ô������ǰ��������
				if (!BaseIni.fetchstatus) {
					break;
				}
			}
		}
		// ����ڶ�������Ҳץȡ��������ô���õ�һ��ץȡ������������һ����ʾ��ǰ��������ץȡ���
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
			JxBrowserDemo.jd.updateBtn(1, "ץȡ");
		}
	}

	public static void main(String[] args) {
		FetchFlyTicketJs fft = new FetchFlyTicketJs(null);
		fft.initCity();
	}
}
