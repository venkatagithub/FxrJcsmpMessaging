package com.cibc.wss.fxr.messaging.solcache;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.TopicPublisher;
import ca.cibcwb.mit.messaging.properties.TopicProperties;

import com.cibc.wss.fxr.messaging.solace.SolaceTopicPublisher;

public class ConfigurationPublisher extends SolaceTopicPublisher{
	
	private String solCacheTopicName;
	private String cacheName;
	private TopicPublisher solCacheTopicPublisher ;
	
	private  static Logger logger = LoggerFactory.getLogger(ConfigurationPublisher.class);
 
	@PostConstruct
	public void init() throws Exception {
		try {
			TopicProperties solCacheTopicProperties  = new TopicProperties(solCacheTopicName);
			solCacheTopicProperties.setCacheName(cacheName);
			
			solCacheTopicPublisher = this.getSolSession().getMitSession().createTopicPublisher(solCacheTopicProperties);
			logger.info("Solcache Topic publisher initialized successfully for:[{}] in the cache:[{}]",solCacheTopicName,cacheName);
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw (Error)t;
		}
		
	}
	
	public void publish(String message) throws Throwable {
		
		logger.info("publishing the configuration to topic:[{}] in the cache:[{}]",solCacheTopicName,cacheName);
		
		if(solCacheTopicPublisher != null){
			publishConfiguration(message,solCacheTopicPublisher,false);
		}
	}

	public void destroy(){
		if(solCacheTopicPublisher != null){
			solCacheTopicPublisher.close();
		}
		logger.info("Sol cache Topic publisher to the topic:{} closed successfully for the cache {}",solCacheTopicName,cacheName);
	}

	public String getSolCacheTopicName() {
		return solCacheTopicName;
	}

	public void setSolCacheTopicName(String solCacheTopicName) {
		this.solCacheTopicName = solCacheTopicName;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public TopicPublisher getSolCacheTopicPublisher() {
		return solCacheTopicPublisher;
	}

	public void setSolCacheTopicPublisher(TopicPublisher solCacheTopicPublisher) {
		this.solCacheTopicPublisher = solCacheTopicPublisher;
	}

}