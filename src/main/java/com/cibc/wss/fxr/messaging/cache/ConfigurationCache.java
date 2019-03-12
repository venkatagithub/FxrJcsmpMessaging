package com.cibc.wss.fxr.messaging.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cibc.fxr.communication.config.ConfigurationModel;


public class ConfigurationCache {
	
	private  static Logger logger = LoggerFactory.getLogger(ConfigurationCache.class);
		
	private ConfigurationModel configuration = null; 
	
	public synchronized ConfigurationModel getConfiguration() {
		return configuration;
	}

	public synchronized void setConfiguration(ConfigurationModel configuration) {		
		this.configuration = configuration;
		logger.info("FxrConfiguration model has been set successfully.");
	}
	
}