package com.bohc.sh.service;

import java.util.List;

import com.bohc.sh.entities.Tarea;

public interface TareaService {
	public abstract List<Tarea> list(Tarea tarea);
}
