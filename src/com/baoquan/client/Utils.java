package com.baoquan.client;

import java.util.List;
import java.util.Map;

public class Utils {

	/**
	 * 检查字符串是否为空 
	 * @param content
	 * @param error
	 */
	public static void checkString(String content, String error) {
		if (null == content || "".equalsIgnoreCase(content)) {
			throw new RuntimeException(error);
		}
	}
	
	/**
	 * 检查Map是否为空 
	 * @param content
	 * @param error
	 */
	public static void checkMap(Map<String, Object> map, String error) {
		if (null == map || map.keySet().size() == 0) {
			throw new RuntimeException(error);
		}
	}
	
	/**
	 * 检查Map是否为空 
	 * @param content
	 * @param error
	 */
	public static void checkList(List<?> list, String error) {
		if (null == list || list.size() == 0) {
			throw new RuntimeException(error);
		}
	}
	
	/**
	 * 字节转 Hex
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
	
	
}
