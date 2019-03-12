package com.cibc.wss.fxr.messaging.solcache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.interfaces.IMessageListener;

import com.cibc.fx.template.realtime.IConsumer;
import com.cibc.fx.template.realtime.IEvent;
import com.cibc.fx.template.realtime.IPublisher;

public abstract class BaseListener implements IMessageListener, IPublisher  {

	private  static Logger logger = LoggerFactory.getLogger(ConfigurationListener.class);
	
	Map<String, IConsumer> consumers = new HashMap<String, IConsumer>();
	
	@Override
	public void subscribe(IConsumer consumer) {
		consumers.put(consumer.getName(), consumer);		
	}

	@Override
	public void notify(IEvent event) {
		logger.info("notifying event " + event);
		
		Iterator<String> iter = consumers.keySet().iterator();		
		while(iter.hasNext()) {
			consumers.get(iter.next()).receive(event);			
		}
	}
}