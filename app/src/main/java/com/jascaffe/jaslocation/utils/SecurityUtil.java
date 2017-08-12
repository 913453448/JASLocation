package com.jascaffe.jaslocation.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类  提供SHA1 和 MD5加密
 * @author LiuQi
 *
 */
public class SecurityUtil {

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final String SHA1_ALGORITHM = "SHA1";
	private static final String MD5_ALGORITHM = "MD5";
	private static final String DEFAULT_CHARSET = "UTF-8";
	public static final String APP_SIGTURE = "ab1ecc7108a298d69724ac63b81e66aa";
	
	public static String sha1(String str) throws Exception {
		return sha1(str, null);
	}
	
	public static String sha1(String str, String charset) throws Exception {
		return encrypt(str, SHA1_ALGORITHM, charset);
	}
	
	public static String md5(String str) {
		return md5(str, null);
	}
	
	public static String md5(String str, String charset) {
		return encrypt(str, MD5_ALGORITHM, charset);
	}
	
	/**
	 * 加密
	 * @param str	需要加密的字符串
	 * @param algorithm	算法, 为null则使用默认算法MD5
	 * @param charset 编码格式	, 为null则使用默认编码格式 UTF-8
	 * @return 指定字符串加密后的16进制字符串
	 */
	public static String encrypt (String str, String algorithm, String charset) {
		
		if (str == null)
			return null;
		
		if (algorithm == null)
			algorithm = MD5_ALGORITHM;
		
		if (charset == null)
			charset = DEFAULT_CHARSET;
		
		String encryptStr = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(str.getBytes(charset));
			
			encryptStr = new String(encodeHex(md.digest()));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.out.println("指定的加密算法不存在");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("指定了不支持的编码格式");
		}
		
		return encryptStr;
	}
	
	
	public static char[] encodeHex(byte[] data) {

		int l = data.length;

		char[] out = new char[l << 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS[0x0F & data[i]];
		}

		return out;
	}
}
