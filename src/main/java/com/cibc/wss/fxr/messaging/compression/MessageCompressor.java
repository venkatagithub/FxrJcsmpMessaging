package com.cibc.wss.fxr.messaging.compression;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class MessageCompressor {

	private  static Logger logger = LoggerFactory.getLogger(MessageCompressor.class);
	
	public byte[] doCompression(String textMessage) throws Exception{
		if(logger.isDebugEnabled()){
			logger.debug("Compression started");
		}
		ByteArrayOutputStream obj=new ByteArrayOutputStream();
	    GZIPOutputStream gzip = new GZIPOutputStream(obj);
	    gzip.write(textMessage.getBytes("UTF-8"));
	    gzip.close();
	    if(logger.isDebugEnabled()){
			logger.debug("Compression completed.");
		}
	    return obj.toByteArray();
	}
}