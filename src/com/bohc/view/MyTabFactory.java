package com.bohc.view;

/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.bohc.bean.BaseIni;
import com.teamdev.jxbrowser.chromium.demo.Tab;
import com.teamdev.jxbrowser.chromium.demo.TabCaption;

/**
 * @author TeamDev Ltd.
 */
public final class MyTabFactory {
	public static Tab createTab(String funname) {
		MyTabContent tabContent = null;
		if (BaseIni.tabmap.containsKey(funname)) {
			Tab t = BaseIni.tabmap.get(funname);
			t.getContent().validate();
			return t;
		}
		final TabCaption tabCaption = new TabCaption();

		if (funname.trim().equals("console")) {
			tabContent = new MyTabConsole();
			tabCaption.setTitle("调试输出");
		} else if (funname.trim().equals("settings")) {
			tabContent = new MyTabSetting();
			tabCaption.setTitle("采集设置");
		}
		tabContent.validate();
		tabContent.addPropertyChangeListener("PageTitleChanged", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				tabCaption.setTitle((String) evt.getNewValue());
			}
		});
		Tab t = new Tab(tabCaption, tabContent);
		BaseIni.tabmap.put(funname, t);
		return t;
	}
}
