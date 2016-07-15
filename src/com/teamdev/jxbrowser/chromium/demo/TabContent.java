/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package com.teamdev.jxbrowser.chromium.demo;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.bohc.bean.BaseIni;
import com.bohc.deal.CheckHtmlIsLoad;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

/**
 * @author TeamDev Ltd.
 */
@SuppressWarnings("serial")
public class TabContent extends JPanel {

	public BrowserView browserView;
	private ToolBar toolBar;
	private JComponent jsConsole;
	private JComponent container;
	private JComponent browserContainer;

	public TabContent(final BrowserView browserView) {
		if (browserView != null) {
			this.browserView = browserView;
			this.browserView.getBrowser().addLoadListener(new LoadAdapter() {
				@Override
				public void onFinishLoadingFrame(FinishLoadingEvent event) {
					if (event.isMainFrame()) {
						firePropertyChange("PageTitleChanged", null, browserView.getBrowser().getTitle());
						if (BaseIni.fetchAirLine != null && !BaseIni.fetchAirLine.isFetching()) {
							CheckHtmlIsLoad.instance().dealHtml(browserView.getBrowser());
						}
					}
				}
			});
			browserContainer = createBrowserContainer();
			// jsConsole = createConsole();
			// toolBar = createToolBar(browserView);

			container = new JPanel(new BorderLayout());
			container.add(browserContainer, BorderLayout.CENTER);

			setLayout(new BorderLayout());
			// add(toolBar, BorderLayout.NORTH);
			add(container, BorderLayout.CENTER);
		}
	}

	private ToolBar createToolBar(BrowserView browserView) {
		ToolBar toolBar = new ToolBar(browserView);
		toolBar.addPropertyChangeListener("TabClosed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange("TabClosed", false, true);
			}
		});
		toolBar.addPropertyChangeListener("JSConsoleDisplayed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				showConsole();
			}
		});
		toolBar.addPropertyChangeListener("JSConsoleClosed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				hideConsole();
			}
		});
		return toolBar;
	}

	private void hideConsole() {
		showComponent(browserContainer);
	}

	private void showComponent(JComponent component) {
		container.removeAll();
		container.add(component, BorderLayout.CENTER);
		validate();
	}

	private void showConsole() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(browserContainer, JSplitPane.TOP);
		splitPane.add(jsConsole, JSplitPane.BOTTOM);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		showComponent(splitPane);
	}

	private JComponent createConsole() {
		JSConsole result = new JSConsole(browserView.getBrowser());
		result.addPropertyChangeListener("JSConsoleClosed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				hideConsole();
				toolBar.didJSConsoleClose();
			}
		});
		return result;
	}

	private JComponent createBrowserContainer() {
		JPanel container = new JPanel(new BorderLayout());
		container.add(browserView, BorderLayout.CENTER);
		return container;
	}

	public void dispose() {
		if (browserView != null) {
			browserView.getBrowser().dispose();
		}
	}
}
