package com.cibc.wss.fxr.messaging.model;

import java.util.Map;


public class FxrMessage {
	
	private String messageStr;
	private Map<String,Object> msgHeaders;
	private String messageId;
	private String correlationId; //currently not populated
	private String destination; 

	public String getMessageStr() {
		return messageStr;
	}

	public void setMessageStr(String messageStr) {
		this.messageStr = messageStr;
	}

	public Map<String, Object> getMsgHeaders() {
		return msgHeaders;
	}

	public void setMsgHeaders(Map<String, Object> msgHeaders) {
		this.msgHeaders = msgHeaders;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public FxrMessage(String messageStr, Map<String, Object> msgHeaders) {
		super();
		this.messageStr = messageStr;
		this.msgHeaders = msgHeaders;
	}
	
	public FxrMessage(String messageStr, Map<String, Object> msgHeaders, String messageId, String correlationId, String destination) {
		super();
		this.messageStr = messageStr;
		this.msgHeaders = msgHeaders;
		this.messageId = messageId;
		this.correlationId = correlationId;
		this.destination = destination;
	}
}