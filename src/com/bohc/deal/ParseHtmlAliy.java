package com.bohc.deal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchAirLine;
import com.bohc.exception.MyParseException;
import com.bohc.parsehtml.QunarPrice;
import com.bohc.sh.entities.QlyFlyticket;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

public class ParseHtmlAliy {
	static ParseHtmlAliy aliy;

	static {
		if (aliy == null) {
			aliy = new ParseHtmlAliy();
		}
	}

	public static ParseHtmlAliy instance() {
		return aliy;
	}

	// ����ʱ��ѡ������ļ���
	private boolean f = false, n = false, an = false, ev = false;// �磬�У����磬����
	private boolean timeflag = false;// �ж���û��ʱ��ѡ�����û�У��ǲ�������
	private QunarPrice qp = new QunarPrice();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private Stack<QlyFlyticket> zdStackMoning = new Stack<QlyFlyticket>();// ����ֱ�ﺽ��
	private Stack<QlyFlyticket> zdStackNoon = new Stack<QlyFlyticket>();// ����ֱ�ﺽ��
	private Stack<QlyFlyticket> zdStackAnoon = new Stack<QlyFlyticket>();// ����ֱ�ﺽ��
	private Stack<QlyFlyticket> zdStackEvening = new Stack<QlyFlyticket>();// ����ֱ�ﺽ��
	private Stack<QlyFlyticket> zzStackMoning = new Stack<QlyFlyticket>();// ������ת����
	private Stack<QlyFlyticket> zzStackNoon = new Stack<QlyFlyticket>();// ������ת����
	private Stack<QlyFlyticket> zzStackAnoon = new Stack<QlyFlyticket>();// ������ת����
	private Stack<QlyFlyticket> zzStackEvening = new Stack<QlyFlyticket>();// ������ת����

