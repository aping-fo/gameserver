package bitcoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lucky on 2017/12/18.
 */
public class Encrypt {
    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    public static String SHA256(final byte[] data) {
        return SHA(data, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public static String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    public static byte[] SHABytes(final byte[] bytes) {
        String strType = "SHA-256";
        // 返回值
        byte[] byteBuffer = null;

        // 是否是有效字符串
        if (bytes != null && bytes.length > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(bytes);
                // 得到 byte 類型结果
                byteBuffer = messageDigest.digest();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return byteBuffer;
    }

    /**
     * 字符串 SHA 加密
     *
     * @return
     */
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    private static String SHA(final byte[] bytes, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (bytes != null && bytes.length > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(bytes);
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();
                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }
}
