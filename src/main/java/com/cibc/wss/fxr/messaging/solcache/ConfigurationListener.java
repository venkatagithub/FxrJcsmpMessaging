package com.cibc.wss.fxr.messaging.solcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.interfaces.IMessageListener;

import com.cibc.fxr.communication.config.ConfigurationModel;
import com.cibc.wss.fxr.messaging.cache.ConfigurationCache;
import com.cibc.wss.fxr.messaging.cache.ConfigurationChangeEvent;
import com.cibc.wss.fxr.messaging.util.JSONConverter;
import com.solacesystems.jcsmp.CacheRequestResult;
import com.solacesystems.jcsmp.Topic;

public class ConfigurationListener extends BaseListener implements IMessageListener {

	private  static Logger logger = LoggerFactory.getLogger(ConfigurationListener.class);
	
	private boolean isCacheRequestComplete;
	private long receiveCount;
	
	private ConfigurationCache configCache;
	
	public ConfigurationListener () {
		receiveCount = 0;
	}

	public void onCacheRequestComplete(Long requestId, Topic topic, CacheRequestResult result) throws Throwable {
		isCacheRequestComplete = true;
	}

	public void onException(Exception e) {
		logger.error("MessageListener.onException Got exception:[{}] ", e.getMessage());
	}

	public void onReceive(MitMessage mitMsg) throws Throwable {
	
		String cfgJsonStr = mitMsg.getString();			
		logger.info(mitMsg.getDestinationName() +  " : Configuration Message received: " + cfgJsonStr);
		
		try {
			ConfigurationModel config = (ConfigurationModel) JSONConverter.convertJsonStringToObject(cfgJsonStr, ConfigurationModel.class);
				
			if(config != null) {
				ConfigurationChangeEvent event = new ConfigurationChangeEvent();
				event.setData(config);
				notify(event);					
			} else {
				logger.error("Incorrect Configuration Published");
			}							
			
		} catch(Exception e) {
			logger.error("Error processing configuration message", e);
		}
		
	}
	
	public void resetIsCacheRequestComplete() { isCacheRequestComplete = false; }
	
    public boolean getIsCacheRequestComplete() { return isCacheRequestComplete; }
    
    public long getMsgCount() { return receiveCount; }
    
    public void resetMsgCount() { receiveCount = 0; }

	public void resetAll() {
		/*
		 *  Resets the cache request status and count for number of messages received
		 */
		resetIsCacheRequestComplete();
		resetMsgCount();
	}

	public ConfigurationCache getConfigCache() {
		return configCache;
	}

	public void setConfigCache(ConfigurationCache configCache) {
		this.configCache = configCache;
	}
	
	
	
}