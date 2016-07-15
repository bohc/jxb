
package com.bohc.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
	private static final Log logger = LogFactory.getLog(FileUtil.class);

	public static String currentDirectory() {
		String RealClzPath = null;

		String currentJarPath = null;
		String path = null;
		try {
			currentJarPath = URLDecoder.decode(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
			path = URLDecoder.decode(FileUtil.class.getResource("").getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		}
		logger.debug("path:" + path);
		int pos = path.indexOf("!");
		if (pos != -1) {
			logger.debug("class is in jar");
			RealClzPath = path.substring(0, pos);
			pos = RealClzPath.lastIndexOf("/");
			if (pos != -1)
				RealClzPath = path.substring(0, pos + 1);
		} else {
			String loaderPath = currentJarPath;
			String packPath = FileUtil.class.getPackage().toString();
			packPath = packPath.replace("package ", "");
			packPath = "/" + packPath.replace(".", "/");
			loaderPath = loaderPath.replace(packPath, "");
			RealClzPath = loaderPath;
		}
		logger.debug("RealClzPath = " + RealClzPath);

		if (RealClzPath.startsWith("file:")) {
			RealClzPath = RealClzPath.substring(6, RealClzPath.length() - 1);
		}
		if (RealClzPath.startsWith("/")) {
			RealClzPath = RealClzPath.substring(1, RealClzPath.length() - 1);
		}
		return RealClzPath;
	}

	public static String ensureDirectory(String directory) {
		File destD = new File(directory);
		if (!(destD.exists())) {
			destD.mkdir();
		}
		return directory;
	}

	public static String getUpperLevelDirectory(String path, int level) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		for (int i = 0; i < level; ++i) {
			path = path.substring(0, path.lastIndexOf("/"));
		}
		return path;
	}
}
