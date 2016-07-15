
package com.bohc.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
	public static String readValue(String filePath, String key) {
		Properties props = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(filePath));
			props.load(in);
			String value = props.getProperty(key);
			System.out.println(key + value);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static HashMap<String, String> readProperties(String filePath) {
		HashMap hashMap = new HashMap();
		Properties props = new Properties();
		InputStream in = null;
		try {
			File f = new File(filePath);
			if (!(f.exists())) {
				return hashMap;
			}
			in = new BufferedInputStream(new FileInputStream(filePath));
			props.load(in);
			Enumeration en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String Property = props.getProperty(key);
				hashMap.put(key, Property);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return hashMap;
	}

	public static int writeProperties(String filePath, String parameterName, String parameterValue) {
		int iRtn = 0;
		Properties prop = new Properties();
		InputStream fis = null;
		OutputStream fos = null;
		try {
			File f = new File(filePath);
			if (!(f.exists())) {
				f.createNewFile();
			}
			fis = new FileInputStream(filePath);

			prop.load(fis);

			fos = new FileOutputStream(filePath);
			if (parameterValue == null) {
				parameterValue = "";
			}
			prop.setProperty(parameterName, parameterValue);

			prop.store(fos, "Update '" + parameterName + "' value");
		} catch (IOException e) {
			System.err.println("Visit " + filePath + " for updating " + parameterName + " value error");
			iRtn = -1;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return iRtn;
	}

	public static int writeProperties(String filePath, HashMap<String, String> hashMap) {
		Properties prop = new Properties();
		int iRtn = 0;
		InputStream fis = null;
		OutputStream fos = null;
		try {
			File f = new File(filePath);
			if (!(f.exists())) {
				f.createNewFile();
			}
			fis = new FileInputStream(filePath);

			prop.load(fis);

			fos = new FileOutputStream(filePath);
			Iterator it = hashMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				prop.setProperty(key, value);
			}

			prop.store(fos, null);
		} catch (IOException e) {
			System.err.println("Visit " + filePath + " for updating value error");
			iRtn = -1;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return iRtn;
	}
}
