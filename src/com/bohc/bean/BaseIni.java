package com.bohc.bean;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bohc.parsehtml.ProxyBean;
import com.bohc.sh.entities.QlyFlyticket;
import com.teamdev.jxbrowser.chromium.demo.Tab;

public class BaseIni {
	public static FetchCitys fetchCitys = new FetchCitys();
	public static Map<String, Tab> tabmap = new HashMap<String, Tab>();
	public static JTextArea logview;
	public static JScrollPane logscroll;
	public static Dimension lsize;
	public static int maxlongtime = 30;// 抓取数据的最大等待时间；
	public static boolean proxyipvalidating = false;
	public static boolean isvalid = false;
	public static Map<Integer, ProxyBean> proxip = new HashMap<Integer, ProxyBean>();
	public static Map<Integer, ProxyBean> proxipvalid = new HashMap<Integer, ProxyBean>();
	public static int browserListenerCount = 0;
	public static int anasecond = 0;
	// 数据的抓取状态，如果为 true ,那么继续抓取，如果为 false，那么停止抓取
	public static boolean fetchstatus = true;

	public static Stack<FetchAirLine> satckAirline = new Stack<FetchAirLine>();// 要抓取的航线
	public static List<FetchAirLine> fetchFailList = new ArrayList<FetchAirLine>();// 要抓取失败的航线的航线
	public static FetchAirLine fetchAirLine;// 当前抓取的航线信息
	public static List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// 从页面上抓取到的航班数据
	public static List<String> flist = new ArrayList<String>();// 页面上被过滤了的航班数据

	public static synchronized void changeCount(boolean b) {
		if (b) {
			if (browserListenerCount == 0) {
				browserListenerCount++;
			}
		} else {
			if (browserListenerCount > 0) {
				browserListenerCount--;
			}
		}
	}
}
