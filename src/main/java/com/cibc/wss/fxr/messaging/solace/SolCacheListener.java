package com.cibc.wss.fxr.messaging.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.solacesystems.jcsmp.CacheRequestResult;
import com.solacesystems.jcsmp.Topic;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.interfaces.IMessageListener;


public class SolCacheListener implements IMessageListener {
	
	private  static Logger logger = LoggerFactory.getLogger(SolCacheListener.class);
	
	public void onCacheRequestComplete(Long arg0, Topic arg1, CacheRequestResult arg2) throws Throwable {
		logger.info("On Cache Request");
		
	}

	public void onException(Exception e) {
		logger.error("Exception in Sol cache listener ",e);
		
	}

	public void onReceive(MitMessage message) throws Throwable {
		logger.info("Received cached message:[{}}" , message.getContent(String.class));
	}

}