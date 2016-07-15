package com.bohc.view;

/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.bohc.bean.BaseIni;
import com.bohc.widget.LimitativeDocument;

/**
 * @author TeamDev Ltd.
 */
@SuppressWarnings("serial")
public class MyTabConsole extends MyTabContent {
	public static JTextArea logview;
	public static JScrollPane scrollPane;

	public MyTabConsole() {
		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("\u6E05\u9664");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logview.setText("");
			}
		});
		btnNewButton.setIcon(new ImageIcon(MyTabConsole.class.getResource("/com/teamdev/jxbrowser/chromium/demo/resources/close.png")));
		btnNewButton.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(btnNewButton);

		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		logview = new JTextArea();
		logview.setDocument(new LimitativeDocument(logview, 300));
		scrollPane.setViewportView(logview);
		logview.setWrapStyleWord(true);
		logview.setRows(0);
		logview.setLineWrap(true);
		logview.setText("\u8C03\u8BD5\u65E5\u5FD7\u8F93\u51FA...\r\n");
		BaseIni.logscroll = scrollPane;
		BaseIni.logview = logview;
	}
}
