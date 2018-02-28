package com.game.sdk.utils;

import com.server.util.ServerLogger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by lucky on 2018/2/26.
 */
public class XmlParser {
    private static final String XML_HEAD = "/agip/header";
    private static final String XML_BODY = "/agip/body";

    public static void main(String[] args){
        String str = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<agip>\n" +
                "    <header>\n" +
                "        <command_id>10000001</command_id>\n" +
                "        <game_id>213</game_id>\n" +
                "        <gateway_id>213999</gateway_id>\n" +
                "    </header>\n" +
                "    <body>\n" +
                "        <gateway_code>gw213999</gateway_code>\n" +
                "        <gateway_password>0yiq29266489292543acid5782706149</gateway_password>\n" +
                "        <mac>0000000000</mac>\n" +
                "        <reconnect_flag>0</reconnect_flag>\n" +
                "        <server_id>0</server_id>\n" +
                "    </body>\n" +
                "</agip>";
        System.out.println( xmlCmdParser(str));
    }

    public static int xmlCmdParser(String content) {
        try {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());
            SAXReader reader = new SAXReader();
            Document doc = reader.read(inputStream);
            Element tag = (Element) doc.selectSingleNode(XML_HEAD);
            return Integer.parseInt(tag.elementText("command_id"));
        } catch (Throwable e) {
            ServerLogger.err(e,"");
        }
        return 0;
    }

    public static <T> void xmlParser(String content, T object) {
        try {
            ServerLogger.warn("xml parser begin");
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());
            SAXReader reader = new SAXReader();
            Document doc = reader.read(inputStream);
            Element tag = (Element) doc.selectSingleNode(XML_HEAD);
            parserObject(object, tag);
            tag = (Element) doc.selectSingleNode(XML_BODY);
            parserObject(object, tag);
            ServerLogger.warn("xml parser end");
        } catch (Throwable e) {
            ServerLogger.err(e, content);
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
            if (object.getClass().getField(name).getType() != String.class) {
                value = object.getClass().getField(name).getType().getDeclaredMethod("valueOf", String.class).invoke(null, e.getText());
            }
            object.getClass().getSuperclass()
                    .getDeclaredMethod("set" + s1.toUpperCase() + s2, object.getClass().getField(name).getType()).invoke(object, value);
        }
    }
}
