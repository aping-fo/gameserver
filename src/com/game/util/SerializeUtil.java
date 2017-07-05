package com.game.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.server.util.ServerLogger;

/**
 * 序列化工具类
 */
public class SerializeUtil {

	// 反序列化
	public static Object deserial(byte[] data) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object result = ois.readObject();
			bis.close();
			ois.close();
			return result;
		} catch (Exception e) {
			ServerLogger.err(e, "deserial err");
			return null;
		}
	}

	// 将对象序列化
	public static byte[] serialObject(Object object) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			oos.flush();

			byte[] result = bos.toByteArray();
			oos.close();
			bos.close();

			return result;
		} catch (Exception e) {
			ServerLogger.err(e, "serial object err");
			return new byte[0];
		}
	}

}
