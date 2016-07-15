package com.bohc.db;

import java.sql.*;

import com.bohc.util.FileUtil;

public class ConnectAccess {
	/**
	 * ��ѧ����ע�⣺ 1:�Ƚ���һ��access�ļ�a1.mdb,������D:/��; 2:�����ݿ��ļ�a1.mdb�н���һ����Table1��
	 * 3��ΪTable1���һ�У�����������һ����¼�� 4��������һ���������ֱ࣬����ȥ���оͿ��ԡ�
	 */
	public static void main(String args[]) throws Exception {
		ConnectAccess ca = new ConnectAccess();
		ca.ConnectAccessFile();
		// ca.ConnectAccessDataSource();
	}

	public void ConnectAccessFile() throws Exception {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		/**
		 * ֱ������access�ļ���
		 */
		String curpath = FileUtil.currentDirectory();
		String dbur1 = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ=" + curpath + "/../data/data.mdb";
		Connection conn = DriverManager.getConnection(dbur1, "username", "password");
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from tarea");
		while (rs.next()) {
			System.out.println(rs.getString(3));
		}
		rs.close();
		// long stime=System.currentTimeMillis();
		// for(int i=0;i<10000;i++){
		// stmt.addBatch("insert into tarea(acode,aname) values (86"+i+",'����"+i+"')");
		// }
		stmt.executeBatch();
		stmt.close();
		conn.close();
//		System.out.print(System.currentTimeMillis() - stime);
	}

	public void ConnectAccessDataSource() throws Exception {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		/**
		 * ����ODBC���ӷ�ʽ ��ν���ODBC���ӣ�
		 * ����windows�£�����ʼ��->��������塿->�����ܺ�ά����->�������ߡ�->������Դ
		 * ����������Դ�������һ��ָ��a1.mdb�ļ�������Դ�� ���紴������ΪdataS1
		 */
		String dbur1 = "jdbc:odbc:dataS1";// ��ΪODBC���ӷ�ʽ
		Connection conn = DriverManager.getConnection(dbur1, "username", "password");
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from Table1");
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
		rs.close();
		stmt.close();
		conn.close();
	}
}
