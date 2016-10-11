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

	// 查找时间选择框有哪几个
	private boolean f = false, n = false, an = false, ev = false;// 早，中，下午，晚上
	private boolean timeflag = false;// 判断有没有时间选择，如果没有，那不做过滤
	private QunarPrice qp = new QunarPrice();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private Stack<QlyFlyticket> zdStackMoning = new Stack<QlyFlyticket>();// 早上直达航班
	private Stack<QlyFlyticket> zdStackNoon = new Stack<QlyFlyticket>();// 中午直达航班
	private Stack<QlyFlyticket> zdStackAnoon = new Stack<QlyFlyticket>();// 下午直达航班
	private Stack<QlyFlyticket> zdStackEvening = new Stack<QlyFlyticket>();// 晚上直达航班
	private Stack<QlyFlyticket> zzStackMoning = new Stack<QlyFlyticket>();// 早上中转航班
	private Stack<QlyFlyticket> zzStackNoon = new Stack<QlyFlyticket>();// 中午中转航班
	private Stack<QlyFlyticket> zzStackAnoon = new Stack<QlyFlyticket>();// 下午中转航班
	private Stack<QlyFlyticket> zzStackEvening = new Stack<QlyFlyticket>();// 晚上中转航班

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
		if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {
			StringBuffer js = new StringBuffer();
			js.append("window.scrollTo(0,document.body.scrollHeight); ");
			js.append("setTimeout(\"var mydiv=document.createElement('div');mydiv.setAttribute('id','bhc_flag');document.body.appendChild(mydiv);\", 5000 );");
			execJs(browser, js.toString());
			// 判断有没有执行成功
			isLoadFinish(browser);
			js.setLength(0);
			js.append("window.scrollTo(0,0); ");
			execJs(browser, js.toString());

			BaseIni.fetchAirLine.setFetching(true);
			try {
				// 在这儿停顿为了让页面回到顶
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (BaseIni.fetchAirLine.getLevel() == 0) {// 处理第一层数据
				// checkTime(browser);// 检查时间选择有哪几个选项
				// parseHtml(browser);// 根据时间选项，取出航班数据
				parseHtmlNoFilterTime(browser);
				filterLine();// 界面设置的过滤条件，两个航班号，头等舱，共享航班
				sortAirLine();// 对航线进行早中下晚分类
				typeAirLine();// 把抓取到的数据进行优先取值分类
				sortFetchTicket();// 过滤抓取到的数据，按价格低到高取前四条航线，分早中下晚
				clearStack();// 清空堆栈数据
				// 第一层数据抓取完成后，判断有没有设置抓取第二层数据。
				if (BaseIni.fetchCitys.isFetchnext() && BaseIni.fetchAirLine.getLevel() == 0) {
					// 如果设置了要抓取第二层数据，那么继续执行抓取第二层数据的方法
					dealSecondList();
				}
				// 如果没有设置要抓取第二层数据，那么执行插入操作,并且把执行中的任务数减一
				BaseIni.changeCount(false);
			} else if (BaseIni.fetchAirLine.getLevel() == 1) {// 第二层直达航班
				parseSencondDataDirect(browser);
			}
		}
	}

	// 判断页面 js 是否执行完成
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

	// 检查当前页面的过滤条件有哪几项 0 早上 ，1 中午，2 下午，3 晚上
	private void checkTime(Browser browser) throws MyParseException {
		DOMElement doc = null;
		String content = null;
		f = false;// 早
		n = false;// 中
		an = false;// 下午
		ev = false;// 晚上
		long oldtime = System.currentTimeMillis();
		while (true) {
			doc = parseAllData(browser, "hdivfilterPanel");
			if (doc != null) {
				doc = doc.findElement(By.id("起飞时间XI2"));
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

	// 根据上午，中午，下午，晚上的条件抓取页面数据，执行的是 js 页面过滤
	private void parseHtml(Browser browser) throws MyParseException {
		if (BaseIni.fetchCitys.isCkmorning() && f) {
			execJsFilterTime(browser, 0, "上午");
		}
		if (BaseIni.fetchCitys.isCknoon() && n) {
			execJsFilterTime(browser, 1, "中午");
		}
		if (BaseIni.fetchCitys.isCkanoon() && an) {
			execJsFilterTime(browser, 2, "下午");
		}
		if (BaseIni.fetchCitys.isCkevening() && ev) {
			execJsFilterTime(browser, 3, "晚上");
		}
		if (timeflag) {// 如果早，中，下，晚一个都没有选，那么取当前页面的所有航班
			fetchAllLine(parseAllDataByClassName(browser, "mb-10"));
		}
	}

	private void parseHtmlNoFilterTime(Browser browser) throws MyParseException {
		// 如果早，中，下，晚一个都没有选，那么取当前页面的所有航班
		fetchAllLine(parseAllDataByClassName(browser, "mb-10"));
	}

	// 执行 js 按航班过滤数据
	private void execJsFilterTime(Browser browser, int v, String vtime) throws MyParseException {
		// 把当前得到的机票页面内容进行解析，得到想要的数据
		StringBuffer js = new StringBuffer();
		js.setLength(0);
		js.append("var cs = document.getElementsByTagName('input');");
		js.append("for(var i=0; i<cs.length; i++){if(cs[i].type=='checkbox'){if(cs[i].value=='" + v + "'){ cs[i].click(); }}}");
		execJs(browser, js.toString());// 选中

		int i = 0;// 超时设置
		while (true) {
			DOMElement doc = parseAllData(browser, "filterResultXI2");
			if (doc != null) {
				Document d = Jsoup.parse(doc.getInnerHTML());
				Elements es = d.select("span.result-text");
				if (es != null && es.first() != null && es.first().ownText().trim().equals("起飞时间：" + vtime)) {
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

		// 取出数据
		fetchAllLine(parseAllDataByClassName(browser, "mb-10"));

		js.setLength(0);
		js.append("var cs = document.getElementsByTagName('input');");
		js.append("for(var i=0; i<cs.length; i++){if(cs[i].type=='checkbox'){if(cs[i].value=='" + v + "'){ cs[i].checked=true; cs[i].click(); }}}");
		execJs(browser, js.toString());// 取消
	}

	// 统一的执行 js ，如果出错，那返回到统一的地方处理
	private void execJs(Browser browser, String js) throws MyParseException {
		try {
			browser.executeJavaScript(js);// 执行js
		} catch (Exception e) {
			throwMyException("页面执行js异常");
		}
	}

	// 按ID来取
	private DOMElement parseAllData(Browser browser, String vid) throws MyParseException {
		// 取出数据
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
			throwMyException("页面解析异常");
		}
		return doc;
	}

	// 按类名取
	private DOMElement parseAllDataByClassName(Browser browser, String cname) throws MyParseException {
		// 取出数据
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
			throwMyException("页面解析异常");
		}
		return doc;
	}

	private void throwMyException(String msg) throws MyParseException {
		// 如果是第一层数据加载失败，那么清除本条线路之前抓取成功的数据，重新抓取
		if (BaseIni.fetchAirLine.getLevel() == 0) {
			BaseIni.rlist.clear();
			BaseIni.flist.clear();
		}
		// 反这条抓取失败的数据重置状态，压回数据栈重新抓取
		FetchAirLine fal = BaseIni.fetchAirLine;
		fal.setFetching(false);
		BaseIni.fetchFailList.add(fal);
		throw new MyParseException(msg);
	}

	// 界面设置的过滤条件，两个航班号，头等舱，共享航班
	private void filterLine() {
		List<QlyFlyticket> rlist = BaseIni.rlist;
		if (rlist == null || rlist.size() == 0) {
			return;
		}
		for (Iterator<QlyFlyticket> it = rlist.iterator(); it.hasNext();) {
			QlyFlyticket qft = it.next();
			// 过滤共享航班
			if (BaseIni.fetchCitys.isCkgxhb()) {
				if (qft.getShareline().equals("共享")) {
					it.remove();
					continue;
				}
			}
			// 过滤中转航班
			if (BaseIni.fetchCitys.isCk_zzhb()) {
				if (qft.getLinetype().equals("中转")) {
					it.remove();
					continue;
				}
			}
			// 过滤双航班号
			if (BaseIni.fetchCitys.isCkdhb()) {
				if (qft.getFltno().indexOf("/") != -1) {
					it.remove();
					continue;
				}
			}
			// 过滤头等舱
			if (BaseIni.fetchCitys.isCktdc()) {
				if (qft.getSitlevel().equals("头等舱")) {
					it.remove();
					continue;
				}
			}
			// 过滤早上航班
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("早上")) {
					it.remove();
					continue;
				}
			}
			// 过滤中午航班
			if (!BaseIni.fetchCitys.isCknoon()) {
				if (qft.getTypetime().equals("中午")) {
					it.remove();
					continue;
				}
			}
			// 过滤下午航班
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("下午")) {
					it.remove();
					continue;
				}
			}
			// 过滤晚上航班
			if (!BaseIni.fetchCitys.isCkmorning()) {
				if (qft.getTypetime().equals("晚上")) {
					it.remove();
					continue;
				}
			}
			// 过滤价格为 0 的航班
			if (!BaseIni.fetchCitys.isCkp0()) {
				if (qft.getTicketprice() == 0) {
					it.remove();
					continue;
				}
			}
		}
	}

	// 把抓取到的数据进行优先取值分类，根据需要的过滤条件，把数据分配到不同的堆栈当中，并且按价格顺序从高到低的顺序压入
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
			if (qft.getLinetype().equals("直达") || qft.getLinetype().equals("经停")) {
				if (qft.getTypetime().equals("上午")) {
					zdStackMoning.push(qft);
				} else if (qft.getTypetime().equals("中午")) {
					zdStackNoon.push(qft);
				}
				if (qft.getTypetime().equals("下午")) {
					zdStackAnoon.push(qft);
				}
				if (qft.getTypetime().equals("晚上")) {
					zdStackEvening.push(qft);
				}
			} else if (qft.getLinetype().equals("中转")) {
				if (qft.getTypetime().equals("上午")) {
					zzStackMoning.push(qft);
				} else if (qft.getTypetime().equals("中午")) {
					zzStackNoon.push(qft);
				}
				if (qft.getTypetime().equals("下午")) {
					zzStackAnoon.push(qft);
				}
				if (qft.getTypetime().equals("晚上")) {
					zzStackEvening.push(qft);
				}
			}
		}
	}

	// 把抓取到的数据时行再一次的排序过滤
	private void sortFetchTicket() {
		// 重新选择抓取的数据
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
		// 获取直达的和经停的
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
		// 数据不够，那么再取中转的
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

	// 清空所有的堆栈数据
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

	// 把要抓取的第二层数据压入数据栈
	private void dealSecondList() {
		List<QlyFlyticket> rlist = BaseIni.rlist;
		if (rlist != null && rlist.size() > 0) {
			for (int i = 0; i < rlist.size(); i++) {
				QlyFlyticket qft = (QlyFlyticket) rlist.get(i);
				if (qft.getChangeflyno() != null && qft.getChangeflyno().length() > 4) {
					JxBrowserDemo.jd.updateUi(qft.getFltno() + "(" + qft.getChangeflyno() + ")\t中转航班，不抓取第二层");
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

	// 抓取所有本页面的航线
	private void fetchAllLine(DOMElement doc) {
		if (doc == null)
			return;
		Document document = Jsoup.parse(doc.getInnerHTML());
		// 得到所有包含航线的 div 元素
		Elements businesses = document.select("div.b-airfly");
		int size = businesses.size();

		Calendar calendar = Calendar.getInstance();
		int j = 0;
		// 只有航线大于 0 才解析
		for (j = 0; j < size; j++) {
			Element businese = businesses.get(j);

			QlyFlyticket qft = new QlyFlyticket();

			qft.setStartcity(BaseIni.fetchAirLine.getFromcity());
			qft.setStartcityjm(BaseIni.fetchAirLine.getFromcityjm());
			qft.setArrivecity(BaseIni.fetchAirLine.getTocity());
			qft.setArrivecityjm(BaseIni.fetchAirLine.getTocityjm());
			qft.setFetchdate(BaseIni.fetchAirLine.getFlydate());
			qft.setOpttime(new Date());
			// 出发时间
			String starttime = "";
			// System.out.println(businese.html());
			// 航线名称
			String airtitle = businese.select("div.col-airline div.air span").first().ownText();
			qft.setAireline(airtitle);
			if (qft.getAireline().trim().equals("旅行专线")) {
				// 如果为旅行专线，那么没有航班号
				qft.setFltno("待定");
				qft.setAirtype("专线");
				qft.setStartairport("待定");
				qft.setArriveairport("待定");
				starttime = businese.select("div.a-tm-be").first().ownText();
				Pattern p = Pattern.compile("[0-9]?[0-9]:[0-9]?[0-9]");
				Matcher match = p.matcher(starttime);
				if (match.find()) {
					starttime = match.group();
				}
			} else {
				// 得到航班号
				qft.setFltno(businese.select("div.col-airline div.num span").first().ownText());
				// 得到发出时间
				starttime = businese.select("div.col-time div.sep-lf h2").first().ownText();
				// 航班类型
				qft.setAirtype(businese.select("div.col-airline div.num span").get(1).ownText());
				// 出发机场
				qft.setStartairport(businese.select("div.col-time div.sep-lf span").first().ownText());
				// 到达机场
				qft.setArriveairport(businese.select("div.col-time div.sep-rt span").first().ownText());
			}

			// 得到航线票价
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

			// 取共享航班
			Elements lnk_a = businese.select("div.col-airline div.num span.g-tips span");
			if (lnk_a != null && lnk_a.first() != null && lnk_a.first().ownText().trim().equals("共享")) {
				qft.setShareline("共享");
			} else {
				qft.setShareline("正常");
			}

			// 取头等舱
			int flag = 0;
			Elements i_fst_cls = businese.select("div.col-airline div.vim span.v");
			if (i_fst_cls != null && i_fst_cls.first() != null) {
				if (i_fst_cls.get(0).ownText().trim().equals("头等舱")) {
					flag = 1;
				} else if (i_fst_cls.get(0).ownText().trim().equals("商务舱")) {
					flag = 2;
				}
			}
			if (flag == 1) {
				qft.setSitlevel("头等舱");
			} else if (flag == 2) {
				qft.setSitlevel("商务舱");
			} else {
				qft.setSitlevel("经济舱");
			}

			// 如果是中转航班，那么在价格上加上100
			Elements zz = businese.select("div.col-time div.trans div.g-tips span.t span");
			if (zz != null && zz.first() != null) {
				if (zz.first().ownText().trim().equals("停")) {
					qft.setLinetype("经停");
					qft.setChangecity(zz.get(1).ownText());
				} else if (zz.first().ownText().trim().equals("转")) {
					if (qft.getTicketprice() > 0) {
						qft.setTicketprice(qft.getTicketprice() + 100 + BaseIni.fetchCitys.getRate());
					}
					qft.setLinetype("中转");
					qft.setChangecity(zz.get(1).ownText());
				}
			} else {
				qft.setLinetype("直达");
			}

			// 计算到达时间，如果出发日期大于到达日期，那说明是第二天到达,在到达日期上加上一天
			try {
				calendar.setTime(BaseIni.fetchAirLine.getFlydate());
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(starttime.split(":")[0]));
				calendar.set(Calendar.MINUTE, Integer.parseInt(starttime.split(":")[1]));
				qft.setStarttime(calendar.getTime());

				if (!qft.getFltno().trim().equals("待定")) {
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
				System.out.println("时间格式化出错：" + e1.getMessage());
			}

			// 得到航班的准点率，有的航班可能没有，所以加个出错处理
			try {
				// Elements es = businese.select("p.a-pty-mint");
				// if (es.size() > 0) {
				// qft.setOffsetrate(es.first().ownText());
				// qft.setOffsettime(es.last().ownText());
				// }
			} catch (Exception e) {
				System.out.println("航班准点率没有：" + e.getMessage());
			}

			// 代理售票点，有的可能也没有
			// Elements els = businese.select("p.rec-name");
			// if (els.size() > 0) {
			// qft.setAgent(els.first().text());
			// }
			BaseIni.rlist.add(qft);
		}
	}

	// 解析第二层的数据,直达航班
	private void parseSencondDataDirect(Browser browser) throws MyParseException {
		DOMElement doc = null;
		String content = null;
		doc = parseAllData(browser, "hdivResultPanel");
		if (doc != null) {
			content = doc.getInnerHTML();
		}
		if (content != null && content.indexOf("请稍等,您查询的结果正在实时搜索中") == -1) {
			BaseIni.fetchAirLine.setFetching(true);// 设置已经取到了页面内容
			try {// 等待一秒，让页面数据加载完成
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
				JxBrowserDemo.jd.updateUi("第二层数据：" + BaseIni.fetchAirLine.getQft().getFltno() + Arrays.asList(tlist.subList(0, 4)));
			} else {
				JxBrowserDemo.jd.updateUi("第二层数据：" + BaseIni.fetchAirLine.getQft().getFltno() + Arrays.asList(tlist));
			}
			BaseIni.changeCount(false);
		}
	}

	public void saveData(int i, FetchAirLine fetchaireline) {
		if (fetchaireline == null)
			return;
		// 只有第一层的整条数据抓取完成，才进行下一条,抓到完成的标识是
		List<QlyFlyticket> rlist = BaseIni.rlist;
		List<String> flist = BaseIni.flist;
		if (rlist != null && rlist.size() > 0) {
			String smsg = fetchaireline.getFromcity() + "－－" + fetchaireline.getTocity() + "\t" + sdf.format(fetchaireline.getFlydate()) + ": 第" + (i + 1) + "条数据：" + "\t";
			smsg += "总条数====" + BaseIni.rlist.size() + "\r\n";
			StringBuffer detaildata = new StringBuffer();
			detaildata.append("\t航班号\t");
			detaildata.append("\t最低价");
			detaildata.append("\t均价");
			detaildata.append("\t出发时间\t\t");
			detaildata.append("\t分类");
			detaildata.append("\t座位");
			detaildata.append("\t共享");
			detaildata.append("\t飞行");
			detaildata.append("\t航空公司");
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
					// 双航班
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
				smsg += "被条件过滤的数据：\r\n";
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

		BaseIni.rlist.clear();
		BaseIni.flist.clear();
	}

	/**
	 * 获取机票价格，该方法会将去哪儿网价格调整为正确价格
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

		// 纠正价格信息
		for (int i = 0; i < (Integer.parseInt(pos) / 18); i++) {
			int p = Integer.parseInt(pos) - (i * 18);
			e = prc.getElementsByAttributeValue("style", "left:-" + p + "px");
			if (e.size() > 0) {
				businesse_prc = businesse_prc.substring(0, i) + e.text() + businesse_prc.substring(i + 1);
			}
		}

		return businesse_prc;
	}

	// 对两个字符串型式的时间进行对比 [时：分]
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

	// 早上：05：00-11：29
	// 中午：11：30-14：29
	// 下午：14：30-18：29
	// 晚上：18：30-04：59
	// 对航班进行早中晚的分类，根据是出发时间 [时:分]
	private void sortAirLine() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		List<QlyFlyticket> tlist = BaseIni.rlist;
		List<QlyFlyticket> tml = new ArrayList<QlyFlyticket>();
		if (tlist != null && tlist.size() > 0) {
			for (QlyFlyticket qft : tlist) {
				String[] ss = sdf.format(qft.getStarttime()).split(":");
				// 如果时间大于等于 ５ 而小于等于 11，那么是早上
				if (Integer.parseInt(ss[0]) >= 5 && Integer.parseInt(ss[0]) <= 11) {
					if (Integer.parseInt(ss[0]) == 11) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("上午");
						} else {
							qft.setTypetime("中午");
						}
					} else {
						qft.setTypetime("上午");
					}
					tml.add(qft);
					continue;
				}
				// 如果时间大于等于 11 而小于等于 14，那么是中午
				if (Integer.parseInt(ss[0]) >= 11 && (Integer.parseInt(ss[0]) <= 14)) {
					if (Integer.parseInt(ss[0]) == 11) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("中午");
						} else {// 这种一般不会存在，因为在前面一个已经判断过了，如果是上午，那么就不会进行到这一个方法里面
							qft.setTypetime("上午");
						}
					} else if (Integer.parseInt(ss[0]) == 14) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("中午");
						} else {
							qft.setTypetime("下午");
						}
					} else {
						qft.setTypetime("中午");
					}
					tml.add(qft);
					continue;
				}
				// 如果时间大于等于 14 而小于等于 18，那么是下午
				if (Integer.parseInt(ss[0]) >= 14 && Integer.parseInt(ss[0]) <= 18) {
					if (Integer.parseInt(ss[0]) == 14) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("下午");
						} else {// 这种一般不会存在，因为在前面一个已经判断过了，如果是中午，那么就不会进行到这一个方法里面
							qft.setTypetime("中午");
						}
					} else if (Integer.parseInt(ss[0]) == 18) {
						if (Integer.parseInt(ss[1]) < 30) {
							qft.setTypetime("下午");
						} else {
							qft.setTypetime("晚上");
						}
					} else {
						qft.setTypetime("下午");
					}
					tml.add(qft);
					continue;
				}
				// 如果时间大于等于 18 并且 小于等于 5，那么是晚上
				if (Integer.parseInt(ss[0]) >= 18 || Integer.parseInt(ss[0]) < 5) {
					if (Integer.parseInt(ss[0]) == 18) {
						if (Integer.parseInt(ss[1]) >= 30) {
							qft.setTypetime("晚上");
						} else {
							qft.setTypetime("下午");
						}
					} else {
						qft.setTypetime("晚上");
					}
					tml.add(qft);
				}
			}
		}
		BaseIni.rlist.clear();
		BaseIni.rlist.addAll(tml);
	}
}
