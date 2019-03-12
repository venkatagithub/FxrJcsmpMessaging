package com.cibc.wss.fxr.messaging.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.cibc.fxr.communication.config.ConfigurationModel;
import com.cibc.fxr.marketrates.RatePublishModel;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class JSONConverter {
	
	final ObjectMapper mapper = new ObjectMapper();
	
	public static String convertToJsonString(Object clz) throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer();
		String jsonString = ow.writeValueAsString(clz);
		return jsonString;
	}

	public static <T> Object convertJsonStringToObject(String jsonString, Class<T> clz) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(dateFormat);
		Object o = mapper.readValue(jsonString, clz);
		return o;
	}
	
	public List<RatePublishModel> convertRatePublishModel(String jsonStr) throws JsonParseException, JsonMappingException, IOException  {
		List<RatePublishModel> ratePublishModelList;
		ratePublishModelList = Arrays.asList(mapper.readValue(jsonStr, RatePublishModel[].class));
		return ratePublishModelList;
	}
	
	public ConfigurationModel convertConfig(String cfgJsonStr) throws JsonParseException, JsonMappingException, IOException  {
		ConfigurationModel cfgModel;
		cfgModel = mapper.readValue(cfgJsonStr, ConfigurationModel.class);
		return cfgModel;
	}

}