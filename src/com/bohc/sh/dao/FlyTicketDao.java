package com.bohc.sh.dao;

import com.bohc.sh.entities.QlyFlyticket;

public interface FlyTicketDao{
	public void save(QlyFlyticket qft);
	public void save(String cond);
}
