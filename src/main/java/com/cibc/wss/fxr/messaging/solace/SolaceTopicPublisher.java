package com.cibc.wss.fxr.messaging.solace;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.MitSDTMap;
import ca.cibcwb.mit.messaging.MitSessionFactory;
import ca.cibcwb.mit.messaging.TopicPublisher;
import ca.cibcwb.mit.messaging.properties.TopicProperties;

import com.cibc.fxr.communication.config.FxrConstants;
import com.cibc.fxr.communication.enums.MessageType;
import com.cibc.wss.fxr.messaging.constants.MessagingConstants;
import com.cibc.wss.fxr.messaging.exception.SolaceMessagingException;
import com.cibc.wss.fxr.messaging.util.CommonUtils;


public class SolaceTopicPublisher {
	
	private SolaceSession solSession;
	private String compressionEnabled;
	private List<String> topicList;
	private Map<String, TopicPublisher> allTopicPublishers;
	private Map<String, TopicProperties> allTopicProperties;
	
	private  static Logger logger = LoggerFactory.getLogger(SolaceTopicPublisher.class);
	
	@PostConstruct
	public void init() throws Exception{

		if(topicList != null && topicList.size()>0) {
			
			allTopicProperties = new HashMap<String, TopicProperties>();
			allTopicPublishers = new HashMap<String, TopicPublisher>();
			
			for(String topicName:topicList){
				
				initializeTopicProperties(topicName);
				TopicPublisher mitTopicPublisher = createTopicPublisher(topicName);
				allTopicPublishers.put(topicName, mitTopicPublisher);
				logger.info("Topic publisher initialized successfully for:[{}]",topicName);
				
			}
		}
	}
	
	private synchronized void initializeTopicProperties(String topicName){
		
		logger.info("Initializing the topic properties for:{}",topicName);
		if(allTopicProperties.get(topicName) == null){
			TopicProperties topicProperties  = new TopicProperties(topicName);
			allTopicProperties.put(topicName, topicProperties);
		}
	}
	
	protected TopicPublisher createTopicPublisher(String topicName) throws Exception{
		try {
			logger.info("Creating topic publisher for:{}",topicName);
			TopicProperties topicProperties = allTopicProperties.get(topicName);
			
			if(topicProperties == null){
				throw new Exception("Failed to initialize topic properties");
			}
			TopicPublisher mitTopicPublisher = this.getSolSession().getMitSession().createTopicPublisher(topicProperties);
			
			return mitTopicPublisher;
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			// theoretically, if f instanceof Exception, it would be caught by the previous catch block
			throw (Error)t;
		}
	}
	
	public void publishMessage(String message, Map<String, Object> headerProperties, MessageType messageType,  String topicName) throws Exception {
		
		logger.info("publishing the message with compression :[{}] to the topic:[{}]",compressionEnabled,topicName);
		MitMessage mitMessage = null;
		
		try {
			
			 logger.info("Message to be published: [{}]",message);

			 MitSDTMap mitSdtMap = MitSessionFactory.getInstance().createMitSDTMap();
			 if(headerProperties != null){
				 for (String key : headerProperties.keySet()) {
					 String value = headerProperties.get(key) != null ? headerProperties.get(key).toString() : null; 
					 mitSdtMap.putString(key, value);
				 }
			 }
			 
			 if(messageType != null){
				 mitSdtMap.putString(FxrConstants.MESSAGE_TYPE,messageType.getText());
			 }
			 
			 mitMessage = createMITMessage(message,mitSdtMap);

			 TopicPublisher topicPublisher = getRequiredTopicPublisher(topicName);
			 if(topicPublisher != null ){
				 topicPublisher.publish(mitMessage);
				 logger.info("Message published successfully to the Topic:[{}]",topicName);
			 }
		}catch (Throwable t) {
			logger.error("Error while publishing the message to the topic:{} - {}",topicName,t);
			handlePublishingException(topicName,mitMessage,t);	
		}
	}
	
