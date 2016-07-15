package com.bohc.util;

import java.io.*;

public class FileReaderByEncode extends InputStreamReader {
	public FileReaderByEncode(String fileName, String charSetName)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(new FileInputStream(fileName), charSetName);
	}
}
