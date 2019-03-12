package com.cibc.wss.fxr.messaging.solace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.MitSDTMap;
import ca.cibcwb.mit.messaging.QueueReceiver;
import ca.cibcwb.mit.messaging.properties.QueueProperties;

import com.cibc.fx.template.realtime.AbstractProcessor;
import com.cibc.wss.fxr.messaging.constants.MessagingConstants;
import com.cibc.wss.fxr.messaging.exception.SolaceMessagingException;
import com.cibc.wss.fxr.messaging.model.FxrMessage;
import com.cibc.wss.fxr.messaging.util.CommonUtils;


public abstract class QueueMessageReceiver extends AbstractProcessor{

	private SolaceSession solSession;
	private QueueReceiver queueReceiver;
	private String queueName;
	private int pullExceptionCount = 0;
	private QueueProperties queueProperties;
	
	private int MAX_SOLACE_EXCEPTION_COUNT = 30;
	
	public abstract boolean processMessage(FxrMessage fxrMessage) throws Exception;
	public abstract boolean handleMitMessageProcessingException(SolaceMessagingException solMsgingException );
	
	private  static Logger logger = LoggerFactory.getLogger(QueueMessageReceiver.class);
	
	public void run() {

		logger.info( "Starting Q-receiver processor [{}] ", getName() );
		try {
			
			initQueueReceiver();
			
			while(isRunning()){
				try {
					List<MitMessage> mitMsgList = queueReceiver.receiveMessages(MessagingConstants.MAX_MSG_COUNT_IN_A_PULL);
					
					if (mitMsgList != null && mitMsgList.size()>0){
						logger.info("No. of Messages received from the queue:[{}] is:[{}]",queueName,mitMsgList.size());
						pullExceptionCount = 0;
						
						for(MitMessage mitMsg : mitMsgList){
							boolean messageProcessed = false;
							
							try{
								
								FxrMessage fxrMessage = createFxrMsgFromMitMsg(mitMsg);
								logger.info("Message Id received:[{}]",fxrMessage.getMessageId());
								logger.info("Message received:[{}] ",fxrMessage,fxrMessage.getMessageId());
								
								messageProcessed = processMessage(fxrMessage);
								logger.info("Message processed successfully for the message Id:[{}]",fxrMessage.getMessageId());
								
							}catch(Exception e){
								
								if(logger.isDebugEnabled()){
									logger.debug("MIT Message received:[{}]",mitMsg);
							    }

								handleMsgProcessingException(e);
								//Setting status to true to avoid looping of same message in the queue
								messageProcessed = true; 
								
							}
							finally{
								
								if(messageProcessed){
									mitMsg.ackMessage();
								}
							}
							
						}
						
						// Rollback all non acked messages from the above list of mitMessages pulled
						queueReceiver.rollbackNonAckMessages();
					}
					
					CommonUtils.sleepInMillis(MessagingConstants.MESSAGE_RETREIVAL_FREQUENCY_IN_MILLIS);
				}
				catch (Throwable e){
					handleMessagePullExceptionFromQueue(e);
					pullExceptionCount++;
					
					//Added this condition to avoid any issue of running into infinite loop.
					if(pullExceptionCount > MAX_SOLACE_EXCEPTION_COUNT){
						setRunning(false);
					}
				}
			}
			
		} catch (Throwable t) {
			logger.error("Error/Exception while initializing the q-receiver for:{}. {}", queueName, t);
			
			String errorMessage = "Exception while initializing the q-receiver for "+queueName;
			handleMsgProcessingException(new SolaceMessagingException(MessagingConstants.SOLACE_MIT_Q_RECEIVER_INITIALIZATION_EXCEPTION,errorMessage,t));
		}
		finally{
			
			logger.info("Stopping the Q-Msg receiver processor:[{}]",this.getName());
			destroy();
			
		}
	}
	
	public void initQueueReceiver() throws Throwable {
	
		initializeQproperties();
		
		if(queueReceiver == null){
			queueReceiver = solSession.getMitSession().createQueueReceiver(queueProperties);
			logger.info("Successfully initialized Q-receiver for: [{}]",queueName);
		}
		else{
			logger.info("Q-receiver for the queue [{}] is already initialized",queueName);
		}
	}
	
	private synchronized void initializeQproperties(){
		
		logger.info("Initializing the Q-Receiver for: [{}]",queueName);
		if(queueProperties == null){
			queueProperties = new QueueProperties(queueName);
		}
	}
	
