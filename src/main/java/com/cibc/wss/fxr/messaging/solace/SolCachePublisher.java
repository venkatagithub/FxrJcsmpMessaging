package com.cibc.wss.fxr.messaging.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.MitSessionFactory;
import ca.cibcwb.mit.messaging.TopicPublisher;
import ca.cibcwb.mit.messaging.properties.TopicProperties;

import com.cibc.wss.fxr.messaging.constants.MessagingConstants;


public class SolCachePublisher {
	
	private SolaceSession solSession;
	private TopicPublisher solCacheTopicPublisher;
	private TopicProperties solCacheTopicProperties;
	private String solCacheName;
	private String compressionEnabled;
	
	private  static Logger logger = LoggerFactory.getLogger(SolaceTopicPublisher.class);
	
	public SolaceSession getSolSession() {
		return solSession;
	}

	public void setSolSession(SolaceSession solSession) {
		this.solSession = solSession;
	}

	public String getSolCacheName() {
		return solCacheName;
	}

	public void setSolCacheName(String solCacheName) {
		this.solCacheName = solCacheName;
	}

	public TopicPublisher getSolCacheTopicPublisher() {
		return solCacheTopicPublisher;
	}

	public void setSolCacheTopicPublisher(TopicPublisher solCacheTopicPublisher) {
		this.solCacheTopicPublisher = solCacheTopicPublisher;
	}

	public TopicProperties getSolCacheTopicProperties() {
		return solCacheTopicProperties;
	}

	public void setSolCacheTopicProperties(TopicProperties solCacheTopicProperties) {
		this.solCacheTopicProperties = solCacheTopicProperties;
	}

	public String getCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(String compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public void initialize() throws Throwable {
		if(solCacheTopicProperties != null){
			solCacheTopicProperties.setCacheName(solCacheName);
			solCacheTopicPublisher = this.getSolSession().getMitSession().createTopicPublisher(solCacheTopicProperties);    
		} 
	}
	
	public void publishSolCaheMessage(String message) throws Throwable{
		logger.info("publishing the message to solcache with compression :[{}]",compressionEnabled);
		 MitMessage mitMessage = null;
		 if(compressionEnabled != null && compressionEnabled.equalsIgnoreCase("Y")){
			 mitMessage =  MitSessionFactory.getInstance().createMitMessage(message);
			 
			  
			 
			 mitMessage.getProperties().putString(MessagingConstants.COMPRESSION,MessagingConstants.COMPRESSION_TRUE);
		 }
		 else{
			 mitMessage = MitSessionFactory.getInstance().createMitMessage(message);
		 }
		
		 solCacheTopicPublisher.publish(mitMessage);  
	}
	
	public void destroy(){
		logger.info("Closing the solcache topic publisher");
		if(solCacheTopicPublisher != null){
			solCacheTopicPublisher.close();
			logger.info("Solcache Topic publisher closed for:[{}] in the solcache:[{}]",solCacheTopicProperties.getTopicName(),solCacheTopicProperties.getCacheName());
		}
	}
}