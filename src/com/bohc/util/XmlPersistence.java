package com.bohc.util;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.thoughtworks.xstream.XStream;

/**
 * 用于将任意对象持久化到XML文件及其逆过程的持久化类（dom4j,xstream实现）
 * 
 * @author: sitinspring(junglesong@gmail.com)
 * @date: 2008-1-8
 */
public class XmlPersistence<T> {
	// XML文件名
	private String xmlFile;

	// XML 文档对象
	private Document document;

	// 根节点
	private Element root;

	// 根节点名称
	private final String rootText = "root";
	
	// 是否写入文件
	private boolean iswriter;

	/**
	 * 单参数构造函数，指定存储的文件名
	 * 
	 * @param xmlFile
	 */
	public XmlPersistence(String xmlFile) {
		this.xmlFile = xmlFile;

		init();
	}

	public XmlPersistence(String xstring, boolean iswriter) {
		this.xmlFile = xstring;
		this.iswriter = iswriter;
		init();
	}

	/**
	 * 添加一个对象對應的節點到XML文件
	 * 
	 * @param type
	 */
	public void add(T type) {
		// 将对象转化为XML文字

		try {
			XStream xStream = new XStream();
			xStream.alias(type.getClass().getName(), type.getClass());
			String xml = xStream.toXML(type);

			// 将转化后的文字变成节点
			Document docTmp = DocumentHelper.parseText(xml);
			Element typeElm = docTmp.getRootElement();

			// 添加这个节点
			root.add(typeElm);

			// 保存文件
			saveDocumentToFile();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 从XML文件中,删除一个对象對應的節點
	 * 
	 * @param type
	 */
	public void del(T type) {
		// 将对象转化为XML文字
		XStream xStream = new XStream();
		String xml = xStream.toXML(type);

		try {
			List nodes = root.elements();

			for (Iterator it = nodes.iterator(); it.hasNext();) {
				Element companyElm = (Element) it.next();

				// 查找，节点全文相同必定元素相同
				if (companyElm.asXML().equals(xml)) {
					// 删除原有节点
					root.remove(companyElm);

					// 保存文件
					saveDocumentToFile();
					return;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 从XML中取得所有对象
	 * 
	 * @return
	 */
	public List<T> loadAll() {
		List<T> retval = new ArrayList<T>();

		try {
			List nodes = root.elements();

			for (Iterator it = nodes.iterator(); it.hasNext();) {
				Element companyElm = (Element) it.next();
				XStream xStream = new XStream();
				T t = null;
				try {
					t = (T) xStream.fromXML(companyElm.asXML());
				} catch (Exception e) {
					e.printStackTrace();
				}
				retval.add(t);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return retval;
	}

	// 初始化文档对象及根节点
	private void init() {
		try {
			if (iswriter) {
				File file = new File(xmlFile);
				// 判断文件的存在以增强程序的健壮性
				if (file.exists()) {
					// 文件存在,直接从文件读取文档对象
					SAXReader reader = new SAXReader();
					document = reader.read(file);
					root = document.getRootElement();
				} else {
					// 文件不存在,创建文档对象
					document = DocumentHelper.createDocument();
					root = document.addElement(rootText);// 创建根节点
				}
			} else {
				if (xmlFile == null || xmlFile.equals("")) {
					document = DocumentHelper.createDocument();
					root = document.addElement(rootText);// 创建根节点
				} else {
					document = DocumentHelper.parseText(xmlFile);
					root = document.getRootElement();// 创建根节点
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 将Document写回文件
	 * 
	 */
	private void saveDocumentToFile() {
		try {
			if (iswriter) {
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setEncoding("GBK"); // 指定XML编码
				XMLWriter writer = new XMLWriter(new FileWriter(xmlFile), format);
				writer.write(document);
				writer.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getXmlFile() {
		if (!iswriter) {
			xmlFile = document.asXML();
		}
		return xmlFile;
	}

}