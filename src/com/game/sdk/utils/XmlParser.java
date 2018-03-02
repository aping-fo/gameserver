package com.game.sdk.utils;

import com.server.util.ServerLogger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by lucky on 2018/2/26.
 */
public class XmlParser {
    public static final String XML_HEAD = "/agip/header";
    public static final String XML_BODY = "/agip/body";

    public static final String FIELD_CMD = "command_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_ROLE_ID = "role_id";

    public static void main(String[] args) {
        String str = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<agip>\n" +
                "    <header>\n" +
                "        <command_id>20003802</command_id>\n" +
                "        <game_id>213</game_id>\n" +
                "        <gateway_id>213999</gateway_id>\n" +
                "    </header>\n" +
                "    <body>\n" +
                "        <result_code>-1811</result_code>\n" +
                "        <user_id>0</user_id>\n" +
                "        <user_type>0</user_type>\n" +
                "        <adult_flag>0</adult_flag>\n" +
                "        <user_class>0</user_class>\n" +
                "        <user_role_count>0</user_role_count>\n" +
                "        <user_point>0</user_point>\n" +
                "        <promoter_id>0</promoter_id>\n" +
                "        <user_flag>0</user_flag>\n" +
                "        <cp_return_value>-1309</cp_return_value>\n" +
                "        <cp_err_msg></cp_err_msg>\n" +
                "        <user_name></user_name>\n" +
                "        <appendix1>0</appendix1>\n" +
                "        <appendix2>0</appendix2>\n" +
                "    </body>\n" +
                "</agip> \n";
        System.out.println(xmlCmdParser(str.trim(), XML_BODY, FIELD_USER_ID));
    }

    public static int xmlCmdParser(String content, String xmlTag, String field) {
        try (InputStream inputStream = new ByteArrayInputStream(content.trim().getBytes("UTF-8"))) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(inputStream);
            Element tag = (Element) doc.selectSingleNode(xmlTag);
            return Integer.parseInt(tag.elementTextTrim(field));
        } catch (Throwable e) {
            ServerLogger.err(e, "");
        }
        return 0;
    }

    public static <T> void xmlParser(String content, T object) {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"))) {
            ServerLogger.info("xml parser begin");
            SAXReader reader = new SAXReader();
            Document doc = reader.read(inputStream);
            Element tag = (Element) doc.selectSingleNode(XML_HEAD);
            parserObject(object, tag);
            tag = (Element) doc.selectSingleNode(XML_BODY);
            parserObject(object, tag);
            ServerLogger.info("xml parser end");
        } catch (Throwable e) {
            ServerLogger.err(e, content);
            e.printStackTrace();
        }
    }

    private static <T> void parserObject(T object, Element tag) throws Exception {
        for (Object o : tag.elements()) {
            Element e = (Element) o;
            String name = e.getName();
            String s1 = name.substring(0, 1);
            String s2 = name.substring(1, name.length());
            System.out.println("set" + s1.toUpperCase() + s2);
            Object value = e.getText();

            Field field;
            try {
                field = object.getClass().getDeclaredField(name);
            } catch (Exception ex) {
                field = object.getClass().getSuperclass().getDeclaredField(name);
            }

            if (field.getType() != String.class) {
                value = field.getType().getDeclaredMethod("valueOf", String.class).invoke(null, e.getText());
            }

            Method method;
            try {
                method = object.getClass().getDeclaredMethod("set" + s1.toUpperCase() + s2, field.getType());
            } catch (Exception ex) {
                method = object.getClass().getSuperclass()
                        .getDeclaredMethod("set" + s1.toUpperCase() + s2, field.getType());
            }

            method.invoke(object, value);
        }
    }
}
