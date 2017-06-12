package com.bohc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bohc.bean.BaseIni;
import com.bohc.bean.FetchCitys;
import com.bohc.bean.action.QlyFlyTicketAction;
import com.bohc.sh.entities.Tarea;
import com.bohc.sh.service.TareaService;
import com.bohc.util.FileManger;
import com.bohc.util.FileUtil;
import com.bohc.util.XmlPersistence;
import javax.swing.JSeparator;

@SuppressWarnings("serial")
public class MyTabSetting extends MyTabContent {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private TareaService tareaService;
	private DefaultListModel defaultListModel, defaultListModel_1, defaultListModel_2, defaultListModel_3, defaultListModel_4;
	private JList list, list_1, list_2, list_3, list_4, list_5;
	private JList list_6;
	private JFileChooser chooser;
	private FileFilter filter;
	private JButton button, button_1, btnapply;
	private JTextField intervaltiime;
	private JCheckBox fetchsecond;
	private JSpinner fetch_day;
	private JTextField txt_startdata;
	private JTextField txt_enddate;
	private JTextField txt_rate;
	private JCheckBox ck_tdc, ck_gxhb, ck_dhb, ck_bcg, ck_p0, ck_zzhb;
	private JCheckBox ck_morning, ck_noon, ck_anoon, ck_evening;
	private JCheckBox ck_fetch_aliy, ck_fetch_qunar;
	private JSpinner flag_fetch_rand_min, flag_fetch_rand_max;
	private JTextField intervaltiimeend;

	public MyTabSetting() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		add(panel_4, BorderLayout.NORTH);

