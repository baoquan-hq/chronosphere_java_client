package com.baoquan.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;

/**
 * 保全客户端
 * 
 * @author CHENLIUFANG 2015下午1:14:17
 */
public class Client {

	/**
	 * 创建新的保全
	 * 
	 * @param template_id
	 *            模板ID（不能为空）
	 * @param identities
	 *            身份事项（可为空）
	 * @param factoids
	 *            陈述集合（可为空）
	 * @return
	 * @throws ApiExcption
	 */
	@Testable
	public Attestation createAttestation(String template_id, Identities identities, List<Factoid> factoids) throws ApiExcption {
		Utils.checkString(template_id, "template_id 不能为空 ！");
		if (factoids != null && factoids.size() > 0) {
			checkFactoids(factoids);
		} else {
			factoids = null;
		}
		Payload payload = new Payload();
		payload.setTemplate_id(template_id);
		payload.setIdentities(identities);
		payload.setFactoids(factoids);
		String result = post(payload, "/api/v1/attestations");
		return JSON.parseObject(result, Attestation.class);
	}

	/**
	 * 通过保全号查询保全
	 * 
	 * @param no
	 *            保全号（不能为空）
	 * @return
	 * @throws ApiExcption
	 */
	@Testable
	public Attestation getAttestation(String no) throws ApiExcption {
		Utils.checkString(no, "no 不能为空 ！");
		String apiPath = "/api/v1/attestation";
		String method = "GET";
		Payload payload = new Payload();
		payload.setNo(no);

		// 生成请求数据的json
		String payloadJson = JSON.toJSONString(payload);
		// 访问时间
		long tonce = getLocalTime();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("access_key", getAccessKey());
		paras.put("tonce", tonce + "");
		paras.put("signature", signature(method, apiPath, payloadJson, tonce));
		paras.put("payload", payloadJson);

		String result = innerHttpClient.get(apiPath, paras);
		return JSON.parseObject(result, Attestation.class);

	}

	/**
	 * 保全单个陈述
	 * 
	 * @param ano
	 *            保全号（不能为空）
	 * @param type
	 *            陈述类型（不能为空）
	 * @param data
	 *            陈述数据（不能为空）
	 * @param attachments
	 *            （可为空）
	 * @return
	 * @throws ApiExcption
	 */
	@Testable
	public FactoidInfo createFactoid(String ano, String type, Map<String, Object> data, List<File> attachments) throws ApiExcption {
		Utils.checkString(ano, "ano 不能为空 ！");
		Utils.checkString(type, "type 不能为空 ！");
		Utils.checkMap(data, "data 不能为空 ！");
		Payload payload = new Payload();
		payload.setAno(ano);
		payload.setType(type);
		payload.setData(data);
		String result = post(payload, "/api/v1/factoids", attachments);
		return JSON.parseObject(result, FactoidInfo.class);
	}

	/**
	 * 保全多个陈述
	 * 
	 * @param ano
	 *            保全号（不能为空）
	 * @param factoids
	 *            陈述集合 （不能为空）
	 * @return
	 * @throws ApiExcption
	 */
	@Testable
	public List<FactoidInfo> createFactoids(String ano, List<Factoid> factoids) throws ApiExcption {
		Utils.checkString(ano, "ano 不能为空 ！");
		Utils.checkList(factoids, "factoids 不能为空 ！");
		checkFactoids(factoids);
		Payload payload = new Payload();
		payload.setAno(ano);
		payload.setFactoids(factoids);
		String result = post(payload, "/api/v1/factoids/multi");
		List<FactoidInfo> list = JSON.parseArray(result, FactoidInfo.class);
		List<String> successIds = new ArrayList<String>();
		List<String> failedIds = new ArrayList<String>();
		//检查陈述是否都保存成功 
		for (FactoidInfo factoidInfo : list) {
			if (factoidInfo.getId() == null ) {
				failedIds.add(factoidInfo.getType());
			}else{
				successIds.add(factoidInfo.getType());
			}
		}
		if (failedIds.size() > 0) {
			String successJson = JSON.toJSONString(successIds);
			String failedJson = JSON.toJSONString(failedIds);
			throw new ApiExcption(9001, String.format("部分陈述保存失败，保存失败的陈述：%s，保存成功的陈述%s ", failedJson, successJson));
		}
		return list;
	}

	/**
	 * 获取服务器时间
	 * 
	 * @return
	 * @throws ApiExcption
	 */
	public Long getServerTime() throws ApiExcption {
		String result = innerHttpClient.get("/api/v1/time", null);
		QueryResult qrs = JSON.parseObject(result, QueryResult.class);
		return qrs.getTimestamp();
	}
	
