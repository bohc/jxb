package com.bohc.sh.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bohc.sh.dao.FlyTicketDao;
import com.bohc.sh.entities.QlyFlyticket;
import com.bohc.sh.service.QlyFlyticketService;

@Service("qlyFlyticketService")
public class QlyFlyticketServiceImpl implements QlyFlyticketService {

	@Autowired
	FlyTicketDao flyticketdao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bohc.sh.service.QlyFlyticketServiceImpl#save(com.bohc.sh.entities
	 * .QlyFlyticket)
	 */
	@Override
	public void save(QlyFlyticket qft) {
		flyticketdao.save(qft);
	}
	@Override
	public void save(String cond) {
		flyticketdao.save(cond);
	}
}
