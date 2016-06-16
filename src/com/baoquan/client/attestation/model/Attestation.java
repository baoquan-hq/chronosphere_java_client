package com.baoquan.client.attestation.model;

import java.util.List;

public class Attestation {

	private String no;// 保全号

	private String template_id; // 模板编号

	private Identities identities; // 身份事项

	private List<Factoid> factoids; // 陈述

	private List<String> factoid_ids;// 陈述的id字段

	public Attestation() {
	}

	public Attestation(String no, String template_id, Identities identities, List<Factoid> factoids) {
		this.no = no;
		this.template_id = template_id;
		this.identities = identities;
		this.factoids = factoids;
	}

	public Attestation(String no, String template_id, List<String> factoid_ids, Identities identities) {
		this.no = no;
		this.template_id = template_id;
		this.identities = identities;
		this.factoid_ids = factoid_ids;
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

	public List<String> getFactoid_ids() {
		return factoid_ids;
	}

	public void setFactoid_ids(List<String> factoid_ids) {
		this.factoid_ids = factoid_ids;
	}
	
	
}
