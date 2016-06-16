package com.baoquan.client.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.Base64;

import org.bouncycastle.openssl.PEMReader;

public class Utils {

	public static String CHARSET_NAME = "UTF-8";
	public static String ALGORITHM_HMACSHA256 = "HmacSHA256";
	public static String ALGORITHM_SHA256 = "SHA-256";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * 至少存在一个
	 * @param error
	 * @param objs 
	 */
	public static void existOne(String error, Object... objs) {
		Boolean hasOne = false;
		for (int i = 0; i < objs.length; i++) {
			Object o = objs[i];
			try {
				if (o instanceof String) {
					checkString(o.toString(), error);
				} else if (o instanceof List) {
					checkList((List) o, error);
				} else if (o instanceof Map) {
					checkMap((Map) o, error);
				} else {
					checkObject(o, error);
				}
				hasOne = true;
				break;
			} catch (Exception e) {

			}
		}

		if (!hasOne) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * 检查字符串是否为空
	 * 
	 * @param content
	 * @param error
	 */
	public static void checkString(String content, String error) {
		if (null == content || "".equalsIgnoreCase(content)) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * 检查对象是否为空
	 * 
	 * @param o
	 * @param error
	 */
	public static void checkObject(Object o, String error) {
		if (o == null) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * 检查Map是否为空
	 * 
	 * @param content
	 * @param error
	 */
	public static void checkMap(Map<String, Object> map, String error) {
		if (null == map || map.keySet().size() == 0) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * 检查List是否为空
	 * 
	 * @param content
	 * @param error
	 */
	public static void checkList(List<?> list, String error) {
		if (null == list || list.size() == 0) {
			throw new RuntimeException(error);
		}
	}

	public static void checkURL(String url, String error) {
		Utils.checkString(url, error);
		Pattern pattern = Pattern.compile("^https://\\w+(\\.\\w+)++(/\\w+)*$", Pattern.CASE_INSENSITIVE);
		if (!pattern.matcher(url).matches()) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * 字节转 Hex
	 * 
	 * @param bts
	 * @return
	 */
	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	/**
	 * 文件的SHA1摘要
	 * 
	 * @param f
	 * @return
	 */
	public static String checksums(File f, String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			DigestInputStream dis = new DigestInputStream(new FileInputStream(f), messageDigest);
			byte[] buf = new byte[100];
			while (dis.read(buf) != -1) {
				// 仅仅读文件就可以了 让 messageDigest 能 get到
			}
			dis.close();
			byte[] digest2 = messageDigest.digest();
			return Utils.bytes2Hex(digest2);

		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException("读取文件时发生异常！");
		}
	}

	/**
	 * 签名
	 * 
	 * @param requestId
	 * @param key
	 * @param secretKey
	 * @param method
	 * @param apiPath
	 * @param payload
	 * @param tonce
	 * @return
	 */
	public static String signature(String requestId, String key, String secretKey, String method, String apiPath, String payload, long tonce) {
		try {
			String data = "".concat(method.toUpperCase()).concat(apiPath).concat(requestId).concat(key).concat(tonce + "").concat(payload);
			return rsaSign(secretKey, data);
		} catch (Exception e) {
			throw new RuntimeException("签名失败！", e);
		}
		
	}

	public static String rsaSign(String key, String data) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		PEMReader pemReader = new PEMReader(new InputStreamReader(new FileInputStream(key)));
	    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(pemReader.readPemObject().getContent());
	    pemReader.close();
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
	    Signature signature = Signature.getInstance("SHA256WithRSA");
	    signature.initSign(privateKey);
	    signature.update(data.getBytes());
	    return Base64.getEncoder().encodeToString(signature.sign());	
	}
	

	/*
	 * 获得服务器时间（秒）
	 */
	public static Long getLocalTime() {
		return new Date().getTime() / 1000;
	}

	public static String getRequestId() {
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}

}
