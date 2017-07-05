package com.game.module.log;

public class SQLWrapper {
	
	private String sql;
	private Object[] params;
	
	public SQLWrapper(String sql, Object[] params) {
		this.sql = sql;
		this.params = params;
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}

}
