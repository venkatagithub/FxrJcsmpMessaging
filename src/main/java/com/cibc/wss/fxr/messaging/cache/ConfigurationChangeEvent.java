package com.cibc.wss.fxr.messaging.cache;
import java.util.Map;

import com.cibc.fx.template.realtime.EventType;
import com.cibc.fx.template.realtime.IEvent;
import com.cibc.fxr.communication.config.ConfigurationModel;

public class ConfigurationChangeEvent implements IEvent {

	private ConfigurationModel model;
	
	@Override
	public EventType getType() {
		return EventType.INFO;
	}

	@Override
	public Object getData() {
		return getModel();
	}
	
	public void setData(ConfigurationModel model) {
		setModel(model);
	}

	public ConfigurationModel getModel() {
		return model;
	}

	public void setModel(ConfigurationModel model) {
		this.model = model;
	}
	
	public String toString() {
		return "ConfigurationChangeEvent {type: "+getType()+", data: " + getData() + "}"; 
	}

	@Override
	public Map<String, Object> getMessageMap() {
		// Add Configuration change message here if any.
		return null;
	}
}