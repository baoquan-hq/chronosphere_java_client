package com.baoquan.client.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.baoquan.client.ApiExcption;

/**
 * 客户端帮助类
 * 
 * @author CHENLIUFANG
 * 
 */
public class HttpClientHelper {
	public static String CHARSET_NAME = "UTF-8";

	/**
	 * 发送post请求
	 * 
	 * @param url
	 * @param entity
	 * @return
	 * @throws ApiExcption
	 */
	public static Object post(String url, HttpEntity entity) throws ApiExcption {
		CloseableHttpClient client = HttpClients.createDefault();
		final HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return doResponse(client, post, url );
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws ApiExcption
	 */
	public static Object get(String url, Map<String, String> params) throws ApiExcption {
		CloseableHttpClient client = HttpClients.createDefault();
		StringBuilder sb = new StringBuilder(url);
		if (params != null && params.keySet().size() > 0) {
			String flag = "?";
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = params.get(key);
				try {
					sb.append(String.format("%s%s=%s", flag, key, URLEncoder.encode(value, CHARSET_NAME)));
				} catch (UnsupportedEncodingException e) {
				}
				flag = "&";
			}
		}
		return doResponse(client, new HttpGet(sb.toString()), sb.toString());
	}

	/**
	 * 执行响应
	 * 
	 * @param client
	 * @param req
	 * @param t
	 * @return
	 * @throws ApiExcption
	 */
	private static Object doResponse(CloseableHttpClient client, HttpUriRequest req, String url ) throws ApiExcption {
		try {
			HttpResponse response = client.execute(req);
			Boolean isDownload = FileDownload.isDownload(response);
			int statusCode = response.getStatusLine().getStatusCode();
			if (checkStatusCode(statusCode)) {
				HttpEntity resEntity = response.getEntity();
				if (isDownload) {
					return   new FileDownload(resEntity.getContent(),  response, client);
				} else {
					String result = EntityUtils.toString(resEntity, CHARSET_NAME);
					EntityUtils.consume(resEntity);// 如果不是下载文件，关闭流
					client.close();
					return   result;
				}
			} else {
				String content = EntityUtils.toString(response.getEntity(), CHARSET_NAME);
				throw new ApiExcption(statusCode, response.getStatusLine().getReasonPhrase() + content  + "==>" + url );
			}
		} catch (ParseException | IOException e) {
			throw new RuntimeException(e);
		} 

	}
	
	
	/**
	 * 执行响应
	 * 
	 * @param client
	 * @param req
	 * @param t
	 * @return
	 * @throws ApiExcption
	 */
	@SuppressWarnings("unchecked")
	private static <T> T doResponse1(CloseableHttpClient client, HttpUriRequest req, Class<T> t) throws ApiExcption {
		try {
			HttpResponse response = client.execute(req);
			int statusCode = response.getStatusLine().getStatusCode();
			if (checkStatusCode(statusCode)) {
				HttpEntity resEntity = response.getEntity();
				if (t.getName().indexOf("String") > -1) {
					String result = EntityUtils.toString(resEntity, CHARSET_NAME);
					EntityUtils.consume(resEntity);// 如果不是下载文件，关闭流
					return (T) result;
				} else {
					return (T) new FileDownload(resEntity.getContent(),  response, client);
				}
			} else {
				String content = EntityUtils.toString(response.getEntity(), CHARSET_NAME);
				throw new ApiExcption(statusCode, response.getStatusLine().getReasonPhrase() + content);
			}
		} catch (ParseException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (t.getName().indexOf("String") > -1) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	

	private static boolean checkStatusCode(int statusCode) {
		return (statusCode + "").startsWith("2") && statusCode < 300;
	}



}
