package com.aki.utils;

public class MySalesforceException extends Exception {

	private static final long serialVersionUID = 1L;
	public String msg;
	
	public MySalesforceException(String msg) {
		super();
		this.msg = msg;
	}

	public MySalesforceException(Exception e, String msg) {
		super(e);
		this.msg = msg;
	}
	


}
