package com.game.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.server.util.ServerLogger;

/**
 * 其他比较杂的公共方法
 */
public class CommonUtil {
	
	public static final Charset CHARSET = Charset.forName("utf-8");

	public static String decode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			ServerLogger.err(e, "decode err " + str);
			return str;
		}
	}

	public static String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			ServerLogger.err(e, "decode err " + str);
			return str;
		}
	}

	public static String getIp(SocketAddress socketAddress) {
		String ip = "";
		if (null != socketAddress) {
			ip = socketAddress.toString();
			String[] split = ip.split(":");
			if (split.length == 2) {
				ip = split[0].substring(1, split[0].length());
			}
		}
		return ip;
	}
	
	public static <T> List<T> initList(int count,T initItem){
		List<T> list = new ArrayList<T>(count);
		for(int i=0;i<count;i++){
			list.add(initItem);
		}
		return list;
	}
	
	public static ArrayList<Integer> initIntList(int count){
		ArrayList<Integer> list = new ArrayList<Integer>(count);
		for(int i=0;i<count;i++){
			list.add(0);
		}
		return list;
	}

	/**
	 * 计算 loga(b)
	 */
	public static double log(int a, int b) {
		return Math.log(b) / Math.log(a);
	}

	public static String md5(String planText) {
		StringBuilder ciperText = new StringBuilder();

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = (messageDigest.digest(planText.getBytes(Charset
					.forName("UTF-8"))));
			for (byte b : bytes) {
				ciperText.append(Character.forDigit((b >> 4) & 0x0f, 16));
				ciperText.append(Character.forDigit(b & 0x0f, 16));
			}
		} catch (NoSuchAlgorithmException e) {
			ServerLogger.err(null, "md5 err" + planText);
		}

		return ciperText.toString();
	}
	
	public static String md5(byte[] src) {
		StringBuilder ciperText = new StringBuilder();

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = (messageDigest.digest(src));
			for (byte b : bytes) {
				ciperText.append(Character.forDigit((b >> 4) & 0x0f, 16));
				ciperText.append(Character.forDigit(b & 0x0f, 16));
			}
		} catch (NoSuchAlgorithmException e) {
			ServerLogger.err(e, "md5 err" + src);
		}

		return ciperText.toString();
	}
	
	public static int index(int[] array,int element){
		for(int i=0;i<array.length;i++){
			if(element==array[i]){
				return i;
			}
		}
		return -1;
	}

	/**
	 * 转成百分比
	 * 
	 * @param num
	 * @return
	 */
	public static String toPercent(double num) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.applyPattern("##.##%");
		return df.format(num);
	}
	
	/**
	 * 元素是否在数组内
	 * @param arr
	 * @param element
	 * @return
	 */
	public static boolean contain(int[] arr,int element){
		for(int ele:arr)
		{
			if(ele==element){
				return true;
			}
		}
		return false;
	}
	
	   /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";
    
    /**
     * 签名算法 
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * RSA验证算法
     * @param data
     * @param publicKey
     * @param sign
     * @return
     */
	public static boolean verifyRSA(String data, PublicKey publicKey, String sign) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(data.getBytes());
			return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
		} catch (Exception e) {
			ServerLogger.err(e, "RSA verify Err!");
			return false;
		}
	}
	
	public static RSAPublicKey getPublicKey(String modulus, String exponent) {  
        try {  
        	byte[] m = Base64.getDecoder().decode(modulus);  
            byte[] e = Base64.getDecoder().decode(exponent);  
            BigInteger b1 = new BigInteger(1,m);    
            BigInteger b2 = new BigInteger(1,e);    
            
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);  
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }  
	
	public static void main(String[] args)throws Exception{

		
		
		String module = "otPXIeqSrXT1QuEx42LlpWLJGbSI1zh7z4YvLZlniPWMZI9xqtytx4RCdPQKmip0v/cFFPuHyJDu8W4h4vEqtfzkyyhgifVodYXRTiMe4Us2QoYyQe4b0hCsowyb7xiIHTBZ/lG5J3OfyWCrClBECxo6MbeM+ZYNsFJX259mUwU=";
		String exponent ="AQAB";
		RSAPublicKey k = getPublicKey(module, exponent);

		
				
		String data = "{\"WaresId\":\"N1\",\"OrderId\":\"243354676\",\"UserId\":\"aaaaa\",\"Type\":\"MS\",\"Time\":\"2015-11-16\"}";
		//encryt2(k,data);
		
		String sign = "HVXY36eEfldQ+6bYra1P3KYwvs9mfv9yquqfUqrsr3yV+qxz2pPELHM06LTmZNjdm34nucDsLG2bEp9EVVFnjnCigveoEPolZk0NAkgc7vul9nki4+5qxST+pfyOAh0BWrW4Yp2zxY9y2v13QozNWE3j11IvuDxjMYZY5VJXnuk=";
		
		System.out.println(verifyRSA(data, k, sign));
		//System.out.println(encrypt(Base64.getEncoder().encode(data.getBytes()), k));
	}
}
