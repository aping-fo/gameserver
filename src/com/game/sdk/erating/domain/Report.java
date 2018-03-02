package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by lucky on 2018/2/1.
 * 协议提拼装
 */
public class Report {
    public Integer getCommand_id() {
        return 0;
    }

    public String toProto() throws Exception {
        StringBuilder sb = new StringBuilder();
        NodeName parentNode = this.getClass().getAnnotation(NodeName.class);
        if (parentNode != null) {
            sb.append("<").append(parentNode.name()).append(">");
        }
        Field[] bodyFields = this.getClass().getDeclaredFields();
        for (Field field : bodyFields) {
            boolean flag = field.isAccessible();
            field.setAccessible(true);
            NodeName childNode = field.getAnnotation(NodeName.class);
            String name = childNode.name();
            sb.append("<").append(name).append(">");
            if (field.getType() == List.class) {
                List<Object> objects = (List<Object>) field.get(this);
                for (Object o : objects) {
                    if (childNode.object()) {
                        String subProto = (String) o.getClass().getMethod("toProto").invoke(o);
                        sb.append(subProto);
                    } else {
                        sb.append(o.toString());
                    }
                }
            } else {
                sb.append(field.get(this));
            }
            sb.append("</").append(name).append(">");
            field.setAccessible(flag);
        }
        if (parentNode != null) {
            sb.append("</").append(parentNode.name()).append(">");
        }
        return sb.toString();
    }

    public String toProto1() throws Exception {
        StringBuilder sb = new StringBuilder();
        NodeName parentNode = this.getClass().getAnnotation(NodeName.class);
        if (parentNode != null) {
            sb.append("&lt;").append(parentNode.name()).append("&gt;");
        }
        Field[] bodyFields = this.getClass().getDeclaredFields();
        for (Field field : bodyFields) {
            boolean flag = field.isAccessible();
            field.setAccessible(true);
            NodeName childNode = field.getAnnotation(NodeName.class);
            String name = childNode.name();
            sb.append("&lt;").append(name).append("&gt;");
            if (field.getType() == List.class) {
                List<Object> objects = (List<Object>) field.get(this);
                for (Object o : objects) {
                    if (childNode.object()) {
                        String subProto = (String) o.getClass().getMethod("toProto1").invoke(o);
                        sb.append(subProto);
                    } else {
                        sb.append(o.toString());
                    }
                }
            } else {
                sb.append(field.get(this));
            }
            field.setAccessible(flag);
        }
        return sb.toString();
    }
}
