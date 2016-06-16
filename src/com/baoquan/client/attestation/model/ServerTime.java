package com.baoquan.client.attestation.model;

import java.util.Date;

public class ServerTime {
	private Long timestamp;

	public Date getTime() {
		return new Date(timestamp);
	}

	public Long getTimestamp(){
		return timestamp ;
	}
}
