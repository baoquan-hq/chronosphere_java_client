package com.baoquan.client.attestation;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.alibaba.fastjson.JSON;
import com.baoquan.client.ApiExcption;
import com.baoquan.client.annotation.Testable;
import com.baoquan.client.attestation.model.Attestation;
import com.baoquan.client.attestation.model.Factoid;
import com.baoquan.client.attestation.model.FactoidInfo;
import com.baoquan.client.attestation.model.Identities;
import com.baoquan.client.attestation.model.Payload;
import com.baoquan.client.attestation.model.ServerTime;
import com.baoquan.client.common.HttpClientHelper;
import com.baoquan.client.common.Utils;

/**
 * 保全API
 * 
 * @author CHENLIUFANG 2015下午1:14:17
 */
public class AttestationApi {

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
	public Attestation createAttestation(String requestId, String template_id, Boolean completed, Identities identities, List<Factoid> factoids, Map<String, List<File>> fileListMap)
			throws ApiExcption {
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
		payload.setCompleted(completed);
		String result = post(payload, "/api/v1/attestations",requestId, fileListMap);
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
	public Attestation getAttestation(String requestId, String no ) throws ApiExcption {
		Utils.checkString(no, "no 不能为空 ！");
		String apiPath = "/api/v1/attestation";
		String method = "GET";
		Payload payload = new Payload();
		payload.setNo(no);
		// 生成请求数据的json
		String payloadJson = JSON.toJSONString(payload);
		// 访问时间
		long tonce = Utils.getLocalTime();
		Map<String, String> paras = new HashMap<String, String>();
		paras.put("access_key", getKey());
		paras.put("tonce", tonce + "");
		paras.put("signature", signature(method, apiPath, requestId, payloadJson, tonce));
		paras.put("payload", payloadJson);
		paras.put("request_id", requestId);
		paras.put("_id", Math.random() + new Date().getTime() + "");

		String result = (String) HttpClientHelper.get(getEndpoint() + apiPath, paras);
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
	public FactoidInfo createFactoid(String requestId, String ano, String type, Map<String, Object> data, List<File> attachments) throws ApiExcption {
		Utils.checkString(ano, "ano 不能为空 ！");
		Utils.checkString(type, "type 不能为空 ！");
		Utils.checkMap(data, "data 不能为空 ！");
		Payload payload = new Payload();
		payload.setAno(ano);
		payload.setType(type);
		payload.setData(data);
		String result = post(payload, "/api/v1/factoids",requestId, attachments);
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
	public List<FactoidInfo> createFactoids(String requestId, String ano, List<Factoid> factoids, Map<String, List<File>> fileListMap) throws ApiExcption {
		Utils.checkString(ano, "ano 不能为空 ！");
		Utils.checkList(factoids, "factoids 不能为空 ！");
		checkFactoids(factoids);
		Payload payload = new Payload();
		payload.setAno(ano);
		payload.setFactoids(factoids);
		String result = post(payload, "/api/v1/factoids/multi",requestId, fileListMap);
		List<FactoidInfo> list = JSON.parseArray(result, FactoidInfo.class);
		List<String> successIds = new ArrayList<String>();
		List<String> failedIds = new ArrayList<String>();
		// 检查陈述是否都保存成功
		for (FactoidInfo factoidInfo : list) {
			if (factoidInfo.getId() == null) {
				failedIds.add(factoidInfo.getType());
			} else {
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
		String result = (String) HttpClientHelper.get(getEndpoint() + "/api/v1/time", null);
		ServerTime serverTime = JSON.parseObject(result, ServerTime.class);
		return serverTime.getTimestamp();
	}

	@SuppressWarnings("unchecked")
	private String post(Payload payload, String apiPath, String requestId, Object attachments) throws ApiExcption {
		String method = "POST";
		// 生成文件摘要并创建上传entity
		MultipartEntity reqEntity = new MultipartEntity();
		// 保存单个陈述的时候
		if (attachments instanceof List) {
			List<File> files = (List<File>) attachments;
			parseAttachments(payload, reqEntity, files);
		} else if (attachments instanceof Map) {
			Map<String, List<File>> listFileMap = (Map<String, List<File>>) attachments;
			parseAttachments(payload, reqEntity, listFileMap);
		}
		// 生成请求数据的json
		String payloadJson = JSON.toJSONString(payload);
		// 大小写问题(java默认首字母小写)
		payloadJson = payloadJson.replaceAll("\"iD\"", "\"ID\"").replaceAll("\"mO\"", "\"MO\"");
		// 访问时间
		long tonce = Utils.getLocalTime();
		// 生成签名
		String signature = signature(method, apiPath, requestId, payloadJson, tonce);
		try {
			reqEntity.addPart("access_key", new StringBody(getKey(), Charset.forName(Utils.CHARSET_NAME)));
			reqEntity.addPart("payload", new StringBody(payloadJson, Charset.forName(Utils.CHARSET_NAME)));
			reqEntity.addPart("signature", new StringBody(signature, Charset.forName(Utils.CHARSET_NAME)));
			reqEntity.addPart("tonce", new StringBody(tonce + "", Charset.forName(Utils.CHARSET_NAME)));
			reqEntity.addPart("request_id", new StringBody(requestId + "", Charset.forName(Utils.CHARSET_NAME)));
		} catch (UnsupportedEncodingException e) {

		}
		return (String) HttpClientHelper.post(getEndpoint() + apiPath, reqEntity);
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
	private void parseAttachments(Payload payload, MultipartEntity reqEntity, Map<String, List<File>> listfilesMap) {
		if (listfilesMap == null || listfilesMap.keySet().size() == 0) {
			return;
		}
		Map<String, List<String>> payloadAttachmentsMap = new HashMap<String, List<String>>();
		Iterator<String> it = listfilesMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			List<File> files = listfilesMap.get(key);
			if (files.size() > 0) {
				List<String> filesums = new ArrayList<String>();
				for (int j = 0; j < files.size(); j++) {
					File f = files.get(j);
					// 文件摘要
					String filesum = Utils.checksums(f, Utils.ALGORITHM_SHA256);
					filesums.add(filesum);
					// 文件上传的名称
					String uploadKey = String.format("attachments[%s][]", key);
					reqEntity.addPart(uploadKey, getFileBody(f));
				}
				if (filesums.size() > 0) {
					payloadAttachmentsMap.put(key + "", filesums);
				}
			}

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
			String filesum = Utils.checksums(f, Utils.ALGORITHM_SHA256);
			filesums.add(filesum);
			// 文件上传的名称
			reqEntity.addPart("attachments[]", getFileBody(f));
		}
		payload.setAttachments(filesums);

	}

	// 中文文件名转码（防止乱码）
	private FileBody getFileBody(File f) {
		FileBody fileBody = new FileBody(f) {
			@Override
			public String getFilename() {
				String name = super.getFilename();
				try {
					String newName = URLEncoder.encode(name, Utils.CHARSET_NAME);
					return newName;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("");
				}
			}
		};
		return fileBody;
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

	private String signature(String method, String apiPath, String requestId, String payload, long tonce) {
		return Utils.signature(requestId, getKey(), getSecretKey(), method, apiPath, payload, tonce);
	}

	public AttestationApi(String key, String secretKey) {
		this(key, secretKey, "https://sandbox.baoquan.com");
	}

	public AttestationApi(String key, String secretKey, String endpoint) {
		Utils.checkString(key, "key不能为空！");
		Utils.checkString(secretKey, "secretKey不能为空！");
		// Utils.checkURL(endpoint,
		// "endpoint不合法!1：必须以https://开头。2：不能有问号。3：不能以/结尾。");
		this.secretKey = secretKey;
		this.key = key;
		this.endpoint = endpoint;

	}

	private String secretKey;
	private String key;
	private String endpoint;// 请求网址

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
