package com.bohc.sh.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.bohc.sh.dao.TareaDao;
import com.bohc.sh.entities.Tarea;
@Repository("tareaDao")
public class TareaDaoImpl extends BaseDao implements TareaDao {

	@Override
	public List<Tarea> list(Tarea tarea) {
		String sql="from Tarea where pid=:pid";
		Query query = getSession().createQuery(sql);
		query.setParameter("pid", tarea.getPid());
		return query.list();
	}

}
