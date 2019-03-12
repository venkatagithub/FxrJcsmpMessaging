package com.cibc.wss.fxr.messaging.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.MitSDTMap;
import ca.cibcwb.mit.messaging.MitSessionFactory;

import com.cibc.fxr.communication.config.FxrConstants;
import com.cibc.fxr.communication.enums.MessageType;
import com.cibc.wss.fxr.messaging.constants.MessagingConstants;



public class CommonUtils {
	
	private  static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
	
	public static byte[] compressTextMessage(String originalMessage) throws Exception{

		logger.debug("Compression started");
		ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(originalMessage.getBytes("UTF-8"));
        gzip.close();
        if(logger.isDebugEnabled()){
			logger.debug("Compression completed.");
		}
        return obj.toByteArray();
	}
	
	public static MitMessage unCompressMessage(MitMessage message) throws Throwable{
		Charset CHARSET = Charset.forName("UTF-8");
		MitMessage unCompressedMItMsg =  null;
		if(message != null){
			byte[] messageBytes = message.getBytes();		
			GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(messageBytes));
			byte[] buffer = new byte[1024 * 4];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
			messageBytes =  out.toByteArray();
			
			String unCompressedTextMsg = new String(messageBytes, CHARSET);
			unCompressedMItMsg = MitSessionFactory.getInstance().createMitMessage(unCompressedTextMsg);
			unCompressedMItMsg.setProperties(message.getProperties());
			unCompressedMItMsg.setDestinationTopic(message.getDestinationName());
		}
		return unCompressedMItMsg;
	} 
	
	public static MitMessage createNewMitMessage(String messageStr,Map<String, Object> headerProperties,MessageType msgType, String compressionEnabled) throws Throwable{
		logger.info("Creating MIT the message with compression :[{}]",compressionEnabled);
		 MitMessage mitMessage = null;
		 Serializable compressedMsg = null;
		 MitSDTMap mitSdtMap = MitSessionFactory.getInstance().createMitSDTMap();
		 if(headerProperties != null){
			 for (String key : headerProperties.keySet()) {
				 String value = headerProperties.get(key) != null ? headerProperties.get(key).toString() : null; 
				 mitSdtMap.putString(key, value);
			 }
		 }
		 mitSdtMap.putString(FxrConstants.MESSAGE_TYPE,msgType.getText());
		 if(compressionEnabled != null && compressionEnabled.equalsIgnoreCase("Y")){
			 compressedMsg =  CommonUtils.compressTextMessage(messageStr);
			 messageStr = (String) compressedMsg;
			 mitSdtMap.putString(MessagingConstants.COMPRESSION,MessagingConstants.COMPRESSION_TRUE);
		 }
		 mitMessage =  MitSessionFactory.getInstance().createMitMessage(messageStr);
		 mitMessage.setProperties(mitSdtMap);

		return mitMessage;
	}
	
	public static void sleepInMillis(int sleepMillis){
		try {
			TimeUnit.MILLISECONDS.sleep(sleepMillis);
		} catch (InterruptedException e) {
			logger.error("InterruptedException while sleeping before pulling the message from the queue:{}", e);
		}
	}
}