	private void handlePublishingException(String topicName,MitMessage mitMessage,Throwable t) throws SolaceMessagingException{
		
		logger.info("Trying to republish the message after restting the topic publisher");
		if(allTopicPublishers == null){
			throw new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_PUBLISH_EXCEPTION,"Failed to reset the topic publisher",t);
		}
			
		try {
			TopicPublisher topicPublisher = getRequiredTopicPublisher(topicName);
			if(topicPublisher != null){			
				topicPublisher.close();
				allTopicPublishers.remove(topicName);
				topicPublisher = null;
				
				topicPublisher = createTopicPublisher(topicName);
				if(topicPublisher != null){
					allTopicPublishers.put(topicName, topicPublisher);
					topicPublisher.publish(mitMessage);
				}
				else{
					throw new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_PUBLISH_EXCEPTION,"Failed to reset the topic publisher for resending the message",t);
				}
			}

		} catch (Throwable e) {
			
			logger.error("Error while re-publishing the message to the topic:{} - {}",topicName,e);
			String errorMessage = "Error while publishing the message to the topic: "+topicName+ ". "+e.getMessage();
			throw new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_PUBLISH_EXCEPTION,errorMessage,e);

		}
	}
	
	private  MitMessage createMITMessage(String message,MitSDTMap mitSdtMap) throws Throwable{
		
		 MitMessage mitMessage = null;
		 Serializable mitMsg = message;
		 
		 if(mitSdtMap == null){
			 mitSdtMap = MitSessionFactory.getInstance().createMitSDTMap();
		 }
		 
		 if(compressionEnabled != null && compressionEnabled.equalsIgnoreCase("Y")){
			 logger.info("Compressing the message");
			 mitMsg =  CommonUtils.compressTextMessage(message);
			 mitSdtMap.putString(MessagingConstants.COMPRESSION,MessagingConstants.COMPRESSION_TRUE);
		 }
		 
		mitMessage = MitSessionFactory.getInstance().createMitMessage(mitMsg);
		mitMessage.setProperties(mitSdtMap);
		
		return mitMessage;
	}
	
	private TopicPublisher getRequiredTopicPublisher(String topicName){
		
		TopicPublisher topicPublisher = null;
		if(topicName != null){
			 topicPublisher = allTopicPublishers.get(topicName);
			 if(topicPublisher == null){
				logger.error("No topic publisher is configured for the topic:[{}]",topicName);
			 }
		}
		else{
			logger.error("Topic name is NULL:[{}]",topicName);
		}
		
		return topicPublisher;
	}

	public void publishConfiguration(String message, TopicPublisher solCacheTopicPublisher) throws Exception {
		publishConfiguration(message, solCacheTopicPublisher, true);
	}
	
	public void publishConfiguration(String message, TopicPublisher solCacheTopicPublisher, boolean printMessage) throws Exception {
		
		try {
			
			if (printMessage) {
			 logger.info("Message to be published: [{}]",message);
			}
			
			 MitMessage mitMessage = createMITMessage(message,null);
			 solCacheTopicPublisher.publish(mitMessage);
			 
		}catch (Throwable t) {
			
			logger.error("Error while publishing the Configuration",t);
			String errorMessage = "Error while publishing the Configuration"+t.getMessage();
			throw new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_PUBLISH_EXCEPTION,errorMessage,t);
			
		}
	}

	public void destroy(){
		
		for (Map.Entry<String, TopicPublisher> entry : allTopicPublishers.entrySet()) {
			entry.getValue().close();
			logger.info("Topic publisher closed for:{}",entry.getKey());
		}
	}
	
	public SolaceSession getSolSession() {
		return solSession;
	}

	public void setSolSession(SolaceSession solSession) {
		this.solSession = solSession;
	}

	public String getCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(String compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public List<String> getTopicList() {
		return topicList;
	}

	public void setTopicList(List<String> topicList) {
		this.topicList = topicList;
	}

	public Map<String, TopicPublisher> getAllTopicPublishers() {
		return allTopicPublishers;
	}

	public void setAllTopicPublishers(Map<String, TopicPublisher> allTopicPublishers) {
		this.allTopicPublishers = allTopicPublishers;
	}

}