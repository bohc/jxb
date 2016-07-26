package com.bohc.sh.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.SQLQuery;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Component;

import com.bohc.sh.dao.FlyTicketDao;
import com.bohc.sh.entities.QlyFlyticket;

@Component("flyTicketDao")
public class FlyTicketDaoImpl extends BaseDao implements FlyTicketDao {
	int result = -1;

	@Override
	public void save(QlyFlyticket qft) {
		getSession().save(qft);
	}

	@Override
	public void save(String cond) {
		final String sql = cond;
		getSession().doWork(new Work() {
			@Override
			public void execute(Connection conn) throws SQLException {
				Statement stat = conn.createStatement();
				stat.executeUpdate("truncate flytickettemp;");
				result = stat.executeUpdate(sql);
				stat.close();
				String call = "{call airlinedeal()}";
				CallableStatement cs = conn.prepareCall(call);
				cs.executeUpdate();
				cs.close();
			}
		});
	}

	@Override
	public void deleteBeforeYesterday() {
		String sql = "delete from flyticket where DATE_FORMAT(starttime,'%Y-%m-%d') < DATE_FORMAT(NOW(),'%Y-%m-%d')";
		getSession().createSQLQuery(sql).executeUpdate();
	}

}