	private Long getLocalTime(){
		return new Date().getTime()/1000;
	}

	private String post(Payload payload, String apiPath) throws ApiExcption {
		return post(payload, apiPath, null);
	}

	private String post(Payload payload, String apiPath, List<File> attachments) throws ApiExcption {
		String method = "POST";
		// 生成文件摘要并创建上传entity
		MultipartEntity reqEntity = new MultipartEntity();
		// 保存单个陈述的时候
		if (attachments != null && attachments.size() > 0) {
			parseAttachments(payload, reqEntity, attachments);
		} else if (payload.getFactoids() != null && payload.getFactoids().size() > 0) {
			parseAttachments(payload, reqEntity);
		}
		// 生成请求数据的json
		String payloadJson = JSON.toJSONString(payload);
		// 大小写问题(java默认首字母小写)
		payloadJson = payloadJson.replaceAll("\"iD\"", "\"ID\"").replaceAll("\"mO\"", "\"MO\"");
		System.out.println(payloadJson);
		// 访问时间
		long tonce = getLocalTime();
		// 生成签名
		String signature = signature(method, apiPath, payloadJson, tonce);
		try {
			reqEntity.addPart("access_key", new StringBody(getAccessKey(), Charset.forName(CHARSET_NAME)));
			reqEntity.addPart("payload", new StringBody(payloadJson, Charset.forName(CHARSET_NAME)));
			reqEntity.addPart("signature", new StringBody(signature, Charset.forName(CHARSET_NAME)));
			reqEntity.addPart("tonce", new StringBody(tonce + "", Charset.forName(CHARSET_NAME)));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return innerHttpClient.post(apiPath, reqEntity);
	}

	/**
	 * 检查factoids
	 * 
	 * @param factoids
	 */
	private void checkFactoids(List<Factoid> factoids) {
		if (factoids == null || factoids.size() == 0) {
			throw new RuntimeException("factoids 不能为空 ！");
		} else {
			for (Factoid factoid : factoids) {
				Utils.checkString(factoid.getType(), "factoid 的  type 不能为空 ！");
				Utils.checkMap(factoid.getData(), "factoid 的 data 不能为空 ！");
			}
		}
	}

	/**
	 * 解析附件(应用于多个陈述)
	 * 
	 * @param payload
	 * @param reqEntity
	 */
	private void parseAttachments(Payload payload, MultipartEntity reqEntity) {
		List<Factoid> list = payload.getFactoids();
		if (list == null || list.size() == 0) {
			return;
		}
		Map<String, List<String>> payloadAttachmentsMap = new HashMap<String, List<String>>();
		for (int i = 0; i < list.size(); i++) {
			Factoid factoid = list.get(i);
			List<File> attachments = factoid.getFiles();
			List<String> filesums = new ArrayList<String>();
			for (int j = 0; j < attachments.size(); j++) {
				File f = attachments.get(j);
				// 文件摘要
				String filesum = checksums(f);
				filesums.add(filesum);
				// 文件上传的名称
				String uploadKey = String.format("attachments[%d][]", i);
				reqEntity.addPart(uploadKey, getFileBody(f));
			}

			if (filesums.size() > 0) {
				payloadAttachmentsMap.put(i + "", filesums);
			}
			// 清理附件列表（避免不必要的json转换）
			factoid.clearFiles();

		}
		// 设置payload的 Attachments
		if (payloadAttachmentsMap.keySet().size() > 0) {
			payload.setAttachments(payloadAttachmentsMap);
		}

	}

	/**
	 * 解析附件(应用于单个陈述)
	 * 
	 * @param payload
	 * @param reqEntity
	 */
	public void parseAttachments(Payload payload, MultipartEntity reqEntity, List<File> attachments) {
		List<String> filesums = new ArrayList<String>();
		for (int j = 0; j < attachments.size(); j++) {
			File f = attachments.get(j);
			// 文件摘要
			String filesum = checksums(f);
			filesums.add(filesum);
			// 文件上传的名称
			reqEntity.addPart("attachments[]", getFileBody(f));
		}
		payload.setAttachments(filesums);

	}
	//中文文件名转码（防止乱码）
	private FileBody getFileBody(File f ){
		FileBody fileBody = new FileBody(f){
			@Override
			public String getFilename() {
				String name = super.getFilename() ;
				try {
					String newName = URLEncoder.encode(name, CHARSET_NAME);
					return newName;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("");
				}
			}
		};
		return fileBody ;
	}
	
	

	/**
	 * 签名
	 * 
	 * @param method
	 *            HTTP请求方法
	 * @param apiPath
	 *            请求api路径
	 * @param payload
	 *            请求内容
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 */
	private String signature(String method, String apiPath, String payload, long tonce) {
		method = method.toUpperCase();
		StringBuffer buf = new StringBuffer();
		buf.append(method).append(apiPath).append(getAccessKey()).append(tonce).append(payload);
		try {
			Mac mac = Mac.getInstance(ALGORITHM_HMACSHA256);
			byte[] secretByte = getSecretKey().getBytes(CHARSET_NAME);
			byte[] dataBytes = buf.toString().getBytes(CHARSET_NAME);
			SecretKey secret = new SecretKeySpec(secretByte, ALGORITHM_HMACSHA256);
			mac.init(secret);
			byte[] doFinal = mac.doFinal(dataBytes);
			return Utils.bytes2Hex(doFinal);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
			throw new RuntimeException("生成签名失败！");
		}

	}

	/**
	 * 文件的SHA1摘要
	 * 
	 * @param f
	 * @return
	 */
	public String checksums(File f) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGORITHM_SHA1);
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