		chooser = new JFileChooser(FileUtil.currentDirectory() + "/../data");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);// 设置选择模式，既可以选择文件又可以选择文件夹
		String extj[] = { "xml", "*" };
		filter = new FileNameExtensionFilter("Xml", extj);
		chooser.setFileFilter(filter);// 设置文件后缀过滤器
		panel_4.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_4.add(panel_6, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) panel_6.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		button = new JButton("\u4FDD\u5B58");
		panel_6.add(button);

		button_1 = new JButton("\u52A0\u8F7D");
		panel_6.add(button_1);

		btnapply = new JButton("\u5E94\u7528");
		btnapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BaseIni.fetchCitys.setFetchnext(fetchsecond.isSelected());
				BaseIni.fetchCitys.setCkbcg(ck_bcg.isSelected());
				BaseIni.fetchCitys.setCkdhb(ck_dhb.isSelected());
				BaseIni.fetchCitys.setCkgxhb(ck_gxhb.isSelected());
				BaseIni.fetchCitys.setCk_zzhb(ck_zzhb.isSelected());
				BaseIni.fetchCitys.setCktdc(ck_tdc.isSelected());
				BaseIni.fetchCitys.setCkmorning(ck_morning.isSelected());
				BaseIni.fetchCitys.setCknoon(ck_noon.isSelected());
				BaseIni.fetchCitys.setCkanoon(ck_anoon.isSelected());
				BaseIni.fetchCitys.setCkevening(ck_evening.isSelected());
				BaseIni.fetchCitys.setCkp0(ck_p0.isSelected());
				BaseIni.fetchCitys.setOverupnum(Integer.parseInt(fetch_day.getValue().toString()));
				BaseIni.fetchCitys.setIntervaltime(Integer.parseInt(intervaltiime.getText()));
				BaseIni.fetchCitys.setIntervaltimeend(Integer.parseInt(intervaltiimeend.getText()));
				BaseIni.fetchCitys.setRate(Integer.parseInt(txt_rate.getText()));
				BaseIni.fetchCitys.setFlagfetchrandmax(Integer.parseInt(flag_fetch_rand_max.getValue().toString()));
				BaseIni.fetchCitys.setFlagfetchrandmin(Integer.parseInt(flag_fetch_rand_max.getValue().toString()));
				BaseIni.fetchCitys.setCk_fetch_aliy(ck_fetch_aliy.isSelected());
				BaseIni.fetchCitys.setCk_fetch_qunar(ck_fetch_qunar.isSelected());
				try {
					BaseIni.fetchCitys.setStartdate(sdf.parse(txt_startdata.getText()));
					BaseIni.fetchCitys.setEnddate(sdf.parse(txt_enddate.getText()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		panel_6.add(btnapply);

		JButton button_2 = new JButton("\u5217\u8868");
		button_2.setEnabled(false);
		panel_6.add(button_2);

		JScrollPane scrollPane = new JScrollPane();
		panel_6.add(scrollPane);
		button_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retval;
				File file = null;
				chooser.setSelectedFile(new File("抓取城市.xml"));
				retval = chooser.showOpenDialog(button_1);// 显示“保存文件”对话框
				if (retval == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					System.out.println("File to save " + file);
				}
				if (file != null) {
					String content = FileManger.readFile(file.getAbsolutePath(), "GBK");
					XmlPersistence<FetchCitys> xp = new XmlPersistence<FetchCitys>(content, false);
					List<FetchCitys> tlist = xp.loadAll();
					if (tlist != null && tlist.size() > 0) {
						BaseIni.fetchCitys = tlist.get(0);
						defaultListModel_2.clear();
						defaultListModel_4.clear();
						for (Tarea ta : BaseIni.fetchCitys.getFromcitys()) {
							defaultListModel_2.add(defaultListModel_2.size(), ta);
						}
						for (Tarea ta : BaseIni.fetchCitys.getTocitys()) {
							defaultListModel_4.add(defaultListModel_4.size(), ta);
						}
					}
				}
			}
		});
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval;
				if (BaseIni.fetchCitys != null) {
					File file = null;
					chooser.setSelectedFile(new File("抓取城市.xml"));
					retval = chooser.showSaveDialog(button);// 显示“保存文件”对话框
					if (retval == JFileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
						System.out.println("File to save " + file);
					}
					if (file != null) {
						file.delete();
						XmlPersistence<FetchCitys> persistCmp = new XmlPersistence<FetchCitys>(file.getAbsolutePath(), true);
						persistCmp.add(BaseIni.fetchCitys);
						// hl.updateUI("保存完成");
					}
				}
			}
		});

		JSplitPane splitPane_5 = new JSplitPane();
		panel_4.add(splitPane_5, BorderLayout.SOUTH);
		splitPane_5.setResizeWeight(0.2);

		JPanel panel_8 = new JPanel();
		splitPane_5.setLeftComponent(panel_8);
		panel_8.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		panel_8.add(panel_5, BorderLayout.NORTH);
		panel_5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel label = new JLabel("\u6293\u53D6\u95F4\u9694");
		panel_5.add(label);

		intervaltiime = new JTextField();
		panel_5.add(intervaltiime);
		intervaltiime.setColumns(4);
		intervaltiime.setText("2");
		
		JLabel label_7 = new JLabel("\u81F3");
		panel_5.add(label_7);
		
		intervaltiimeend = new JTextField();
		intervaltiimeend.setText("6");
		panel_5.add(intervaltiimeend);
		intervaltiimeend.setColumns(4);

		JLabel label_1 = new JLabel("\u8D77\u59CB\u65E5\u671F\uFF1A");
		panel_5.add(label_1);

		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTime(new Date());
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		txt_startdata = new JTextField();
		panel_5.add(txt_startdata);
		txt_startdata.setColumns(10);
		txt_startdata.setText(sdf.format(cal.getTime()));

		JLabel label_2 = new JLabel("\u7EC8\u6B62\u65E5\u671F\uFF1A");
		panel_5.add(label_2);

		txt_enddate = new JTextField();
		panel_5.add(txt_enddate);
		txt_enddate.setColumns(10);
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 60);
		txt_enddate.setText(sdf.format(cal.getTime()));

		Label label_3 = new Label("\u56DE\u7A0B\u589E\u52A0\u5929\u6570\uFF1A");
		panel_5.add(label_3);

		fetch_day = new JSpinner();
		panel_5.add(fetch_day);
		fetch_day.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				BaseIni.fetchCitys.setOverupnum(Integer.parseInt(((JSpinner) e.getSource()).getValue().toString()));
			}
		});
		fetch_day.setModel(new SpinnerNumberModel(1, 0, 60, 6));

		JLabel lblNewLabel_2 = new JLabel("\u6D6E\u52A8\u4EF7\u683C");
		panel_5.add(lblNewLabel_2);

		txt_rate = new JTextField();
		panel_5.add(txt_rate);
		txt_rate.setColumns(5);
		txt_rate.setText("0");

		Panel panel_7 = new Panel();
		panel_8.add(panel_7, BorderLayout.CENTER);
		panel_7.setLayout(new BorderLayout(0, 0));

		JPanel panel_11 = new JPanel();
		panel_7.add(panel_11, BorderLayout.NORTH);
		FlowLayout flowLayout_1 = (FlowLayout) panel_11.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);

		ck_tdc = new JCheckBox("\u4E0D\u53D6\u5934\u7B49\u8231");
		ck_tdc.setSelected(true);
		panel_11.add(ck_tdc);
		ck_tdc.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setCktdc(ck_tdc.isSelected());
			}
		});

		fetchsecond = new JCheckBox("\u6293\u53D6\u7B2C\u4E8C\u5C42");
		panel_11.add(fetchsecond);
		fetchsecond.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setFetchnext(fetchsecond.isSelected());
			}
		});

		ck_gxhb = new JCheckBox("\u4E0D\u53D6\u5171\u4EA8");
		ck_gxhb.setSelected(true);
		panel_11.add(ck_gxhb);
		ck_gxhb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setCkgxhb(ck_gxhb.isSelected());
			}
		});

		ck_zzhb = new JCheckBox("\u4E0D\u53D6\u4E2D\u8F6C");
		panel_11.add(ck_zzhb);
		ck_zzhb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setCk_zzhb(ck_zzhb.isSelected());
			}
		});

		ck_dhb = new JCheckBox("\u4E0D\u53D6\u53CC\u822A\u73ED");
		ck_dhb.setSelected(true);
		panel_11.add(ck_dhb);
		ck_dhb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setCkdhb(ck_dhb.isSelected());
			}
		});

		ck_bcg = new JCheckBox("\u91CD\u91C7\u5931\u8D25\u822A\u73ED");
		panel_11.add(ck_bcg);

		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_9.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_9, BorderLayout.CENTER);

		ck_morning = new JCheckBox("\u65E9(06:00-11:59)");
		ck_morning.setSelected(true);
		ck_morning.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				BaseIni.fetchCitys.setCkmorning(ck_morning.isSelected());
			}
		});
		panel_9.add(ck_morning);

		ck_noon = new JCheckBox("\u4E2D(12:00-12:59)");
		ck_noon.setSelected(true);
		ck_noon.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				BaseIni.fetchCitys.setCknoon(ck_noon.isSelected());
			}
		});
		panel_9.add(ck_noon);

		ck_anoon = new JCheckBox("\u4E0B(13:00-17:59)");
		ck_anoon.setSelected(true);
		ck_anoon.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				BaseIni.fetchCitys.setCkanoon(ck_anoon.isSelected());
			}
		});
		panel_9.add(ck_anoon);

		ck_evening = new JCheckBox("\u665A(18:-05:59)");
		ck_evening.setSelected(true);
		ck_evening.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				BaseIni.fetchCitys.setCkevening(ck_evening.isSelected());
			}
		});
		panel_9.add(ck_evening);

		JPanel panel_10 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_10.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_10, BorderLayout.SOUTH);

		ck_p0 = new JCheckBox("\u8FC7\u6EE4 0 \u4EF7\u683C");
		ck_p0.setSelected(true);
		panel_10.add(ck_p0);

		JSeparator separator = new JSeparator();
		panel_10.add(separator);

		ck_fetch_qunar = new JCheckBox("\u53BB\u54EA\u513F");
		ck_fetch_qunar.setSelected(true);
		ck_fetch_qunar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				BaseIni.fetchCitys.setCk_fetch_qunar(((JCheckBox) evt.getSource()).isSelected());
			}
		});
		panel_10.add(ck_fetch_qunar);

		ck_fetch_aliy = new JCheckBox("\u963F\u91CC");
		ck_fetch_aliy.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				BaseIni.fetchCitys.setCk_fetch_aliy(((JCheckBox) evt.getSource()).isSelected());
			}
		});
		panel_10.add(ck_fetch_aliy);

		JLabel label_4 = new JLabel("\u4EA4\u53C9\u6293\u53D6");
		panel_10.add(label_4);

		flag_fetch_rand_min = new JSpinner();
		flag_fetch_rand_min.setModel(new SpinnerNumberModel(3, 3, 15, 1));
		panel_10.add(flag_fetch_rand_min);

		JLabel label_5 = new JLabel("\u81F3");
		panel_10.add(label_5);

		flag_fetch_rand_max = new JSpinner();
		flag_fetch_rand_max.setModel(new SpinnerNumberModel(8, 3, 15, 1));
		panel_10.add(flag_fetch_rand_max);

		JLabel label_6 = new JLabel("\u6761");
		panel_10.add(label_6);

		ck_bcg.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				BaseIni.fetchCitys.setCkbcg(ck_bcg.isSelected());
			}
		});

		list_6 = new JList();
		splitPane_5.setRightComponent(new JScrollPane(list_6));
		list_6.setModel(new AbstractListModel() {
			String[] values = new String[] { "\u4E4C\u9C81\u6728\u9F50\uFF0D\u6606\u660E.xml", "\u4E4C\u9C81\u6728\u9F50\uFF0D\u6606\u660E.xml", "\u4E4C\u9C81\u6728\u9F50\uFF0D\u6606\u660E.xml",
					"\u4E4C\u9C81\u6728\u9F50\uFF0D\u6606\u660E.xml" };

			public int getSize() {
				return values.length;
			}

			public Object getElementAt(int index) {
				return values[index];
			}
		});

		intervaltiime.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				try {
					int v = (int) event.getNewValue();
					BaseIni.fetchCitys.setIntervaltime(v);
				} catch (Exception e) {
				}
			}
		});

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		add(splitPane);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel("\u51FA\u53D1\u57CE\u5E02");
		lblNewLabel.setFont(new Font("宋体", Font.BOLD, 12));
		lblNewLabel.setBackground(Color.LIGHT_GRAY);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel, BorderLayout.NORTH);

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.6);
		panel.add(splitPane_1);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(null);
		splitPane_1.setLeftComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);
		panel_2.add(splitPane_2);

		list = new JList(new DefaultListModel<Tarea>());
		list.setBorder(new EmptyBorder(0, 0, 0, 0));
		list.setCellRenderer(new dbListCell());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				defaultListModel_1.clear();
				Tarea area = (Tarea) defaultListModel.get(list.getSelectedIndex());
				area.setPid(area.getAcode());
				List<Tarea> tlist = tareaService.list(area);
				if (tlist != null && tlist.size() > 0) {
					for (Tarea ta : tlist) {
						defaultListModel_1.add(0, ta);
					}
				}
			}
		});
		splitPane_2.setLeftComponent(new JScrollPane(list));
		defaultListModel = (DefaultListModel) list.getModel();
		Tarea tarea = new Tarea();
		tarea.setPid("86000000");
		tareaService = QlyFlyTicketAction.instance().getTareaservice();
		List<Tarea> tlist = tareaService.list(tarea);
		for (Tarea ta : tlist) {
			defaultListModel.add(defaultListModel.size(), ta);
		}
		list.setModel(defaultListModel);

		list_1 = new JList();
		list_1.setBorder(null);
		splitPane_2.setRightComponent(new JScrollPane(list_1));
		defaultListModel_1 = new DefaultListModel<Tarea>();
		list_1.setModel(defaultListModel_1);
		list_1.setCellRenderer(new dbListCell());
		list_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Tarea area = (Tarea) defaultListModel_1.get(list_1.getSelectedIndex());
					if (!defaultListModel_2.contains(area)) {
						defaultListModel_2.add(defaultListModel_2.size(), area);
						BaseIni.fetchCitys.getFromcitys().add(area);
					}
				}
			}
		});

		list_2 = new JList();
		list_2.setBorder(null);
		splitPane_1.setRightComponent(new JScrollPane(list_2));
		list_2.setModel(new DefaultListModel<Tarea>());
		list_2.setCellRenderer(new dbListCell());
		defaultListModel_2 = (DefaultListModel) list_2.getModel();
		list_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list_2.locationToIndex(e.getPoint());
					BaseIni.fetchCitys.getFromcitys().remove(defaultListModel_2.get(index));
					defaultListModel_2.removeElementAt(index);
				}
			}
		});

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel_1 = new JLabel("\u5230\u8FBE\u57CE\u5E02");
		lblNewLabel_1.setFont(new Font("宋体", Font.BOLD, 12));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBackground(Color.LIGHT_GRAY);
		panel_1.add(lblNewLabel_1, BorderLayout.NORTH);

		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.7);
		panel_1.add(splitPane_3);

		JPanel panel_3 = new JPanel();
		splitPane_3.setLeftComponent(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_4 = new JSplitPane();
		splitPane_4.setResizeWeight(0.5);
		panel_3.add(splitPane_4);

		list_3 = new JList();
		splitPane_4.setLeftComponent(new JScrollPane(list_3));
		list_3.setModel(defaultListModel);
		list_3.setCellRenderer(new dbListCell());
		list_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				defaultListModel_3.clear();
				Tarea area = (Tarea) defaultListModel.get(list_3.getSelectedIndex());
				area.setPid(area.getAcode());
				List<Tarea> tlist = tareaService.list(area);
				if (tlist != null && tlist.size() > 0) {
					for (Tarea ta : tlist) {
						defaultListModel_3.add(0, ta);
					}
				}
			}
		});

		list_4 = new JList();
		splitPane_4.setRightComponent(new JScrollPane(list_4));
		list_4.setModel(new DefaultListModel<Tarea>());
		list_4.setCellRenderer(new dbListCell());
		defaultListModel_3 = (DefaultListModel) list_4.getModel();
		list_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Tarea area = (Tarea) defaultListModel_3.get(list_4.getSelectedIndex());
					if (!defaultListModel_4.contains(area)) {
						defaultListModel_4.add(defaultListModel_4.size(), area);
						BaseIni.fetchCitys.getTocitys().add(area);
					}
				}
			}
		});

		list_5 = new JList();
		splitPane_3.setRightComponent(new JScrollPane(list_5));
		list_5.setModel(new DefaultListModel<Tarea>());
		list_5.setCellRenderer(new dbListCell());
		defaultListModel_4 = (DefaultListModel) list_5.getModel();
		list_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list_5.locationToIndex(e.getPoint());
					BaseIni.fetchCitys.getTocitys().remove(defaultListModel_4.get(index));
					defaultListModel_4.removeElementAt(index);
				}
			}

		});
	}
}

/**
 * 定制swing里面dblist的cell单元格如何显示
 */
class dbListCell extends JLabel implements ListCellRenderer<Tarea> {
	public ImageIcon _bgIcon;

	@Override
	public Component getListCellRendererComponent(JList<? extends Tarea> list, Tarea value, int index, boolean isSelected, boolean cellHasFocus) {
		setText(value.getArea());
		setIcon(null);
		// 判断是否选中
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}

}