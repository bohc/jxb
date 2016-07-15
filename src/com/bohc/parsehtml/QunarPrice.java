package com.bohc.parsehtml;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchAirLine;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.util.FileManger;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

public class QunarPrice {

	Date fromdate = null;
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public void getTicket(DOMElement doc, FetchAirLine fetchAirLine, List<QlyFlyticket> rlist, List<String> flist) {
		if (doc == null) {
			JxBrowserDemo.jd.updateUi("��һ������û��ȡ��");
			return;
		}
		String htmlcontent = doc.getInnerHTML();
		fromdate = fetchAirLine.getFlydate();
		getPrice(htmlcontent, fetchAirLine, rlist, flist);
	}

	public void getTicketSecond(DOMElement doc, FetchAirLine fetchAirLine, List<Integer> rlist) {
		doc = doc.findElement(By.id("wrlistPHXI7XI4"));
		if (doc == null) {
			JxBrowserDemo.jd.updateUi("û�д˺�����Ϣ");
			return;
		}
		String htmlcontent = doc.getInnerHTML();
		fromdate = fetchAirLine.getFlydate();
		getPriceSencond(htmlcontent, rlist);
	}

	/**
	 * ��ȡȥ�Ķ����ļ۸���Ϣ�б���ҳ
	 * 
	 * @param htmlContent
	 *            ȥ�Ķ��۸���Ϣ�б���ҳ����
	 */
	public void getPrice(String htmlContent, FetchAirLine fetchAirLine, List<QlyFlyticket> rlist, List<String> flist) {
		try {
			Document document = Jsoup.parse(htmlContent);
			Elements businesses = document.select("div.avt-column");
			int size = businesses.size();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromdate);

			FileManger.writeFile("d://temp/" + fetchAirLine.getFromcity() + "��" + fetchAirLine.getTocity() + " " + new SimpleDateFormat("yyyy-MM-dd").format(fetchAirLine.getFlydate()) + ".html", htmlContent, "UTF-8");
			int j = 0;
			for (j = 0; j < size; j++) {
				QlyFlyticket qft = new QlyFlyticket();
				try {
					Element businese = businesses.get(j);
					qft.setFltno(businese.select("div.a-model").first().select("span").first().ownText());

					String starttime = "";
					starttime = businese.select("div.a-dep-time").first().ownText();

					// ��ץȡ������
					Elements lnk_a = businese.select("div.n-gx-tit");
					if (BaseIni.fetchCitys.isCkgxhb()) {
						if (lnk_a != null && lnk_a.first() != null && lnk_a.first().ownText().trim().equals("����")) {
							flist.add(qft.getFltno() + "\t" + starttime + "\t" + lnk_a.first().ownText().trim());
							continue;
						}
					}

					// ��ץȡͷ�Ȳ�
					Elements i_fst_cls = businese.select("a.tag-wp").eq(1);
					if (BaseIni.fetchCitys.isCktdc()) {
						if (i_fst_cls != null && i_fst_cls.first() != null && i_fst_cls.first().ownText().trim().equals("ͷ�Ȳ�")) {
							flist.add(qft.getFltno() + "\t" + starttime + "\t" + i_fst_cls.first().ownText().trim());
							continue;
						}
					}

					// ��ץȡ˫�����
					if (BaseIni.fetchCitys.isCkdhb()) {
						if (qft.getFltno().indexOf("/") != -1) {
							flist.add(qft.getFltno() + "\t" + starttime + "\t" + "˫�����");
							continue;
						}
					}

					Elements prcs = businese.select("span.prc_wp");
					String fprice = "";
					if (prcs.size() > 0) {
						fprice = getPriceNew(prcs.first());
					} else {
						fprice = "0";
					}
					fprice = fprice.trim();

					qft.setStartcity(fetchAirLine.getFromcity());
					qft.setStartcityjm(fetchAirLine.getFromcityjm());
					qft.setArrivecity(fetchAirLine.getTocity());
					qft.setArrivecityjm(fetchAirLine.getTocityjm());
					qft.setFetchdate(fetchAirLine.getFlydate());
					qft.setOpttime(new Date());

					qft.setAireline(businese.select("div.a-name").first().ownText());
					qft.setAirtype(businese.select("div.a-model").first().select("span").get(1).ownText());
					qft.setStartairport(businese.select("div.a-dep-airport").first().ownText());

					try {
						calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(starttime.split(":")[0]));
						calendar.set(Calendar.MINUTE, Integer.parseInt(starttime.split(":")[1]));
						qft.setStarttime(calendar.getTime());
						String arrivetime = businese.select("div.a-arr-time").first().ownText();
						Date date = calendar.getTime();
						if (compareString(starttime, arrivetime)) {
							calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
						}

						calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrivetime.split(":")[0]));
						calendar.set(Calendar.MINUTE, Integer.parseInt(arrivetime.split(":")[1]));
						qft.setArrivetime(calendar.getTime());
						calendar.setTime(date);
					} catch (Exception e1) {
						System.out.println("ʱ���ʽ������" + e1.getMessage());
					}
					try {
						Elements es = businese.select("p.a-pty-mint");
						if (es.size() > 0) {
							qft.setOffsetrate(es.first().ownText());
							qft.setOffsettime(es.last().ownText());
						}
					} catch (Exception e) {
						System.out.println("����׼����û�У�" + e.getMessage());
					}

