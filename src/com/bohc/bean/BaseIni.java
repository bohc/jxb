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
	public static int maxlongtime = 30;// ץȡ���ݵ����ȴ�ʱ�䣻
	public static boolean proxyipvalidating = false;
	public static boolean isvalid = false;
	public static Map<Integer, ProxyBean> proxip = new HashMap<Integer, ProxyBean>();
	public static Map<Integer, ProxyBean> proxipvalid = new HashMap<Integer, ProxyBean>();
	public static int browserListenerCount = 0;
	public static int anasecond = 0;
	// ���ݵ�ץȡ״̬�����Ϊ true ,��ô����ץȡ�����Ϊ false����ôֹͣץȡ
	public static boolean fetchstatus = true;

	public static Stack<FetchAirLine> satckAirline = new Stack<FetchAirLine>();// Ҫץȡ�ĺ���
	public static List<FetchAirLine> fetchFailList = new ArrayList<FetchAirLine>();// Ҫץȡʧ�ܵĺ��ߵĺ���
	public static FetchAirLine fetchAirLine;// ��ǰץȡ�ĺ�����Ϣ
	public static List<QlyFlyticket> rlist = new ArrayList<QlyFlyticket>();// ��ҳ����ץȡ���ĺ�������
	public static List<String> flist = new ArrayList<String>();// ҳ���ϱ������˵ĺ�������

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
