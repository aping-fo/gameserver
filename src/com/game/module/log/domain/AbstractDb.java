package com.game.module.log.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucky on 2018/3/12.
 */
public class AbstractDb {
    private static final String WHERE_KEY = "player_id";
    private boolean saveFlag; //true:save ,false update

    public void setSaveOrUpdate(boolean saveOrUpdate) {
        this.saveFlag = saveOrUpdate;
    }

    public String getSql() {
        String table = this.getClass().getAnnotation(DB.class).name();
        StringBuilder sb = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        if (saveFlag) {
            sb.append("insert into ").append(table);
            sb.append("(");
            for (Field f : fields) {
                DB db = f.getAnnotation(DB.class);
                if (db != null) {
                    sb.append(db.name()).append(",");
                }
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            sb.append(")");
            sb.append(" values(");

            for (Field f : fields) {
                DB db = f.getAnnotation(DB.class);
                if (db != null) {
                    sb.append("?,");
                }
            }

            sb.replace(sb.length() - 1, sb.length(), "");
            sb.append(")");
        } else {
            sb.append("update ").append(table).append(" set ");
            for (Field f : fields) {
                DB db = f.getAnnotation(DB.class);
                if (db != null) {
                    if (!db.name().equals(WHERE_KEY)) {
                        sb.append(db.name()).append(" = ?,");
                    }
                }
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            sb.append(" where player_id = ?");
        }
        return sb.toString();
    }

    public Object[] getParams() {
        List<Object> params = new ArrayList<>();
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            if (saveFlag) {
                for (Field f : fields) {
                    DB db = f.getAnnotation(DB.class);
                    if (db != null) {
                        boolean flag = f.isAccessible();
                        f.setAccessible(true);
                        params.add(f.get(this));
                        f.setAccessible(flag);
                    }
                }
            } else {
                Object v = null;
                for (Field f : fields) {
                    DB db = f.getAnnotation(DB.class);
                    if (db != null) {
                        boolean flag = f.isAccessible();
                        f.setAccessible(true);
                        if (!WHERE_KEY.equals(db.name())) {
                            params.add(f.get(this));
                        } else {
                            v = f.get(this);
                        }
                        f.setAccessible(flag);
                    }
                }
                params.add(v);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return params.toArray();
    }
}
