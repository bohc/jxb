package com.bohc.sh.service;

import com.bohc.sh.entities.QlyFlyticket;

public interface QlyFlyticketService {

	public abstract void save(QlyFlyticket qft);
	public abstract void save(String cond);
	public abstract void deleteBeforeYesterday();

}