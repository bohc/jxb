package com.bohc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class FileManger {

	/**
	 * 取文件后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileExtension(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf('.');
		if (lastIndexOfDot == -1)
			return "";
		int fileNameLength = fileName.length();
		return fileName.substring(lastIndexOfDot, fileNameLength);
	}

	public void copyFile(String oldPath, String newPath) {
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte buffer[] = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1)
					fs.write(buffer, 0, byteread);
				inStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	public static void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myDelFile = new File(filePath);
			myDelFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param dir
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true,否则返回false
	 */
	public static boolean deleteDirectory(String dir) {
		// 如果dir不以文件分隔符结尾，自动添加文件分隔符
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			// System.out.println("删除目录失败"+dir+"目录不存在！");
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
				// 删除子目录
			} else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}
		if (!flag) {
			// System.out.println("删除目录失败");
			return false;
		}
		// 删除当前目录
		if (dirFile.delete()) {
			// System.out.println("删除目录"+dir+"成功！");
			return true;
		} else {
			// System.out.println("删除目录"+dir+"失败！");
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param fileName
	 * @return 单个文件删除成功返回true,否则返回false
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}

	/**
	 * 写文件
	 * 
	 * @return
	 */
	public static String writeFile(String filepath, String text, String encode) {
		FileOutputStream fw = null;
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.mkdirs();
			}
			file.delete();

			fw = new FileOutputStream(filepath);
			OutputStreamWriter ow = new OutputStreamWriter(fw, encode);
			BufferedWriter bw = new BufferedWriter(ow);
			bw.write(text);
			bw.flush();
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ex) {
				}
			}
		}
		return filepath;
	}
	
	/**
	 * 追加写文件
	 * 
	 * @return
	 */
	public static String reWriteFile(String filepath, String text) {
		FileWriter fw = null;
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.mkdirs();
				file.delete();
			}else{
				file.createNewFile();
			}

			fw =new FileWriter( file,true) ;
			fw.write(text+"\r\n");
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ex) {
				}
			}
		}
		return filepath;
	}

	/**
	 * 读取文件
	 * 
	 * @return
	 */
	public static String readFile(String filepath, String encode) {
		InputStreamReader fr = null;
		StringBuilder sb = new StringBuilder();
		try {
			fr = new InputStreamReader(new FileInputStream(filepath), encode);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line+"\r\n");
			}
		} catch (Exception ex) {
			sb.append("-");
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException ex1) {
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 读取文件
	 * 
	 * @return
	 */
	public static String readFile(String filepath, String encode,String comparevalue) {
		InputStreamReader fr = null;
		StringBuilder sb = new StringBuilder();
		try {
			fr = new InputStreamReader(new FileInputStream(filepath), encode);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			int f=0;
			if(comparevalue==null || comparevalue.equals("")){
				f=1;
			}
			while ((line = br.readLine()) != null) {
				if(f==0){
					if(line.indexOf(comparevalue)>-1){
						continue;
					}
				}
				sb.append(line);
			}
		} catch (Exception ex) {
			sb.append("-");
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException ex1) {
				}
			}
		}
		return sb.toString();
	}

	public static String createFile(String realpath, String filepath,
			String text) throws Exception {
		Date pathDate = new Date();
		long pathTimestamp = pathDate.getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String pathYyyymmdd = "";
		pathYyyymmdd = dateFormat.format(pathDate);

		String path = filepath + "/" + pathYyyymmdd;
		String fileurl = realpath + "/" + path;
		File f1 = new File(realpath + "/" + filepath);
		if (!f1.exists()) {
			f1.mkdirs();
		}
		File f2 = new File(realpath + "/" + filepath + "/" + pathYyyymmdd);
		if (!f2.exists()) {
			f2.mkdirs();
		}

		File file = new File(fileurl, "" + pathTimestamp);
		OutputStream os = new FileOutputStream(file);
		byte[] b = text.getBytes();
		InputStream in = new ByteArrayInputStream(b);
		BufferedInputStream bin = new BufferedInputStream(in);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = bin.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		in.close();
		return path + "/" + pathTimestamp;
	}

	public static String createimg(String realpath, String filepath, String text)
			throws Exception {
		Date pathDate = new Date();
		long pathTimestamp = pathDate.getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String pathYyyymmdd = "";
		pathYyyymmdd = dateFormat.format(pathDate);

		String path = filepath + pathYyyymmdd + "/" + pathTimestamp;
		String fileurl = realpath + "/" + path;
		File f1 = new File(realpath + "/" + filepath);
		if (!f1.exists()) {
			f1.mkdir();
		}
		File f2 = new File(realpath + "/" + filepath + "/" + pathYyyymmdd);
		if (!f2.exists()) {
			f2.mkdir();
		}

		File file = new File(fileurl, "" + pathTimestamp);
		OutputStream os = new FileOutputStream(file);
		File infile = new File(text);

		InputStream in = new FileInputStream(infile);
		BufferedInputStream bin = new BufferedInputStream(in);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = bin.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		in.close();
		return path;
	}

	public static String getFile(String realpath, String path)
			throws IOException {
		String filepath = "", s;
		File f1 = new File(realpath + "/" + path);
		FileReader fr = new FileReader(f1);
		BufferedReader br = new BufferedReader(fr);
		s = br.readLine();
		while (s != null) {
			filepath = filepath + s;
			s = br.readLine();
		}

		return new String(filepath);
	}

	/**
	 * 转换编码,并写入新文件
	 * 
	 * @param filePath
	 * @param fileName
	 * @throws Exception
	 */
	public void convertFile(String filePath, String oldEncode, String newEncode)
			throws Exception {
		// 重写FileReader类,让file可以以指定编码格式读入
		FileReaderByEncode file = new FileReaderByEncode(filePath, oldEncode);
		BufferedReader br = new BufferedReader(file);
		String temp = "";
		StringBuffer sb = new StringBuffer();
		while ((temp = br.readLine()) != null) {
			sb.append(temp + '\n');
		}
		br.close();
		temp = sb.toString();
		// filePath = filePath.replace(FILE_PATH, NEW_FOLDER);
		File tempFile = new File(filePath);
		// 检查指定目录中是否存在目录,没有则新建
		if (!tempFile.exists()) {
			tempFile.mkdirs();
		}
		// 按照编码写出该文件
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
				filePath), newEncode);
		out.write(temp);
		out.flush();
		out.close();
	}
	
	public String formatXml(String str) throws UnsupportedEncodingException,
    IOException, DocumentException {
		//System.out.println(" str :  " + str);
		SAXReader reader=new SAXReader();
		//System.out.println(reader);
		//注释：创建一个串的字符输入流
		StringReader in=new StringReader(str);
		Document doc=reader.read(in);
		// System.out.println(doc.getRootElement());
		//注释：创建输出格式
		OutputFormat formater=OutputFormat.createPrettyPrint();
		//注释：设置xml的输出编码
		formater.setEncoding("utf-8");
		//注释：创建输出(目标)
		StringWriter out=new StringWriter();
		//注释：创建输出流
		XMLWriter writer=new XMLWriter(out,formater);
		//注释：输出格式化的串到目标中，执行后。格式化后的串保存在out中。
		writer.write(doc);
		
		String destXML=out.toString();
		writer.close();
		out.close();
		in.close();
		//注释：返回我们格式化后的结果
		return destXML;       
	}
}
