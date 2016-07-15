/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package com.teamdev.jxbrowser.chromium.demo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.CustomProxyConfig;
import com.teamdev.jxbrowser.chromium.HostPortPair;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;
import com.teamdev.jxbrowser.chromium.swing.DefaultDownloadHandler;
import com.teamdev.jxbrowser.chromium.swing.DefaultPopupHandler;

/**
 * @author TeamDev Ltd.
 */
public final class TabFactory {
	public static Tab createFirstTab() {
		return createTab("http://flight.qunar.com", null, 0);
	}

	public static Tab createTab() {
		return createTab("about:blank", null, 0);
	}

	public static Tab createTab(String url) {
		return createTab(url, null, 0);
	}

	public static Tab createTab(String proxyip, int proxyport) {
		return createTab("about:blank", proxyip, proxyport);
	}

	public static Tab createTab(String url, String proxyip, int proxyport) {
		Browser browser = null;
		if (proxyip == null || proxyip.equals("")) {
			browser = new Browser();
		} else {
			HostPortPair httpServer = new HostPortPair(proxyip, proxyport);
			HostPortPair httpsServer = new HostPortPair(proxyip, proxyport);
			HostPortPair ftpServer = new HostPortPair(proxyip, proxyport);
			String exceptions = "<local>";
			browser = new Browser(new CustomProxyConfig(httpServer, httpsServer, ftpServer, exceptions));
		}
		BrowserView browserView = new BrowserView(browser);
		TabContent tabContent = new TabContent(browserView);

		browser.setDownloadHandler(new DefaultDownloadHandler(browserView));
		browser.setDialogHandler(new DefaultDialogHandler(browserView));
		browser.setPopupHandler(new DefaultPopupHandler());
		final TabCaption tabCaption = new TabCaption();
		tabCaption.setTitle("about:blank");

		tabContent.addPropertyChangeListener("PageTitleChanged", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				tabCaption.setTitle((String) evt.getNewValue());
			}
		});
		browser.loadURL(url);
		return new Tab(tabCaption, tabContent);
	}
}
