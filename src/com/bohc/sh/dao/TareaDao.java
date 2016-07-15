package com.bohc.sh.dao;

import java.util.List;

import com.bohc.sh.entities.Tarea;

public interface TareaDao {
	public abstract List<Tarea> list(Tarea tarea);
}
