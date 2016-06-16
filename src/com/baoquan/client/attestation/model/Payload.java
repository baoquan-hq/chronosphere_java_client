package com.baoquan.client.attestation.model;

import java.util.List;
import java.util.Map;

public class Payload {
	//保全相关信息 
	private String no ;
	private String template_id;
	private Identities identities;
	private List<Factoid>  factoids;
	private String ano;
	private String type ;
	private Map<String, Object> data ;
	private Factoid factoid;
	private Object attachments;
	private Boolean completed = false ;
	public Payload() { 
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
	public String getAno() {
		return ano;
	}
	public void setAno(String ano) {
		this.ano = ano;
	}
	
	public Factoid getFactoid() {
		return factoid;
	}
	public void setFactoid(Factoid factoid) {
		this.factoid = factoid;
	}
	public Object getAttachments() {
		return attachments;
	}
	public void setAttachments(Object attachments) {
		this.attachments = attachments;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

}
