package com.bohc.deal;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bohc.bean.BaseIni;
import com.bohc.parsehtml.ProxyBean;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.demo.JxBrowserDemo;
import com.teamdev.jxbrowser.chromium.demo.Tab;
import com.teamdev.jxbrowser.chromium.demo.TabFactory;
import com.teamdev.jxbrowser.chromium.demo.TabbedPane;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;

public class ValidateProxyIp {
	private TabbedPane bt;
	private Browser browser;
	int v = -1;
	private String vurl = "http://211.149.235.153:8080/qly/gip.jsp";
	int staycount = 0;

	public ValidateProxyIp(TabbedPane bt) throws InterruptedException {
		super();
		this.bt = bt;
		BaseIni.proxipvalid.clear();
		CreateTab ct = new CreateTab();
		ct.start();
	}

	private void createTab(final ProxyBean pb) {
		try {
			bt.disposeAllTabs();
			Tab tab = TabFactory.createTab(vurl, pb.getIp(), pb.getPort());
			bt.addTab(tab);
			bt.selectTab(tab);
			browser = tab.getContent().browserView.getBrowser();
			BaseIni.proxyipvalidating = true;

			browser.addLoadListener(new LoadAdapter() {
				LoadListener loadlistener = this;

				@Override
				public void onFailLoadingFrame(FailLoadingEvent event) {
					if (event.isMainFrame()) {
						staycount=20;
						browser.removeLoadListener(loadlistener);
					}
				}

				@Override
				public void onDocumentLoadedInMainFrame(LoadEvent event) {
					getHtml(pb);
					browser.removeLoadListener(loadlistener);
				}

			});
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (staycount < 20) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						staycount++;
					}
					staycount = 0;
					BaseIni.proxyipvalidating = false;
				}
			}).start();
		} catch (final Exception e1) {
			JxBrowserDemo.jd.updateUi("地址不可用：" + pb.toString() + "\t" + e1.getMessage() + "\r\n");
		}
	}

	private void getHtml(ProxyBean pb) {
		String content;
		//开始解析返回内容；重新开始计时
		staycount=0;
		try {
			content = browser.getDocument().findElement(By.tagName("body")).getInnerHTML();
			Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
			Matcher m = p.matcher(content);
			while (m.find()) {
				pb.setRealip(m.group(1));
				BaseIni.proxipvalid.put(++v, pb);
				JxBrowserDemo.jd.updateUi(v + "\t可用地址：" + "\t" + pb.toString() + "\r\n");
			}
		} catch (Exception e) {
			JxBrowserDemo.jd.updateUi("获取信息出错" + "\r\n");
		}
		staycount = 20;
	}

	class CreateTab extends Thread {
		@Override
		public void run() {
			for (int i = 0; i < BaseIni.proxip.size(); i++) {
				try {
					while (BaseIni.proxyipvalidating) {
						Thread.sleep(1000);
					}
					ProxyBean pb = BaseIni.proxip.get(i);
					createTab(pb);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Long oldv = System.currentTimeMillis();
			while (BaseIni.proxyipvalidating) {
				if (BaseIni.proxyipvalidating || System.currentTimeMillis() - oldv > 1000 * 20) {
					break;
				}
			}

			BaseIni.proxip.clear();
			int k = 0;
			Set<String> eset = new HashSet<String>();
			for (Integer key : BaseIni.proxipvalid.keySet()) {
				ProxyBean epb = BaseIni.proxipvalid.get(key);
				if (!eset.contains(epb.getRealip().trim())) {
					BaseIni.proxip.put(k++, epb);
					eset.add(epb.getRealip().trim());
				}
			}

			JxBrowserDemo.jd.updateUi("ip地址验证完成\r\n");
			for (Integer p : BaseIni.proxipvalid.keySet()) {
				JxBrowserDemo.jd.updateUi(p + "\t验证完成地:\t" + BaseIni.proxipvalid.get(p) + "\r\n");
			}
		}
	}

}
