package com.bohc.sh.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bohc.sh.dao.TareaDao;
import com.bohc.sh.entities.Tarea;
import com.bohc.sh.service.TareaService;

@Service("tareaService")
public class TareaServiceImpl implements TareaService {
	@Autowired
	private TareaDao tareaDao;

	@Override
	public List<Tarea> list(Tarea tarea) {
		return tareaDao.list(tarea);
	}

}
