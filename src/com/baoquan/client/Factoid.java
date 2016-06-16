package com.baoquan.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Factoid {
	private String type;
	private Map<String, Object> data;
	private List<File> files;
	private List<AttachmentInfo> attachments;

	public Factoid() {
	}

	public Factoid(String type, Map<String, Object> data) {
		this.type = type;
		this.data = data;
	}

	public Factoid(String type, Map<String, Object> data, List<File> files) {
		this.type = type;
		this.data = data;
		this.files = files;
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

	public void addFile(File file) {
		if (files == null) {
			files = new ArrayList<File>();
		}

		files.add(file);
	}

	public List<File> getFiles() {
		if (files == null) {
			files = new ArrayList<File>();
		}
		return files;
	}

	public void clearFiles() {
		files = null;
	}

	public List<AttachmentInfo> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentInfo> attachments) {
		this.attachments = attachments;
	}
}