	private FxrMessage createFxrMsgFromMitMsg(MitMessage mitMsg) throws SolaceMessagingException {
		
		MitSDTMap mitSDTMap = mitMsg.getProperties();
		Map<String, Object> propertyMap = null;
		String msgId = mitMsg.getMessageId();
		String correlationId = mitMsg.getCorrelationId();
		String destinationname = mitMsg.getDestinationName();
		FxrMessage fxrMessage = null;
		
		try{
			if(mitSDTMap!= null && !mitSDTMap.isEmpty()){
				propertyMap = new HashMap<String, Object>();
				for(String key : mitSDTMap.keySet()){
					propertyMap.put(key, mitSDTMap.get(key));
				}
			}
			
			if(isDecompressionRequired(mitSDTMap)){
				mitMsg = CommonUtils.unCompressMessage(mitMsg);
			}
			
			fxrMessage = new FxrMessage(mitMsg.getString(), // msg string 
													propertyMap, // header map
													msgId, // message id
													correlationId, // correlation id
													destinationname); // destination name == Topic name through which the message came
		} catch (Throwable e) {
			logger.error("Error while converting MIT message into FxrMessage:{}",e);
			throw new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_CONVERSION_EXCEPTION,"Error while converting MIT message into FxrMessage",e);
		}
		
		return fxrMessage;
	}
	
	private boolean isDecompressionRequired(MitSDTMap mitSDTMap) throws Throwable{
		boolean decompressionRequired = false;
		if(mitSDTMap != null && mitSDTMap.get(MessagingConstants.COMPRESSION) != null 	&& mitSDTMap.get(MessagingConstants.COMPRESSION).equals(MessagingConstants.COMPRESSION_TRUE)){
			decompressionRequired = true;
		}
		
		logger.info("Decompression required: [{}]",decompressionRequired);
		return decompressionRequired;
	}

	private void handleMessagePullExceptionFromQueue(Throwable t){
		
/*		String errorMessage = "Exception/Error while pulling messages from the queue:"+ this.queueName;
		logger.error(errorMessage);
		logger.error("Exception/Error: {} ", t);
		boolean reconnected = false;*/
		
		String errorMessage = "handleMessagePullExceptionFromQueue:Exception/Error while pulling messages from the queue:"+ this.queueName;
		logger.error("handleMessagePullExceptionFromQueue:"+errorMessage);
		logger.error("handleMessagePullExceptionFromQueue:Exception/Error: {} ", t);
		
		try{
			if(pullExceptionCount<=2){
				String errorMsg = t.getMessage();
				handleMsgProcessingException(new Exception (errorMsg));
			}
			
			logger.info("Resetting the Q-Receiver:{}",queueName);
			if(solSession != null){
				//solSession.resetMitSession();
				closeQueueMessageReceiver();
				initQueueReceiver();
			}
		
		} catch (Throwable e) {
			logger.error("Exception while resetting Queue receiver for the queue:{}",queueName);
			logger.error("Exception/Error: {} ", e);
			pullExceptionCount++;
		}
		finally{
			logger.info("Sleeping for [{}] milli secs",MessagingConstants.MSG_RETRIEVAL_FREQUENCY_AFTER_EXCEPTION_IN_MILLIS);
			CommonUtils.sleepInMillis(MessagingConstants.MSG_RETRIEVAL_FREQUENCY_AFTER_EXCEPTION_IN_MILLIS);
		}
	}
	
	
	private void handleMsgProcessingException(Exception e){
		
		try{
			String errorMessage = "Exception/Error while pulling messages from the queue:"+ this.queueName;
			logger.error(errorMessage);
			logger.error("Exception/Error: {} ", e);
			handleMitMessageProcessingException(new SolaceMessagingException(MessagingConstants.SOLACE_MIT_MSG_PULL_EXCEPTION,errorMessage,e));
		}
		catch(Exception e1){
			logger.info("Exception thrown from message processing module:", e1);
		}

	}
	
	public void destroy(){
		
		setRunning(false);
		setTerminated(true);
		closeQueueMessageReceiver();
	}
	
	private void closeQueueMessageReceiver(){
		
		if(queueReceiver != null){
			queueReceiver.close();
			queueReceiver = null;
			logger.info("Queue msg receiver stopped for [{}]",getName());
		}
	}
		
	public SolaceSession getSolSession() {
		return solSession;
	}

	public void setSolSession(SolaceSession solSession) {
		this.solSession = solSession;
	}

	public QueueReceiver getQueueReceiver() {
		return queueReceiver;
	}
	public void setQueueReceiver(QueueReceiver queueReceiver) {
		this.queueReceiver = queueReceiver;
	}
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public QueueProperties getQueueProperties() {
		return queueProperties;
	}
	
	public void setQueueProperties(QueueProperties queueProperties) {
		this.queueProperties = queueProperties;
	}
	
}