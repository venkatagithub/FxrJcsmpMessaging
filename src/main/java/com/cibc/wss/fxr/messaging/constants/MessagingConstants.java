package com.cibc.wss.fxr.messaging.constants;


public class MessagingConstants {
	
	  public static final String COMPRESSION = "CMPRSN";
	  public static final String COMPRESSION_TRUE = "T";
	  
	  public static final int MESSAGE_RETREIVAL_FREQUENCY_IN_MILLIS = 1;
	  public static final int MSG_RETRIEVAL_FREQUENCY_AFTER_EXCEPTION_IN_MILLIS = 60000 ;
	  public static final int MAX_MSG_COUNT_IN_A_PULL = 255;
	  
	  public static final int MAX_SESSION_RESET_COUNT_BEFORE_FORCE_SHUTDOWN = 5;
	  
	  public static final int SOLACE_INITIALIZATION_EXCEPTION = 100;
	  public static final int SOLACE_MIT_MSG_CONVERSION_EXCEPTION = 101;
	  public static final int SOLACE_MIT_MSG_PULL_EXCEPTION = 102;
	  public static final int SOLACE_MIT_Q_RECEIVER_INITIALIZATION_EXCEPTION = 103;
	  
	  public static final int SOLACE_MIT_MSG_PUBLISH_EXCEPTION = 104;
}