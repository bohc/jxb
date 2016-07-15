/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package com.teamdev.jxbrowser.chromium.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.bohc.bean.BaseIni;
import com.bohc.deal.FetchFlyTicketJs;
import com.bohc.deal.FetchFlyTicketS;
import com.bohc.deal.FetchFlyTicketTest;
import com.bohc.deal.ValidateProxyIp;
import com.bohc.parsehtml.ParseContent;
import com.bohc.view.MyTabFactory;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.demo.resources.Resources;

/**
 * @author TeamDev Ltd.
 */
public class JxBrowserDemo {
	private static TabbedPane mt, bt;
	private static JFrame frame;
	private static JSplitPane splitPane;
	private static Tab ct = null;
	private JButton btnfetch, btncancel;
	public static JxBrowserDemo jd;

	private static void initEnvironment() throws Exception {
		System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JxBrowser Demo");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	public static void main(String[] args) throws Exception {
		initEnvironment();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jd = new JxBrowserDemo();
				jd.initAndDisplayUI();
			}
		});
	}

	private void initAndDisplayUI() {

		LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);
		LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);
		LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);

		final TabbedPane tabbedPane = new TabbedPane();
		bt = tabbedPane;
		insertTab(tabbedPane, TabFactory.createFirstTab());
		insertNewTabButton(tabbedPane);

		final TabbedPane mYTabbedPane = new TabbedPane();
		mt = mYTabbedPane;
		insertTab(mYTabbedPane, MyTabFactory.createTab("console"));

		frame = new JFrame("ÍøÒ³ä¯ÀÀÆ÷");
		frame.setTitle("\u53BB\u54EA\u513F\u673A\u7968\u6570\u636E\u6293\u53D6");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				BaseIni.tabmap.clear();
				tabbedPane.disposeAllTabs();
				mYTabbedPane.disposeAllTabs();
			}
		});
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		createMainToolBar();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.add(tabbedPane, JSplitPane.TOP);
		splitPane.add(mYTabbedPane, JSplitPane.BOTTOM);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		frame.setSize(1024, 668);
		frame.setLocationRelativeTo(null);
		frame.setIconImage(Resources.getIcon("jxbrowser16x16.png").getImage());
		frame.setVisible(true);
	}

	private void insertNewTabButton(final TabbedPane tabbedPane) {
		TabButton button = new TabButton(Resources.getIcon("new-tab.png"), "New tab");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				insertTab(tabbedPane, TabFactory.createTab());
			}
		});
		tabbedPane.addTabButton(button);
	}

	public void insertTab(TabbedPane tabbedPane, Tab tab) {
		tabbedPane.addTab(tab);
		tabbedPane.selectTab(tab);
	}

	private void createMainToolBar() {
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		FlowLayout fl_panel = new FlowLayout(FlowLayout.LEFT, 5, 5);
		fl_panel.setAlignOnBaseline(true);
		panel.setLayout(fl_panel);

		btnfetch = new JButton("\u6293\u53D6");
		btnfetch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FetchFlyTicketS fft = new FetchFlyTicketS(bt);
					fft.start();
					BaseIni.fetchstatus = true;
					btnfetch.setEnabled(false);
					btncancel.setEnabled(true);
					BaseIni.anasecond = 0;
					BaseIni.changeCount(false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton btnJs = new JButton("JS\u6293\u53D6");
		btnJs.setEnabled(false);
		btnJs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FetchFlyTicketJs fft = new FetchFlyTicketJs(bt);
				fft.start();
			}
		});
		panel.add(btnJs);
		btnfetch.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(btnfetch);

		JButton btnNewButton_1 = new JButton("\u8BBE\u7F6E");
		btnNewButton_1.setHorizontalAlignment(SwingConstants.LEFT);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertTab(mt, MyTabFactory.createTab("settings"));
			}
		});

		btncancel = new JButton("\u53D6\u6D88");
		btncancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BaseIni.fetchstatus = false;
				btnfetch.setEnabled(true);
			}
		});
		btncancel.setEnabled(false);
		panel.add(btncancel);
		panel.add(btnNewButton_1);

		final JButton btnNewButton_2 = new JButton("Òþ²Ø");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnNewButton_2.getText().trim().equals("Òþ²Ø")) {
					splitPane.setDividerLocation(splitPane.getWidth());
					splitPane.setContinuousLayout(true);
					btnNewButton_2.setText("ÏÔÊ¾");
				} else {
					splitPane.setDividerLocation(100);
					splitPane.setContinuousLayout(true);
					btnNewButton_2.setText("Òþ²Ø");
				}
			}
		});

		JButton btnNewButton_4 = new JButton("´úÀí");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ParseContent.searchProxyIp();
			}
		});
		panel.add(btnNewButton_4);
		panel.add(btnNewButton_2);

		JButton btnNewButton_3 = new JButton("\u65E5\u5FD7");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertTab(mt, MyTabFactory.createTab("console"));
				splitPane.setDividerLocation(100);
				splitPane.setContinuousLayout(true);
				btnNewButton_2.setText("Òþ²Ø");
			}
		});
		panel.add(btnNewButton_3);

		JButton btnNewButton_5 = new JButton("\u6D4B\u8BD5");
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					new ValidateProxyIp(bt);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(btnNewButton_5);
		
		JButton btnTest = new JButton("test");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FetchFlyTicketTest ft = new FetchFlyTicketTest(bt);
				ft.start();
				BaseIni.fetchstatus = true;
				btnfetch.setEnabled(false);
				btncancel.setEnabled(true);
				BaseIni.anasecond = 0;
				BaseIni.changeCount(false);
			}
		});
		panel.add(btnTest);
		panel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { btnfetch, btnNewButton_1 }));
	}

	public void updateUi(final String msg) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (BaseIni.logview != null) {
							try {
								if (BaseIni.logview.getDocument().getLength() > 6000) {
									int length = msg.length();
									if (msg.length() > BaseIni.logview.getDocument().getLength()) {
										length = BaseIni.logview.getDocument().getLength();
									}
									BaseIni.logview.getDocument().remove(0, length);
								}
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							BaseIni.logview.append("\r\n" + msg);
							int height = 10;
							Point p = new Point();
							p.setLocation(0, BaseIni.logview.getLineCount() * height);
							BaseIni.logscroll.getViewport().setViewPosition(p);
							if(BaseIni.logview.isFocusable()){
								//requestFocus();
								BaseIni.logview.select(BaseIni.logview.getDocument().getLength(), BaseIni.logview.getDocument().getLength()); 
							}
						}
					}
				});
			}
		}).start();
	}

	public void updateBtn(final int btnno, final String msg) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						switch (btnno) {
							case 1:
								btnfetch.setEnabled(true);
								btncancel.setEnabled(false);
								break;
						}
					}
				});
			}
		}).start();
	}

}
