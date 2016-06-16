package com.baoquan.client;

import java.util.Date;
import java.util.List;

public class QueryResult {

	public Date getTime() {
		return new Date(timestamp);
	}

	public FactoidInfo getFactoidInfo() {
		return new FactoidInfo(id, type, created_at);
	}

	public Attestation getAttestation() {
		return new Attestation(no, template_id, identities, factoids);
	}

	public QueryResult() {
	}

	private Long timestamp;

	private String id; // 陈述id

	private String type;// 陈述类型

	private Long created_at;// 陈述创建时间

	private String no;// 保全号

	private String template_id; // 模板编号

	private Identities identities; // 身份事项

	private List<Factoid> factoids; // 陈述

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
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

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
	}

	public Identities getIdentities() {
		return identities;
	}

	public void setIdentities(Identities identities) {
		this.identities = identities;
	}

	public List<Factoid> getFactoids() {
		return factoids;
	}

	public void setFactoids(List<Factoid> factoids) {
		this.factoids = factoids;
	}

}
