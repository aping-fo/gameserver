package com.game.sdk.utils;

import java.security.MessageDigest;
import java.util.Base64;

public class EncoderHandler {

    //	private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA1";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final Base64.Encoder encoder = Base64.getEncoder();

    /**
     * encode string
     *
     * @param algorithm
     * @param str
     * @return String
     */
    public static String encode(String algorithm, String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(str.getBytes());
            return formattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String encodeBase64(String s) {
        if (s == null)
            return null;
        return encoder.encodeToString(s.getBytes());
    }

    /**
     * 获取签名字符串
     *
     * @param s
     * @return
     */
    public static String sign(String s) {
        String sign = encode(SHA1, s);
        sign = encodeBase64(sign);
        return sign;
    }
    /**
     * encode By MD5
     *
     * @param str
     * @return String
     */
//	public static String encodeByMD5(String str) {
//		if (str == null) {
//			return null;
//		}
//		try {
//			MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
//			messageDigest.update(str.getBytes());
//			return formattedText(messageDigest.digest());
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

    /**
     * Takes the raw bytes from the digest and formats them correct.
     *
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    private static String formattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }
}
