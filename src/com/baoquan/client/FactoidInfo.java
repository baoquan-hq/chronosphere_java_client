package com.baoquan.client;

public class FactoidInfo {
	
	private String id; // 陈述id
	private String type;// 陈述类型
	private Long created_at;// 陈述创建时间
	public FactoidInfo() {
	}
	
	public FactoidInfo(String id, String type, Long created_at) {
		this.id = id;
		this.type = type;
		this.created_at = created_at;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Long created_at) {
		this.created_at = created_at;
	}
}