	public Client(String accessKey, String secretKey) {
		this(secretKey, accessKey, "https://sandbox.baoquan.com");
	}

	public Client(String accessKey, String secretKey, String endpoint) {
		Utils.checkString(accessKey, "accessKey不能为空！");
		Utils.checkString(secretKey, "secretKey不能为空！");
		checkEndpoint(endpoint, "endpoint不合法!1：必须以https://开头。2：不能有问号。3：不能以/结尾。");
		this.secretKey = secretKey;
		this.accessKey = accessKey;
		this.endpoint = endpoint;

	}

	private void checkEndpoint(String endpoint, String error) {
		Utils.checkString(endpoint, error);
		Pattern pattern = Pattern.compile("^https://\\w+(\\.\\w+)++(/\\w+)*$", Pattern.CASE_INSENSITIVE);
		if (!pattern.matcher(endpoint).matches()) {
			throw new RuntimeException(error);
		}
	}

	private String secretKey;
	private String accessKey;
	private String endpoint;// 请求网址
	public static String CHARSET_NAME = "UTF-8";
	public static String ALGORITHM_HMACSHA256 = "HmacSHA256";
	public static String ALGORITHM_SHA1 = "SHA1";

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	private InnerHttpClient innerHttpClient = new InnerHttpClient();

	/**
	 * 负责请求的客户端
	 * 
	 * @author ChenLiufang 2015上午11:42:24
	 */
	private class InnerHttpClient {

		public InnerHttpClient() {

		}

		protected String post(String apiPath, HttpEntity entity) throws ApiExcption {
			CloseableHttpClient client = HttpClients.createDefault();
			final HttpPost post = new HttpPost(getEndpoint() + apiPath);
			post.setEntity(entity);
			final StringBuffer sbuf = new StringBuffer();
			HttpResponse response;
			try {
				response = client.execute(post);
				int statusCode = response.getStatusLine().getStatusCode();
				if ((statusCode + "").startsWith("2")&& statusCode < 300) {
					HttpEntity resEntity = response.getEntity();
					// httpclient自带的工具类读取返回数据
					sbuf.append(EntityUtils.toString(resEntity,"UTF-8"));
					EntityUtils.consume(resEntity);
				} else {
					String content = EntityUtils.toString(response.getEntity(),"UTF-8");
					throw new ApiExcption(statusCode, response.getStatusLine().getReasonPhrase() + content);
				}
			} catch (ParseException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return sbuf.toString();
		}

		protected String get(String apiPath, Map<String, String> params) throws ApiExcption {
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder sb = new StringBuilder(getEndpoint() + apiPath);
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
			final StringBuffer sbuf = new StringBuffer();
			final HttpGet get = new HttpGet(sb.toString());
			HttpResponse response;
			try {
				response = client.execute(get);
				int statusCode = response.getStatusLine().getStatusCode();
				if ((statusCode + "").startsWith("2") && statusCode < 300) {
					HttpEntity resEntity = response.getEntity();
					// httpclient自带的工具类读取返回数据
					sbuf.append(EntityUtils.toString(resEntity,"UTF-8"));
					EntityUtils.consume(resEntity);
				} else {
					String content = EntityUtils.toString(response.getEntity(),"UTF-8");
					throw new ApiExcption(statusCode, response.getStatusLine().getReasonPhrase() + content);
				}
			} catch (ParseException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return sbuf.toString();
		}

	}
}