					qft.setTicketprice(Integer.parseInt(fprice) + BaseIni.fetchCitys.getRate());
					// �������ת���࣬��ô�ڼ۸��ϼ���200
					Elements zz = businese.select("div.p-tips-content");
					if (zz != null && zz.size() > 0) {
						zz = zz.first().select("span.bold");
						if (zz != null && zz.first() != null && zz.first().text().trim().equals("��ת")) {
							qft.setTicketprice((Integer.parseInt(fprice) + 200) + BaseIni.fetchCitys.getRate());
							zz = businese.select("div.air-wp");
							if (zz.size() > 1) {
								qft.setChangeflyno(zz.get(1).select("span.n").first().ownText());
							}
							zz = businese.select("div.ifo");
							if (zz.size() > 0) {
								qft.setChangecity(zz.first().ownText());
							}
						}
					}
					qft.setArriveairport(businese.select("div.a-arr-airport").first().ownText());
					try {
						qft.setAgent(businese.select("p.rec-name").first().text());
					} catch (Exception e) {
						// System.out.println("û����Ʊ�㣺"+e.getMessage());
					}

					// ���ϣ�05��00-11��29
					// ���磺11��30-14��29
					// ���磺14��30-18��29
					// ���ϣ�18��30-04��59

					if (starttime != null) {
						String[] ss = starttime.split(":");
						if (ss != null && ss.length == 2) {
							// �����ȡ���ϵģ���ô����
							if (!BaseIni.fetchCitys.isCkmorning()) {
								if (Integer.parseInt(ss[0]) >= 5 && Integer.parseInt(ss[0]) <= 11) {
									if (Integer.parseInt(ss[0]) == 11) {
										if (Integer.parseInt(ss[1]) < 30) {
											continue;
										}
									} else {
										continue;
									}
								}
							} else {
								if (Integer.parseInt(ss[0]) >= 5 && Integer.parseInt(ss[0]) <= 11) {
									boolean b = false;
									if (Integer.parseInt(ss[0]) == 11) {
										if (Integer.parseInt(ss[1]) < 30) {
											b = true;
										}
									} else {
										b = true;
									}
									if (b) {
										qft.setTypetime("����");
									}
								}
							}
							// �����ȡ����ģ���ô����
							if (!BaseIni.fetchCitys.isCknoon()) {
								if (Integer.parseInt(ss[0]) >= 11 && (Integer.parseInt(ss[0]) <= 14)) {
									if (Integer.parseInt(ss[0]) == 11) {
										if (Integer.parseInt(ss[1]) >= 30) {
											continue;
										}
									} else if (Integer.parseInt(ss[0]) == 14) {
										if (Integer.parseInt(ss[1]) < 30) {
											continue;
										}
									} else {
										continue;
									}
								}
							} else {
								if (Integer.parseInt(ss[0]) >= 11 && (Integer.parseInt(ss[0]) <= 14)) {
									boolean b = false;
									if (Integer.parseInt(ss[0]) == 11) {
										if (Integer.parseInt(ss[1]) >= 30) {
											b = true;
										}
									} else if (Integer.parseInt(ss[0]) == 14) {
										if (Integer.parseInt(ss[1]) < 30) {
											b = true;
										}
									} else {
										b = true;
									}
									if (b) {
										qft.setTypetime("����");
									}
								}
							}

							// �����ȡ����ģ���ô����
							if (!BaseIni.fetchCitys.isCkanoon()) {
								if (Integer.parseInt(ss[0]) >= 14 && Integer.parseInt(ss[0]) <= 18) {
									if (Integer.parseInt(ss[0]) == 14) {
										if (Integer.parseInt(ss[1]) >= 30) {
											continue;
										}
									} else if (Integer.parseInt(ss[0]) == 18) {
										if (Integer.parseInt(ss[1]) < 30) {
											continue;
										}
									} else {
										continue;
									}
								}
							} else {
								if (Integer.parseInt(ss[0]) >= 14 && Integer.parseInt(ss[0]) <= 18) {
									boolean b = false;
									if (Integer.parseInt(ss[0]) == 14) {
										if (Integer.parseInt(ss[1]) >= 30) {
											b = true;
										}
									} else if (Integer.parseInt(ss[0]) == 18) {
										if (Integer.parseInt(ss[1]) < 30) {
											b = true;
										}
									} else {
										b = true;
									}
									if (b) {
										qft.setTypetime("����");
									}
								}
							}

							// �����ȡ���ϵģ���ô����
							if (!BaseIni.fetchCitys.isCkevening()) {
								if (Integer.parseInt(ss[0]) >= 18 || Integer.parseInt(ss[0]) < 5) {
									if (Integer.parseInt(ss[0]) == 18) {
										if (Integer.parseInt(ss[1]) >= 30) {
											continue;
										}
									} else {
										continue;
									}
								}
							} else {
								if (Integer.parseInt(ss[0]) >= 18 || Integer.parseInt(ss[0]) < 5) {
									boolean b = false;
									if (Integer.parseInt(ss[0]) == 18) {
										if (Integer.parseInt(ss[1]) >= 30) {
											b = true;
										}
									} else {
										b = true;
									}
									if (b) {
										qft.setTypetime("����");
									}
								}
							}
						}
					}
					rlist.add(qft);
				} catch (Exception e) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					JxBrowserDemo.jd.updateUi(fetchAirLine.getFromcity() + "\t" + fetchAirLine.getTocity() + "\t" + sdf.format(fetchAirLine.getFlydate()) + "\terror:" + j + "  " + e.getMessage() + "\r\n");
				}
			}
			businesses.clear();
		} catch (Exception e) {
			System.out.println("����ǰ����" + e.getMessage() + "\r\n");
			JxBrowserDemo.jd.updateUi("����ǰ����" + e.getMessage() + "\r\n");
		}
	}

	/**
	 * ��ȡȥ�Ķ����ļ۸���Ϣ�б���ҳ
	 * 
	 * @param htmlContent
	 *            ȥ�Ķ��۸���Ϣ�б���ҳ����
	 */
	public void getPriceSencond(String htmlContent, List tlist) {
		try {
			Document document = Jsoup.parse(htmlContent);
			Elements businesses = document.select("div.qvt-column");
			int size = businesses.size();
			Set<String> eset = new HashSet<String>();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromdate);

			for (int j = 0; j < size; j++) {
				try {
					Element businese = businesses.get(j);
					Elements prcs = businese.select("em.prc");
					String fprice = "";
					if (prcs.size() > 1) {
						fprice = getPrice(prcs.get(1));
					} else {
						fprice = getPrice(prcs.first());
					}
					fprice = fprice.trim();
					if (eset.contains(fprice)) {
						continue;
					} else {
						eset.add(fprice);
					}
					tlist.add(Integer.parseInt(fprice) + BaseIni.fetchCitys.getRate());
				} catch (Exception e) {
					JxBrowserDemo.jd.updateUi("error:" + e.getMessage() + "\r\n");
				}
			}
			businesses.clear();
		} catch (Exception e) {
			JxBrowserDemo.jd.updateUi(e.getMessage() + "\r\n");
		}
	}

	/**
	 * ��ȡ��Ʊ�۸񣬸÷����Ὣȥ�Ķ����۸����Ϊ��ȷ�۸�
	 * 
	 * @param prc
	 * @return
	 */
	private String getPriceNew(Element prc) {
		String strstyle = prc.attr("style");
		String pos = Pattern.compile("[^0-9]").matcher(strstyle).replaceAll("");
		strstyle = strstyle.trim() + ";left:-" + pos + "px";// width:48px;left:-48px
		Elements e = prc.getElementsByAttributeValue("style", strstyle);
		String businesse_prc = Pattern.compile("[^0-9]").matcher(e.first().text()).replaceAll("");

		// �����۸���Ϣ
		for (int i = 0; i < (Integer.parseInt(pos) / 16); i++) {
			int p = Integer.parseInt(pos) - (i * 16);
			e = prc.getElementsByAttributeValue("style", "left:-" + p + "px");
			if (e.size() > 0) {
				businesse_prc = businesse_prc.substring(0, i) + e.text() + businesse_prc.substring(i + 1);
			}
		}

		return businesse_prc;
	}

	/**
	 * ��ȡ��Ʊ�۸񣬸÷����Ὣȥ�Ķ����۸����Ϊ��ȷ�۸�
	 * 
	 * @param prc
	 * @return
	 */
	private String getPrice(Element prc) {
		String businesse_prc = prc.text();
		boolean up = prc.html().contains("WIDTH");
		Elements es = prc.getElementsByAttributeValue("style", up ? "WIDTH: 22px; LEFT: -22px" : "width:22px;left:-22px");
		if (es.size() > 0) {
			businesse_prc = es.get(0).text();
		}
		es = prc.getElementsByAttributeValue("style", up ? "WIDTH: 33px; LEFT: -33px" : "width:33px;left:-33px");
		if (es.size() > 0) {
			businesse_prc = es.get(0).text();
		}
		es = prc.getElementsByAttributeValue("style", up ? "WIDTH: 44px; LEFT: -44px" : "width:44px;left:-44px");
		if (es.size() > 0) {
			businesse_prc = es.get(0).text();
		}
		es = prc.getElementsByAttributeValue("style", up ? "WIDTH: 55px; LEFT: -55px" : "width:55px;left:-55px");
		if (es.size() > 0) {
			businesse_prc = es.get(0).text();
		}
		// �����۸���Ϣ
		int length = businesse_prc.length();
		es = prc.getElementsByAttributeValue("style", up ? "LEFT: -11px" : "left:-11px");
		if (es.size() > 0) {
			businesse_prc = businesse_prc.substring(0, length - 1) + es.get(0).text();
		}
		es = prc.getElementsByAttributeValue("style", up ? "LEFT: -22px" : "left:-22px");
		if (es.size() > 0) {
			businesse_prc = businesse_prc.substring(0, length - 2) + es.get(0).text() + businesse_prc.substring(length - 1);
		}
		es = prc.getElementsByAttributeValue("style", up ? "LEFT: -33px" : "left:-33px");
		if (es.size() > 0) {
			businesse_prc = businesse_prc.substring(0, length - 3) + es.get(0).text() + businesse_prc.substring(length - 2);
		}
		es = prc.getElementsByAttributeValue("style", up ? "LEFT: -44px" : "left:-44px");
		if (es.size() > 0) {
			businesse_prc = businesse_prc.substring(0, length - 4) + es.get(0).text() + businesse_prc.substring(length - 3);
		}
		es = prc.getElementsByAttributeValue("style", up ? "LEFT: -55px" : "left:-55px");
		if (es.size() > 0) {
			businesse_prc = businesse_prc.substring(0, length - 5) + es.get(0).text() + businesse_prc.substring(length - 4);
		}
		return businesse_prc;
	}

	public boolean compareString(String starttime, String arrivetime) {
		if (starttime == null || arrivetime == null) {
			return false;
		}
		String[] ss = starttime.split(":");
		String[] as = arrivetime.split(":");
		try {
			if (Integer.parseInt(ss[0]) > Integer.parseInt(as[0])) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static void main(String[] args) {
		String htmlContent = FileManger.readFile("D:/temp/���������� 2015-08-16.html", "UTF-8");
		BaseIni.fetchCitys.setCkgxhb(true);// ������
		BaseIni.fetchCitys.setCktdc(true);// ͷ�Ȳ�
		BaseIni.fetchCitys.setCkdhb(true);// ˫�����
		BaseIni.fetchCitys.setCkmorning(true);// ��
		BaseIni.fetchCitys.setCknoon(true);// ����
		BaseIni.fetchCitys.setCkanoon(true);// ����
		BaseIni.fetchCitys.setCkevening(true);// ����

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, 8);
		cal.set(Calendar.DAY_OF_MONTH, 16);

		QunarPrice qp = new QunarPrice();
		List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();
		List<String> flist = new ArrayList<String>();
		FetchAirLine fetchAirLine = new FetchAirLine();
		fetchAirLine.setFlydate(cal.getTime());
		fetchAirLine.setFromcity("����");
		fetchAirLine.setFromcityjm("86530100");
		fetchAirLine.setTocity("����");
		fetchAirLine.setTocityjm("86110100");
		fetchAirLine.setUrl("");
		qp.fromdate = fetchAirLine.getFlydate();
		qp.getPrice(htmlContent, fetchAirLine, rlist, flist);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("���У�" + rlist.size());
		for (QlyFlyticket qft : rlist) {
			System.out.println(qft.getFltno() + "\t" + sdf.format(qft.getStarttime()) + "\t" + qft.getTypetime());
		}
	}
}
