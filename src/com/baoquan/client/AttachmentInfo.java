package com.baoquan.client;

public class AttachmentInfo {
	private String id;
	private String file_key;
	private String file_name;
	private Long size;
	private String content_type;
	private Long created_at;
	public AttachmentInfo() {
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFile_key() {
		return file_key;
	}
	public void setFile_key(String file_key) {
		this.file_key = file_key;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public String getContent_type() {
		return content_type;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}
	public Long getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Long created_at) {
		this.created_at = created_at;
	}
}