	public void loadAllDate(Browser browser) throws MyParseException {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		DOMElement doc = null;
		String content = null;

		/**
		doc = parseAllData(browser, "ks-component1026");
		if (doc != null) {
			JxBrowserDemo.jd.updateUi(doc.getInnerHTML());
			long waittime = System.currentTimeMillis();
			// while (true) {
			// doc = parseAllData(browser, "page2");
			// if (doc != null) {
			// System.out.println(doc.getInnerHTML());
			// break;
			// }
			// long curwaittime = System.currentTimeMillis();
			// if((curwaittime-waittime)/1000 >= 10){
			// break;
			// }
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// }
			StringBuffer js = new StringBuffer();
			js.append("var frames = document.getElementsByTagName('iframe');");
			js.append("var doc = frames[0].contentDocument;");
			// js.append("var ifr1win = window.frames[0];");
			// js.append("ifr1win.contentDocument.document.getElementById(\"TPL_username_1\").value
			// = \"Johnny Bravo\";");
			js.append("alert('doc');");
			//js.append("if(frames[0].src){alert(doc);}");
			execJs(browser, js.toString());
		}
		 */
		doc = parseAllData(browser, "J_FlightListBox");
		if (doc != null) {
			content = doc.getInnerHTML();
		}

		if (content != null && content.indexOf("���ڼ����У����Ժ�") == -1) {
			StringBuffer js = new StringBuffer();
			js.append("window.scrollTo(0,document.body.scrollHeight); ");
			js.append("setTimeout(\"var mydiv=document.createElement('div');mydiv.setAttribute('id','bhc_flag');document.body.appendChild(mydiv);\", 5000 );");
			execJs(browser, js.toString());
			// �ж���û��ִ�гɹ�
			isLoadFinish(browser);
			js.setLength(0);
			js.append("window.scrollTo(0,0); ");
			execJs(browser, js.toString());

			BaseIni.fetchAirLine.setFetching(true);
			try {
				// �����ͣ��Ϊ����ҳ��ص���
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (BaseIni.fetchAirLine.getLevel() == 0) {// �����һ������
				// checkTime(browser);// ���ʱ��ѡ�����ļ���ѡ��
				// parseHtml(browser);// ����ʱ��ѡ�ȡ����������
				parseHtmlNoFilterTime(browser);
				filterLine();// �������õĹ�����������������ţ�ͷ�Ȳգ������࣬��ת����
				sortAirLine();// �Ժ��߽��������������
				typeAirLine();// ��ץȡ�������ݽ�������ȡֵ����
				sortFetchTicket();// ����ץȡ�������ݣ����۸�͵���ȡǰ�������ߣ�����������
				clearStack();// ��ն�ջ����
				// ��һ������ץȡ��ɺ��ж���û������ץȡ�ڶ������ݡ�
				if (BaseIni.fetchCitys.isFetchnext() && BaseIni.fetchAirLine.getLevel() == 0) {
					// ���������Ҫץȡ�ڶ������ݣ���ô����ִ��ץȡ�ڶ������ݵķ���
					dealSecondList();
				}
				// ���û������Ҫץȡ�ڶ������ݣ���ôִ�в������,���Ұ�ִ���е���������һ
				BaseIni.changeCount(false);
			} else if (BaseIni.fetchAirLine.getLevel() == 1) {// �ڶ���ֱ�ﺽ��
				parseSencondDataDirect(browser);
			}
		}
	}

	private void parseSencondDataDirect(Browser browser) {
		// TODO Auto-generated method stub

	}

	private void dealSecondList() {
		// TODO Auto-generated method stub

	}

	// ������еĶ�ջ����
	private void clearStack() {
		zdStackMoning.clear();
		zzStackMoning.clear();
		zdStackNoon.clear();
		zzStackNoon.clear();
		zdStackAnoon.clear();
		zzStackAnoon.clear();
		zdStackEvening.clear();
		zzStackEvening.clear();
	}

	// ��ץȡ��������ʱ����һ�ε��������
	private void sortFetchTicket() {
		// ����ѡ��ץȡ������
		BaseIni.rlist.clear();
		List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();
		rlist.addAll(getLine(zdStackMoning, zzStackMoning));
		rlist.addAll(getLine(zdStackNoon, zzStackNoon));
		rlist.addAll(getLine(zdStackAnoon, zzStackAnoon));
		rlist.addAll(getLine(zdStackEvening, zzStackEvening));
		BaseIni.rlist.addAll(rlist);
	}

	//
	private List<QlyFlyticket> getLine(Stack<QlyFlyticket> zdStack, Stack<QlyFlyticket> zzStack) {
		List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();
		int fcount = 4;
		// ��ȡֱ��ĺ;�ͣ��
		while (true) {
			if (!zdStack.isEmpty()) {
				rlist.add(zdStack.pop());
				if (rlist.size() >= fcount) {
					break;
				}
			} else {
				break;
			}
		}
		// ���ݲ�������ô��ȡ��ת��
		if (rlist.size() < 4) {
			while (true) {
				if (!zzStack.isEmpty()) {
					rlist.add(zzStack.pop());
					if (rlist.size() >= fcount) {
						break;
					}
				} else {
					break;
				}
			}
		}
		return rlist;
	}

	// ��ץȡ�������ݽ�������ȡֵ���࣬������Ҫ�Ĺ��������������ݷ��䵽��ͬ�Ķ�ջ���У����Ұ��۸�˳��Ӹߵ��͵�˳��ѹ��
	private void typeAirLine() {
		List<QlyFlyticket> rlist = BaseIni.rlist;
		if (rlist == null || rlist.size() == 0) {
			return;
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

		for (int i = rlist.size(); i > 0; i--) {
			QlyFlyticket qft = rlist.get(i - 1);
			if (qft.getLinetype().equals("ֱ��") || qft.getLinetype().equals("��ͣ")) {
				if (qft.getTypetime().equals("����")) {
					zdStackMoning.push(qft);
				} else if (qft.getTypetime().equals("����")) {
					zdStackNoon.push(qft);
				}
				if (qft.getTypetime().equals("����")) {
					zdStackAnoon.push(qft);
				}
				if (qft.getTypetime().equals("����")) {
					zdStackEvening.push(qft);
				}
			} else if (qft.getLinetype().equals("��ת")) {
				if (qft.getTypetime().equals("����")) {
					zzStackMoning.push(qft);
				} else if (qft.getTypetime().equals("����")) {
					zzStackNoon.push(qft);
				}
				if (qft.getTypetime().equals("����")) {
					zzStackAnoon.push(qft);
				}
				if (qft.getTypetime().equals("����")) {
					zzStackEvening.push(qft);
				}
			}
		}
	}

	// �������õĹ�����������������ţ�ͷ�Ȳգ�������
	private void filterLine() {
		List<QlyFlyticket> rlist = BaseIni.rlist;
		if (rlist == null || rlist.size() == 0) {
			return;
		}
		for (Iterator<QlyFlyticket> it = rlist.iterator(); it.hasNext();) {
			QlyFlyticket qft = it.next();
			// ���˹�����
			if (BaseIni.fetchCitys.isCkgxhb()) {
				if (qft.getShareline().equals("����")) {
					it.remove();
					continue;
				}
			}
			// ������ת����
			if (BaseIni.fetchCitys.isCk_zzhb()) {
				if (qft.getLinetype().equals("��ת")) {
					it.remove();
					continue;
				}
			}
			// ����˫�����
			if (BaseIni.fetchCitys.isCkdhb()) {
				if (qft.getFltno().indexOf("/") != -1) {
					it.remove();
					continue;
				}
			}
			// ����ͷ�Ȳ�
			if (BaseIni.fetchCitys.isCktdc()) {
				if (qft.getSitlevel().equals("ͷ�Ȳ�")) {
					it.remove();
					continue;
				}
			}
			// �������Ϻ���
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("����")) {
					it.remove();
					continue;
				}
			}
			// �������纽��
			if (!BaseIni.fetchCitys.isCknoon()) {
				if (qft.getTypetime().equals("����")) {
					it.remove();
					continue;
				}
			}
			// �������纽��
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("����")) {
					it.remove();
					continue;
				}
			}
			// �������Ϻ���
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("����")) {
					it.remove();
					continue;
				}
			}
			// ���˼۸�Ϊ 0 �ĺ���
			if (!BaseIni.fetchCitys.isCkp0()) {
				if (qft.getTicketprice() == 0) {
					it.remove();
					continue;
				}
			}
		}
	}

	private DOMElement parseAllData(Browser browser, String vid) throws MyParseException {
		// ȡ������
		DOMElement doc = null;
		try {
			long oldtime = System.currentTimeMillis();
			while (true) {
				doc = browser.getDocument().findElement(By.id(vid));
				if (doc != null) {
					break;
				}
				if ((System.currentTimeMillis() - oldtime) / 1000 > 10) {
					break;
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			throwMyException("ҳ������쳣");
		}
		return doc;
	}

	private void throwMyException(String msg) throws MyParseException {
		// ����ǵ�һ�����ݼ���ʧ�ܣ���ô���������·֮ǰץȡ�ɹ������ݣ�����ץȡ
		if (BaseIni.fetchAirLine.getLevel() == 0) {
			BaseIni.rlist.clear();
			BaseIni.flist.clear();
		}
		// ������ץȡʧ�ܵ���������״̬��ѹ������ջ����ץȡ
		FetchAirLine fal = BaseIni.fetchAirLine;
		fal.setFetching(false);
		BaseIni.fetchFailList.add(fal);
		throw new MyParseException(msg);
	}

	// ͳһ��ִ�� js ����������Ƿ��ص�ͳһ�ĵط�����
	private void execJs(Browser browser, String js) throws MyParseException {
		try {
			browser.executeJavaScript(js);// ִ��js
		} catch (Exception e) {
			throwMyException("ҳ��ִ��js�쳣");
		}
	}

	// �ж�ҳ�� js �Ƿ�ִ�����
	private void isLoadFinish(Browser browser) throws MyParseException {
		DOMElement doc = null;
		long oldtime = System.currentTimeMillis();
		while (true) {
			doc = parseAllData(browser, "bhc_flag");
			if (doc != null) {
				System.out.println(doc.getInnerHTML());
				break;
			}
			if ((System.currentTimeMillis() - oldtime) / 1000 > 10) {
				break;
			}
		}
	}

	private void parseHtmlNoFilterTime(Browser browser) throws MyParseException {
		// ����磬�У��£���һ����û��ѡ����ôȡ��ǰҳ������к���
		fetchAllLine(parseAllData(browser, "J_FlightListBox"));
	}

	// ץȡ���б�ҳ��ĺ���
	private void fetchAllLine(DOMElement doc) {
		if (doc == null)
			return;
		Document document = Jsoup.parse(doc.getInnerHTML());
		// �õ����а������ߵ� div Ԫ��
		Elements businesses = document.select("div.J_FlightItem");
		int size = businesses.size();

		Calendar calendar = Calendar.getInstance();
		int j = 0;
		// ֻ�к��ߴ��� 0 �Ž���
		for (j = 0; j < size; j++) {
			Element businese = businesses.get(j);

			QlyFlyticket qft = new QlyFlyticket();
			qft.setStartcity(BaseIni.fetchAirLine.getFromcity());
			qft.setStartcityjm(BaseIni.fetchAirLine.getFromcityjm());
			qft.setArrivecity(BaseIni.fetchAirLine.getTocity());
			qft.setArrivecityjm(BaseIni.fetchAirLine.getTocityjm());
			qft.setFetchdate(BaseIni.fetchAirLine.getFlydate());
			qft.setOpttime(new Date());

			// ����ʱ��
			String starttime = "", linename = "", flyno = "";
			// ��������airline-name /[a-zA-Z0-9]+/
			linename = businese.select("p.airline-name span").first().ownText();
			qft.setAireline(linename);
			//System.out.println(businese.select("p.airline-name span").first().ownText());
			// ���û������ר��
			if (qft.getAireline().trim().equals("����ר��")) {
				// ���Ϊ����ר�ߣ���ôû�к����
				// qft.setFltno("����");
				// qft.setAirtype("ר��");
				// qft.setStartairport("����");
				// qft.setArriveairport("����");
				// starttime = businese.select("div.a-tm-be").first().ownText();
			} else {
				Pattern p = Pattern.compile("[a-zA-Z0-9]+");
				Matcher match = p.matcher(linename);
				if (match.find()) {
					flyno = match.group();
				}
				// �õ������
				qft.setFltno(flyno);
				// �õ�����ʱ��
				starttime = businese.select("p.flight-time-deptime").first().ownText();
				// ��������
				qft.setAirtype(businese.select("div.pi-flightlogo-nl p").get(1).ownText());
				// ��������
				qft.setStartairport(businese.select("div.port-detail p.port-dep").first().ownText());
				// �������
				qft.setArriveairport(businese.select("div.port-detail p.port-arr").first().ownText());
			}

			// �õ�����Ʊ��
			Elements prcs = businese.select("span.J_FlightListPrice");
			if (prcs.size() > 0) {
				String fprice = prcs.first().ownText();
				try {
					qft.setTicketprice(Integer.parseInt(fprice) + BaseIni.fetchCitys.getRate());
				} catch (NumberFormatException e) {
					qft.setTicketprice(0);
				}
			} else {
				qft.setTicketprice(0);
			}
			if (qft.getTicketprice() == 0) {
				continue;
			}

			// û�й�����
			int shareflag = 0;
			Elements lnk_a = businese.select("div.pi-flightlogo-nl a.link-dot");
			if (lnk_a != null && lnk_a.size() > 0) {
				for (int i = 0; i < lnk_a.size(); i++) {
					String mmstr = lnk_a.get(i).ownText().trim();
					if (mmstr.equals("����")) {
						shareflag = 1;
						break;
					}
					if (mmstr.equals("��ͣ")) {
						shareflag = 2;
						break;
					}
				}
			}
			if (shareflag == 1) {
				qft.setShareline("����");
			} else if (shareflag == 2) {
				qft.setShareline("��ͣ");
			} else {
				qft.setShareline("����");
			}

			// ��λ����
			int flag = 0;
			Elements i_fst_cls = businese.select("td.flight-price");
			if (i_fst_cls.size() > 0) {
				i_fst_cls = i_fst_cls.select("span.discount");
				if (i_fst_cls != null && i_fst_cls.size() > 0) {
					for (int i = 0; i < i_fst_cls.size(); i++) {
						if (i_fst_cls.get(i).ownText().trim().equals("ͷ�Ȳ�")) {
							flag = 1;
							break;
						} else if (i_fst_cls.get(i).ownText().trim().equals("�����")) {
							flag = 2;
							break;
						}
					}
				}
			}
			if (flag == 1) {
				qft.setSitlevel("ͷ�Ȳ�");
			} else if (flag == 2) {
				qft.setSitlevel("�����");
			} else {
				qft.setSitlevel("���ò�");
			}

			// �������ת���࣬��ô�ڼ۸��ϼ���100
			Elements zz = businese.select("div.transfer-city");
			if (zz != null && zz.size() > 0) {
				Elements zn = zz.first().select("label.transfer-city-label");
				if (zn != null && zn.size() > 0) {
					if (zn != null && zn.first() != null) {
						String znstr = zn.first().ownText();
						if (znstr.trim().indexOf("��ת") != -1) {
							if (qft.getTicketprice() > 0) {
								qft.setTicketprice(qft.getTicketprice() + 100 + BaseIni.fetchCitys.getRate());
								qft.setLinetype("��ת");
							}
							// ȡ����ת����
							qft.setChangecity(znstr.trim().substring(znstr.trim().length() - 2));
							// ȡ����ת�����
							String fno = "";
							Elements fnos = businese.select("div.pi-flightlogo-nl-JD");
							if (fnos != null && fnos.size() > 0) {
								Elements fnos2 = fnos.first().select("p.airline-name");
								if (fnos2 != null && fnos2.size() > 0) {
									Elements fnos3 = fnos2.first().select("span.J_line");
									if (fnos3 != null && fnos3.size() > 0) {
										fno = fnos3.first().ownText();
									}
								}
							}
							Pattern p = Pattern.compile("[a-zA-Z0-9]+");
							Matcher match = p.matcher(fno);
							if (match.find()) {
								fno = match.group();
							}
							qft.setChangeflyno(fno);
						} else {
							qft.setLinetype("ֱ��");
						}
					} else {
						qft.setLinetype("ֱ��");
					}
				}
			} else {
				qft.setLinetype("ֱ��");
			}

			// ���㵽��ʱ�䣬����������ڴ��ڵ������ڣ���˵���ǵڶ��쵽��,�ڵ��������ϼ���һ��
			try {
				calendar.setTime(BaseIni.fetchAirLine.getFlydate());
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(starttime.split(":")[0]));
				calendar.set(Calendar.MINUTE, Integer.parseInt(starttime.split(":")[1]));
				qft.setStarttime(calendar.getTime());

				if (!qft.getFltno().trim().equals("����")) {
					String arrivetime = businese.select("span.s-time").first().ownText();
					calendar.setTime(BaseIni.fetchAirLine.getFlydate());
					if (compareString(starttime, arrivetime)) {
						calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
					}

					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrivetime.split(":")[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(arrivetime.split(":")[1]));
					qft.setArrivetime(calendar.getTime());
				}

			} catch (Exception e1) {
				System.out.println("ʱ���ʽ������" + e1.getMessage());
			}

			// �õ������׼���ʣ��еĺ������û�У����ԼӸ�������
			try {
				Elements es = businese.select("td.flight-ontime-rate p");
				if (es.size() > 0) {
					qft.setOffsetrate(es.first().ownText());
					qft.setOffsettime("0");
				}
			} catch (Exception e) {
				System.out.println("����׼����û�У�" + e.getMessage());
			}

			// ������Ʊ�㣬�еĿ���Ҳû��
			// Elements els = businese.select("p.rec-name");
			// if (els.size() > 0) {
			// qft.setAgent(els.first().text());
			// }
			BaseIni.rlist.add(qft);
		}
	}

	// �������ַ�����ʽ��ʱ����жԱ� [ʱ����]
	private boolean compareString(String starttime, String arrivetime) {
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

	// ���ϣ�05��00-11��29
	// ���磺11��30-14��29
	// ���磺14��30-18��29
	// ���ϣ�18��30-04��59
	// �Ժ������������ķ��࣬�����ǳ���ʱ�� [ʱ:��]
	private void sortAirLine() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		List<QlyFlyticket> tlist = BaseIni.rlist;
		List<QlyFlyticket> tml = new ArrayList<QlyFlyticket>();
		if (tlist != null && tlist.size() > 0) {
			for (QlyFlyticket qft : tlist) {
				String[] ss = sdf.format(qft.getStarttime()).split(":");
				// ���ʱ����ڵ��� �� ��С�ڵ��� 11����ô������
				if (Integer.parseInt(ss[0]) >= 5 && Integer.parseInt(ss[0]) <= 11) {
					if (Integer.parseInt(ss[0]) == 11) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("����");
						} else {
							qft.setTypetime("����");
						}
					} else {
						qft.setTypetime("����");
					}
					tml.add(qft);
					continue;
				}
				// ���ʱ����ڵ��� 11 ��С�ڵ��� 14����ô������
				if (Integer.parseInt(ss[0]) >= 11 && (Integer.parseInt(ss[0]) <= 14)) {
					if (Integer.parseInt(ss[0]) == 11) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("����");
						} else {// ����һ�㲻����ڣ���Ϊ��ǰ��һ���Ѿ��жϹ��ˣ���������磬��ô�Ͳ�����е���һ����������
							qft.setTypetime("����");
						}
					} else if (Integer.parseInt(ss[0]) == 14) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("����");
						} else {
							qft.setTypetime("����");
						}
					} else {
						qft.setTypetime("����");
					}
					tml.add(qft);
					continue;
				}
				// ���ʱ����ڵ��� 14 ��С�ڵ��� 18����ô������
				if (Integer.parseInt(ss[0]) >= 14 && Integer.parseInt(ss[0]) <= 18) {
					if (Integer.parseInt(ss[0]) == 14) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("����");
						} else {// ����һ�㲻����ڣ���Ϊ��ǰ��һ���Ѿ��жϹ��ˣ���������磬��ô�Ͳ�����е���һ����������
							qft.setTypetime("����");
						}
					} else if (Integer.parseInt(ss[0]) == 18) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("����");
						} else {
							qft.setTypetime("����");
						}
					} else {
						qft.setTypetime("����");
					}
					tml.add(qft);
					continue;
				}
				// ���ʱ����ڵ��� 18 ���� С�ڵ��� 5����ô������
				if (Integer.parseInt(ss[0]) >= 18 || Integer.parseInt(ss[0]) < 5) {
					if (Integer.parseInt(ss[0]) == 18) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("����");
						} else {
							qft.setTypetime("����");
						}
					} else {
						qft.setTypetime("����");
					}
					tml.add(qft);
				}
			}
		}
		BaseIni.rlist.clear();
		BaseIni.rlist.addAll(tml);
	}
}
