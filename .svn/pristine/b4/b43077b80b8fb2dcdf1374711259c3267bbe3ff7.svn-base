package com.game.module.serial;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface SerialDataDao {

	@SQL("update serial_data set data=:data where id = :id")
	public void updateSerialData(@SQLParam("data")byte[]data,@SQLParam("id")int id);
	
	@SQL("insert ignore into serial_data(id) values(:id)")
	public void initSerialData(@SQLParam("id")int id);
	
	@SQL("select data from serial_data where id = :id")
	public byte[] selectSerialData(@SQLParam("id")int id);
	
}
