package com.bohc.deal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.bohc.bean.action.QlyFlyTicketAction;
import com.bohc.exception.MyParseException;
import com.bohc.parsehtml.QunarPrice;
import com.bohc.sh.entities.QlyFlyticket;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

public class CheckHtmlIsLoad {
	static CheckHtmlIsLoad chl;

	static {
		if (chl == null) {
			chl = new CheckHtmlIsLoad();
		}
	}

	public static CheckHtmlIsLoad instance() {
		return chl;
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

	public void dealHtml(Browser browser) {
		try {
			if (BaseIni.fetchCitys.getCururl() == 1) {
				loadAllDate(browser);
			} else {
				ParseHtmlAliy.instance().loadAllDate(browser);
			}
		} catch (MyParseException e) {
			JxBrowserDemo.jd.updateUi(BaseIni.browserListenerCount + "====" + e.getMessage());
			BaseIni.changeCount(false);
		}
	}

	private void loadAllDate(Browser browser) throws MyParseException {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		DOMElement doc = null;
		String content = null;
		doc = parseAllDataByClassName(browser, "mb-10");
		if (doc != null) {
			content = doc.getInnerHTML();
		}
		if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {
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
				filterLine();// �������õĹ�����������������ţ�ͷ�Ȳգ�������
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

	// ��鵱ǰҳ��Ĺ����������ļ��� 0 ���� ��1 ���磬2 ���磬3 ����
	private void checkTime(Browser browser) throws MyParseException {
		DOMElement doc = null;
		String content = null;
		f = false;// ��
		n = false;// ��
		an = false;// ����
		ev = false;// ����
		long oldtime = System.currentTimeMillis();
		while (true) {
			doc = parseAllData(browser, "hdivfilterPanel");
			if (doc != null) {
				doc = doc.findElement(By.id("���ʱ��XI2"));
				if (doc != null) {
					content = doc.getInnerHTML();
					Document document = Jsoup.parse(content);
					Elements es = document.getElementsByAttributeValue("type", "checkbox");
					if (es.size() > 0) {
						break;
					}
				}
				if ((System.currentTimeMillis() - oldtime) / 1000 > 10) {
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doc = parseAllData(browser, "hdivfilterPanel");
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
			if (!f && !n && !an && !ev) {
				timeflag = true;
			}
		}
	}

	// �������磬���磬���磬���ϵ�����ץȡҳ�����ݣ�ִ�е��� js ҳ�����
	private void parseHtml(Browser browser) throws MyParseException {
		if (BaseIni.fetchCitys.isCkmorning() && f) {
			execJsFilterTime(browser, 0, "����");
		}
		if (BaseIni.fetchCitys.isCknoon() && n) {
			execJsFilterTime(browser, 1, "����");
		}
		if (BaseIni.fetchCitys.isCkanoon() && an) {
			execJsFilterTime(browser, 2, "����");
		}
		if (BaseIni.fetchCitys.isCkevening() && ev) {
			execJsFilterTime(browser, 3, "����");
		}
		if (timeflag) {// ����磬�У��£���һ����û��ѡ����ôȡ��ǰҳ������к���
			fetchAllLine(parseAllDataByClassName(browser, "mb-10"));
		}
	}

	private void parseHtmlNoFilterTime(Browser browser) throws MyParseException {
		// ����磬�У��£���һ����û��ѡ����ôȡ��ǰҳ������к���
		fetchAllLine(parseAllDataByClassName(browser, "mb-10"));
	}

	// ִ�� js �������������
	private void execJsFilterTime(Browser browser, int v, String vtime) throws MyParseException {
		// �ѵ�ǰ�õ��Ļ�Ʊҳ�����ݽ��н������õ���Ҫ������
		StringBuffer js = new StringBuffer();
		js.setLength(0);
		js.append("var cs = document.getElementsByTagName('input');");
		js.append("for(var i=0; i<cs.length; i++){if(cs[i].type=='checkbox'){if(cs[i].value=='" + v + "'){ cs[i].click(); }}}");
		execJs(browser, js.toString());// ѡ��

		int i = 0;// ��ʱ����
		while (true) {
			DOMElement doc = parseAllData(browser, "filterResultXI2");
			if (doc != null) {
				Document d = Jsoup.parse(doc.getInnerHTML());
				Elements es = d.select("span.result-text");
				if (es != null && es.first() != null && es.first().ownText().trim().equals("���ʱ�䣺" + vtime)) {
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
					break;
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			i++;
			if (i == 10) {
				break;
			}
		}

		// ȡ������
		fetchAllLine(parseAllDataByClassName(browser, "mb-10"));

		js.setLength(0);
		js.append("var cs = document.getElementsByTagName('input');");
		js.append("for(var i=0; i<cs.length; i++){if(cs[i].type=='checkbox'){if(cs[i].value=='" + v + "'){ cs[i].checked=true; cs[i].click(); }}}");
		execJs(browser, js.toString());// ȡ��
	}

	// ͳһ��ִ�� js ����������Ƿ��ص�ͳһ�ĵط�����
	private void execJs(Browser browser, String js) throws MyParseException {
		try {
			browser.executeJavaScript(js);// ִ��js
		} catch (Exception e) {
			throwMyException("ҳ��ִ��js�쳣");
		}
	}

	// ��ID��ȡ
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

	// ������ȡ
	private DOMElement parseAllDataByClassName(Browser browser, String cname) throws MyParseException {
		// ȡ������
		DOMElement doc = null;
		try {
			long oldtime = System.currentTimeMillis();
			while (true) {
				doc = browser.getDocument().findElement(By.className(cname));
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

	// ��Ҫץȡ�ĵڶ�������ѹ������ջ
	private void dealSecondList() {
		List<QlyFlyticket> rlist = BaseIni.rlist;
		if (rlist != null && rlist.size() > 0) {
			for (int i = 0; i < rlist.size(); i++) {
				QlyFlyticket qft = (QlyFlyticket) rlist.get(i);
				if (qft.getChangeflyno() != null && qft.getChangeflyno().length() > 4) {
					JxBrowserDemo.jd.updateUi(qft.getFltno() + "(" + qft.getChangeflyno() + ")\t��ת���࣬��ץȡ�ڶ���");
					continue;
				}
				FetchAirLine fal = new FetchAirLine();
				fal.setFromcity(BaseIni.fetchAirLine.getFromcity());
				fal.setFromcityjm(BaseIni.fetchAirLine.getFromcityjm());
				fal.setTocity(BaseIni.fetchAirLine.getTocity());
				fal.setTocityjm(BaseIni.fetchAirLine.getTocityjm());
				fal.setFlydate(BaseIni.fetchAirLine.getFlydate());
				fal.setLevel(1);
				fal.setQft(qft);

				String gurl = "http://flight.qunar.com";
				Calendar cal = Calendar.getInstance();
				cal.setTime(qft.getStarttime());
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String sb = qft.getFltno() + "|" + sdf.format(qft.getStarttime());
				String url = gurl + "/site/oneway_detail.htm?&origional=" + sb.toString() + "&searchDepartureAirport=" + BaseIni.fetchAirLine.getFromcity() + "&searchArrivalAirport=" + BaseIni.fetchAirLine.getTocity()
						+ "&searchDepartureTime=" + sdf.format(qft.getStarttime()) + "&searchArrivalTime=" + sdf.format(cal.getTime()) + "&nextNDays=0&arrivalTime=" + sdf.format(cal.getTime()) + "&code=" + qft.getFltno()
						+ "&listlp=" + qft.getTicketprice() + "&sortid=&deptcity=" + BaseIni.fetchAirLine.getFromcity() + "&arricity=" + BaseIni.fetchAirLine.getTocity()
						+ "&tserver=union.corp.qunar.com&wtype=all&lowflight=true&lowflightpr=" + qft.getTicketprice() + "&from=fi_re_search";
				fal.setUrl(url);

				BaseIni.satckAirline.push(fal);
			}
		}
	}

	// ץȡ���б�ҳ��ĺ���
	private void fetchAllLine(DOMElement doc) {
		if (doc == null)
			return;
		Document document = Jsoup.parse(doc.getInnerHTML());
		// �õ����а������ߵ� div Ԫ��
		Elements businesses = document.select("div.b-airfly");
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
			String starttime = "";
			// System.out.println(businese.html());
			// ��������
			String airtitle = businese.select("div.col-airline div.air span").first().ownText();
			qft.setAireline(airtitle);
			if (qft.getAireline().trim().equals("����ר��")) {
				// ���Ϊ����ר�ߣ���ôû�к����
				qft.setFltno("����");
				qft.setAirtype("ר��");
				qft.setStartairport("����");
				qft.setArriveairport("����");
				starttime = businese.select("div.a-tm-be").first().ownText();
				Pattern p = Pattern.compile("[0-9]?[0-9]:[0-9]?[0-9]");
				Matcher match = p.matcher(starttime);
				if (match.find()) {
					starttime = match.group();
				}
			} else {
				// �õ������
				qft.setFltno(businese.select("div.col-airline div.num span").first().ownText());
				// �õ�����ʱ��
				starttime = businese.select("div.col-time div.sep-lf h2").first().ownText();
				// ��������
				qft.setAirtype(businese.select("div.col-airline div.num span").get(1).ownText());
				// ��������
				qft.setStartairport(businese.select("div.col-time div.sep-lf span").first().ownText());
				// �������
				qft.setArriveairport(businese.select("div.col-time div.sep-rt span").first().ownText());
			}

			// �õ�����Ʊ��
			Elements prcs = businese.select("div.col-price p.prc span.prc_wp");
			if (prcs != null && prcs.first() != null) {
				String fprice = getPriceNew(prcs.first());
//				System.out.println(qft.getFltno() + "\t" + fprice + "\t" + prcs.first().html());
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

			// ȡ������
			Elements lnk_a = businese.select("div.col-airline div.num span.g-tips span");
			if (lnk_a != null && lnk_a.first() != null && lnk_a.first().ownText().trim().equals("����")) {
				qft.setShareline("����");
			} else {
				qft.setShareline("����");
			}

			// ȡͷ�Ȳ�
			int flag = 0;
			Elements i_fst_cls = businese.select("div.col-airline div.vim span.v");
			if (i_fst_cls != null && i_fst_cls.first() != null) {
				if (i_fst_cls.get(0).ownText().trim().equals("ͷ�Ȳ�")) {
					flag = 1;
				} else if (i_fst_cls.get(0).ownText().trim().equals("�����")) {
					flag = 2;
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
			Elements zz = businese.select("div.col-time div.trans div.g-tips span.t span");
			if (zz != null && zz.first() != null) {
				if (zz.first().ownText().trim().equals("ͣ")) {
					qft.setLinetype("��ͣ");
					qft.setChangecity(zz.get(1).ownText());
				} else if (zz.first().ownText().trim().equals("ת")) {
					if (qft.getTicketprice() > 0) {
						qft.setTicketprice(qft.getTicketprice() + 100 + BaseIni.fetchCitys.getRate());
					}
					qft.setLinetype("��ת");
					qft.setChangecity(zz.get(1).ownText());
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
					String arrivetime = businese.select("div.col-time div.sep-rt h2").first().ownText();
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
				// Elements es = businese.select("p.a-pty-mint");
				// if (es.size() > 0) {
				// qft.setOffsetrate(es.first().ownText());
				// qft.setOffsettime(es.last().ownText());
				// }
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

	// �����ڶ��������,ֱ�ﺽ��
	private void parseSencondDataDirect(Browser browser) throws MyParseException {
		DOMElement doc = null;
		String content = null;
		doc = parseAllData(browser, "hdivResultPanel");
		if (doc != null) {
			content = doc.getInnerHTML();
		}
		if (content != null && content.indexOf("���Ե�,����ѯ�Ľ������ʵʱ������") == -1) {
			BaseIni.fetchAirLine.setFetching(true);// �����Ѿ�ȡ����ҳ������
			try {// �ȴ�һ�룬��ҳ�����ݼ������
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			List<Integer> tlist = new ArrayList<Integer>();
			qp.getTicketSecond(parseAllData(browser, "hdivResultPanel"), BaseIni.fetchAirLine, tlist);
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
				List<QlyFlyticket> rlist = BaseIni.rlist;
				for (int i = 0; i < rlist.size(); i++) {
					QlyFlyticket t = rlist.get(i);
					if (BaseIni.fetchAirLine.getQft().equals(t)) {
						BaseIni.rlist.get(i).setAvgticketprice(avgprice);
					}
				}
			}

			if (tlist.size() > 4) {
				JxBrowserDemo.jd.updateUi("�ڶ������ݣ�" + BaseIni.fetchAirLine.getQft().getFltno() + Arrays.asList(tlist.subList(0, 4)));
			} else {
				JxBrowserDemo.jd.updateUi("�ڶ������ݣ�" + BaseIni.fetchAirLine.getQft().getFltno() + Arrays.asList(tlist));
			}
			BaseIni.changeCount(false);
		}
	}

	public void saveData(int i, FetchAirLine fetchaireline) {
		if (fetchaireline == null)
			return;
		// ֻ�е�һ�����������ץȡ��ɣ��Ž�����һ��,ץ����ɵı�ʶ��
		List<QlyFlyticket> rlist = BaseIni.rlist;
		List<String> flist = BaseIni.flist;
		if (rlist != null && rlist.size() > 0) {
			String smsg = fetchaireline.getFromcity() + "����" + fetchaireline.getTocity() + "\t" + sdf.format(fetchaireline.getFlydate()) + ": ��" + (i + 1) + "�����ݣ�" + "\t";
			smsg += "������====" + BaseIni.rlist.size() + "\r\n";
			StringBuffer detaildata = new StringBuffer();
			detaildata.append("\t�����\t");
			detaildata.append("\t��ͼ�");
			detaildata.append("\t����");
			detaildata.append("\t����ʱ��\t\t");
			detaildata.append("\t����");
			detaildata.append("\t��λ");
			detaildata.append("\t����");
			detaildata.append("\t����");
			detaildata.append("\t���չ�˾");
			detaildata.append("\r\n");
			detaildata.append("\t");
			for (int c = 0; c < 88; c++) {
				detaildata.append("-");
			}
			detaildata.append("\r\n");

			StringBuffer sbf = new StringBuffer();
			sbf.append("insert into flytickettemp ");
			sbf.append(
					"(fltno,aireline,airtype,startairport,arriveairport,startcity,startcityjm,arrivecity,arrivecityjm,starttime,arrivetime,offsetrate,offsettime,ticketprice,avgticketprice,agent,fetchdate,opttime,typetime,changeflyno,changecity) ");
			sbf.append("values ");
			for (QlyFlyticket qft : rlist) {
				String fno = qft.getFltno();
				if (qft.getChangeflyno() != null && qft.getChangeflyno().length() > 4) {
					fno += "(" + qft.getChangeflyno() + ")";
				} else if (qft.getFltno().indexOf("/") != -1) {
					// ˫����
				} else {
					fno += "\t";
				}
				detaildata.append("\t" + fno);
				detaildata.append("\t" + qft.getTicketprice());
				detaildata.append("\t" + qft.getAvgticketprice());
				detaildata.append("\t" + df.format(qft.getStarttime()));
				detaildata.append("\t" + qft.getTypetime());
				detaildata.append("\t" + qft.getSitlevel());
				detaildata.append("\t" + qft.getShareline());
				detaildata.append("\t" + qft.getLinetype());
				detaildata.append("\t" + qft.getAireline());
				detaildata.append("\r\n");
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

			smsg += detaildata.toString();

			if (flist != null && flist.size() > 0) {
				smsg += "���������˵����ݣ�\r\n";
				StringBuffer filterdata = new StringBuffer();
				int f = 0;
				for (String msg : flist) {
					f++;
					filterdata.append("\t" + msg);
					if ((f != 0 && f % 3 == 0) || f == (flist.size() - 1)) {
						filterdata.append("\r\n");
					}
				}
				smsg += filterdata;
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

		BaseIni.rlist.clear();
		BaseIni.flist.clear();
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
		for (int i = 0; i < (Integer.parseInt(pos) / 18); i++) {
			int p = Integer.parseInt(pos) - (i * 18);
			e = prc.getElementsByAttributeValue("style", "left:-" + p + "px");
			if (e.size() > 0) {
				businesse_prc = businesse_prc.substring(0, i) + e.text() + businesse_prc.substring(i + 1);
			}
		}

		return businesse_prc;
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
