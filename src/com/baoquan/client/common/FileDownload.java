package com.baoquan.client.common;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

public class FileDownload {
	private InputStream is;
	HttpResponse response;
	private CloseableHttpClient client;

	public FileDownload(InputStream is, HttpResponse response, CloseableHttpClient client) {
		super();
		this.is = is;
		this.client = client;
		this.response = response;
	}

	public static Boolean isDownload(HttpResponse response) {
		return response.getFirstHeader("Content-Disposition") != null;
	}
	

	public InputStream getInputStream() {
		return is;
	}

	public String getContentDisposition() {
		Header h = response.getFirstHeader("Content-Disposition");
		return h == null ? "attachment;filename=evidence.txt" : h.getValue().replace("Content-Disposition:", "");
	}

	public String getContentType() {
		Header h = response.getFirstHeader("content_type");
		return h == null ? "application/text" : h.getValue().replace("content_typ:", "");
	}

	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
