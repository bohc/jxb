package com.bohc.parsehtml;

import java.io.IOException;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bohc.bean.BaseIni;
import com.bohc.util.FileManger;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

public class ParseContent {
	public static void searchProxyIp() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				BaseIni.proxip.clear();
				BaseIni.proxipvalid.clear();
				try {
					for (int c = 1, ind = 0; c < 21; c++) {
						Document doc = Jsoup.connect("http://www.proxy.com.ru/list_" + c + ".html").timeout(3000).get();
						Elements businesses = doc.select("table[style=TABLE-LAYOUT: fixed;border-collapse: collapse] tr");
						int size = businesses.size();
						for (int j = 0; j < size; j++) {
							ProxyBean pb = new ProxyBean();
							try {
								Element businese = businesses.get(j);
								Iterator<Element> prcs = businese.select("td").iterator();
								int i = 0;
								while (prcs.hasNext()) {
									Element ep = prcs.next();
									try {
										switch (i) {
											case 1:
												pb.setIp(ep.ownText().trim());
												break;
											case 2:
												pb.setPort(Integer.parseInt(ep.ownText()));
												break;
											case 3:
												pb.setProxytype(ep.ownText());
												break;
											case 4:
												pb.setProxyadd(ep.ownText());
												break;
										}
									} catch (Exception e) {
									}
									i++;
								}
								if (pb.getIp() != null && !pb.getIp().trim().equals("")) {
									// pb.getProxyadd().indexOf("电信") != -1 &&
									// pb.getProxyadd().indexOf("移动") != -1 &&
									if (pb.getProxyadd().indexOf("台湾") == -1 && pb.getProxyadd().indexOf("香港") == -1) {
										BaseIni.proxip.put(ind, pb);
										JxBrowserDemo.jd.updateUi(ind + "\t" + pb.toString() + "\r\n");
										ind++;
									}
								}
								// System.out.println("去哪儿网价格信息：" + fprice);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						businesses.clear();
					}
				} catch (IOException e) {
					JxBrowserDemo.jd.updateUi(e.getMessage() + "\r\n");
				}
			};
		}).start();
	}

}
