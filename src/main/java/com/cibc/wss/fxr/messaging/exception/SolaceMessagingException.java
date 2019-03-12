package com.cibc.wss.fxr.messaging.exception;


public class SolaceMessagingException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private int errorCode;
	private String errorMsg;
 
	public SolaceMessagingException(int errorCode,String errorMsg,Throwable cause) {
		super(errorMsg, cause);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}