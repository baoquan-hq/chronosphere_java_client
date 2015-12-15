package com.baoquan.client;

/**
 * 调用api时产生的异常
 * @author ChenLiufang
 * 2015下午12:28:57
 */
public class ApiExcption extends Exception {
	private static final long serialVersionUID = 1L;
	private int code;
	private String error;

	public ApiExcption(int code, String error) {
		this.code = code;
		this.error = error;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return error;
	}

}
