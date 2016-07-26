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

		if (content != null && content.indexOf("正在加载中，请稍候") == -1) {
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
				filterLine();// 界面设置的过滤条件，两个航班号，头等舱，共享航班，中转航班
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

	private void parseSencondDataDirect(Browser browser) {
		// TODO Auto-generated method stub

	}

	private void dealSecondList() {
		// TODO Auto-generated method stub

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

	// 统一的执行 js ，如果出错，那返回到统一的地方处理
	private void execJs(Browser browser, String js) throws MyParseException {
		try {
			browser.executeJavaScript(js);// 执行js
		} catch (Exception e) {
			throwMyException("页面执行js异常");
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

	private void parseHtmlNoFilterTime(Browser browser) throws MyParseException {
		// 如果早，中，下，晚一个都没有选，那么取当前页面的所有航班
		fetchAllLine(parseAllData(browser, "J_FlightListBox"));
	}

	// 抓取所有本页面的航线
	private void fetchAllLine(DOMElement doc) {
		if (doc == null)
			return;
		Document document = Jsoup.parse(doc.getInnerHTML());
		// 得到所有包含航线的 div 元素
		Elements businesses = document.select("div.J_FlightItem");
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
			String starttime = "", linename = "", flyno = "";
			// 航线名称airline-name /[a-zA-Z0-9]+/
			linename = businese.select("p.airline-name span").first().ownText();
			qft.setAireline(linename);
			//System.out.println(businese.select("p.airline-name span").first().ownText());
			// 这儿没有旅行专线
			if (qft.getAireline().trim().equals("旅行专线")) {
				// 如果为旅行专线，那么没有航班号
				// qft.setFltno("待定");
				// qft.setAirtype("专线");
				// qft.setStartairport("待定");
				// qft.setArriveairport("待定");
				// starttime = businese.select("div.a-tm-be").first().ownText();
			} else {
				Pattern p = Pattern.compile("[a-zA-Z0-9]+");
				Matcher match = p.matcher(linename);
				if (match.find()) {
					flyno = match.group();
				}
				// 得到航班号
				qft.setFltno(flyno);
				// 得到发出时间
				starttime = businese.select("p.flight-time-deptime").first().ownText();
				// 航班类型
				qft.setAirtype(businese.select("div.pi-flightlogo-nl p").get(1).ownText());
				// 出发机场
				qft.setStartairport(businese.select("div.port-detail p.port-dep").first().ownText());
				// 到达机场
				qft.setArriveairport(businese.select("div.port-detail p.port-arr").first().ownText());
			}

			// 得到航线票价
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

			// 没有共享航班
			int shareflag = 0;
			Elements lnk_a = businese.select("div.pi-flightlogo-nl a.link-dot");
			if (lnk_a != null && lnk_a.size() > 0) {
				for (int i = 0; i < lnk_a.size(); i++) {
					String mmstr = lnk_a.get(i).ownText().trim();
					if (mmstr.equals("共享")) {
						shareflag = 1;
						break;
					}
					if (mmstr.equals("经停")) {
						shareflag = 2;
						break;
					}
				}
			}
			if (shareflag == 1) {
				qft.setShareline("共享");
			} else if (shareflag == 2) {
				qft.setShareline("经停");
			} else {
				qft.setShareline("正常");
			}

			// 座位级别
			int flag = 0;
			Elements i_fst_cls = businese.select("td.flight-price");
			if (i_fst_cls.size() > 0) {
				i_fst_cls = i_fst_cls.select("span.discount");
				if (i_fst_cls != null && i_fst_cls.size() > 0) {
					for (int i = 0; i < i_fst_cls.size(); i++) {
						if (i_fst_cls.get(i).ownText().trim().equals("头等舱")) {
							flag = 1;
							break;
						} else if (i_fst_cls.get(i).ownText().trim().equals("商务舱")) {
							flag = 2;
							break;
						}
					}
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
			Elements zz = businese.select("div.transfer-city");
			if (zz != null && zz.size() > 0) {
				Elements zn = zz.first().select("label.transfer-city-label");
				if (zn != null && zn.size() > 0) {
					if (zn != null && zn.first() != null) {
						String znstr = zn.first().ownText();
						if (znstr.trim().indexOf("中转") != -1) {
							if (qft.getTicketprice() > 0) {
								qft.setTicketprice(qft.getTicketprice() + 100 + BaseIni.fetchCitys.getRate());
								qft.setLinetype("中转");
							}
							// 取到中转城市
							qft.setChangecity(znstr.trim().substring(znstr.trim().length() - 2));
							// 取到中转航班号
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
							qft.setLinetype("直达");
						}
					} else {
						qft.setLinetype("直达");
					}
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
				System.out.println("时间格式化出错：" + e1.getMessage());
			}

			// 得到航班的准点率，有的航班可能没有，所以加个出错处理
			try {
				Elements es = businese.select("td.flight-ontime-rate p");
				if (es.size() > 0) {
					qft.setOffsetrate(es.first().ownText());
					qft.setOffsettime("0");
				}
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
