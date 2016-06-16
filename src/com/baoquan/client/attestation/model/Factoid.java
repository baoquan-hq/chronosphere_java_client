package com.baoquan.client.attestation.model;

import java.util.List;
import java.util.Map;

public class Factoid extends FactoidInfo {
	private Map<String, Object> data;
	private List<AttachmentInfo> attachments;
	private Long created_at;// 陈述创建时间
	public Factoid() {
	}

	public Factoid(String type, Map<String, Object> data) {
		setType(type);
		this.data = data;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public List<AttachmentInfo> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentInfo> attachments) {
		this.attachments = attachments;
	}


	public Long getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Long created_at) {
		this.created_at = created_at;
	}
	
}
