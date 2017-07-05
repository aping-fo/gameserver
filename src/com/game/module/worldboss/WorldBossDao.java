package com.game.module.worldboss;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface WorldBossDao {
    @SQL("select data from wb_data where id = :id")
    public byte[] selectWorldBossRecords(@SQLParam("id")int id);

    @SQL("select max(id) from wb_data")
    public Integer selectMaxId();

    @SQL("REPLACE INTO wb_data VALUES(:id, :data)")
    public void updateWorldRecord(@SQLParam("id") int type, @SQLParam("data") byte[] bytes);
}
