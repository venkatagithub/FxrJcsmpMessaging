package com.cibc.wss.fxr.messaging.solace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cibc.fx.template.realtime.AbstractProcessor;
import com.cibc.wss.fxr.messaging.constants.MessagingConstants;
import com.cibc.wss.fxr.messaging.model.FxrMessage;
import com.cibc.wss.fxr.messaging.util.CommonUtils;

import ca.cibcwb.mit.messaging.MitMessage;
import ca.cibcwb.mit.messaging.MitSDTMap;
import ca.cibcwb.mit.messaging.MitTxSession;
import ca.cibcwb.mit.messaging.TxQueueReceiver;
import ca.cibcwb.mit.messaging.properties.QueueProperties;


public abstract class TxQueueMsgReceiver extends AbstractProcessor{
	
	private SolaceSession solSession;
	private MitTxSession mitTxSession;
	private String queueName;
	private TxQueueReceiver txQueueReceiver;
	private QueueProperties queueProperties;
	private Thread txQReceiverThread;

	public abstract void processMessage(FxrMessage fxrMessage);
	
	private  static Logger logger = LoggerFactory.getLogger(TxQueueMsgReceiver.class);
	
	public SolaceSession getSolSession() {
		return solSession;
	}

	public void setSolSession(SolaceSession solSession) {
		this.solSession = solSession;
	}

	public MitTxSession getMitTxSession() {
		return mitTxSession;
	}

	public void setMitTxSession(MitTxSession mitTxSession) {
		this.mitTxSession = mitTxSession;
	}

	public QueueProperties getQueueProperties() {
		return queueProperties;
	}

	public void setQueueProperties(QueueProperties queueProperties) {
		this.queueProperties = queueProperties;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public TxQueueReceiver getTxQueueReceiver() {
		return txQueueReceiver;
	}

	public void setTxQueueReceiver(TxQueueReceiver txQueueReceiver) {
		this.txQueueReceiver = txQueueReceiver;
	}

	public void run() {
		startProcessor();
	}
	
	@Override
	public void start() {
		txQReceiverThread = new Thread(this,getName());
		txQReceiverThread.start();
	}
	
	@Override
	public void stop() {
		if(isRunning()) {
			logger.info( "Stopping the Txn-Q receiver processor [{}]",getName());			
			setRunning(false);
		} else {
			logger.warn("[{}]  is not running ",getName());
		}
	}
	
	public void initTxQueueReceiver() throws Throwable {
		logger.info("Initializing the q-receiver for the Tx-Q [{}]",queueProperties.getQueueName());
		if(mitTxSession == null){
			mitTxSession = solSession.getMitSession().createMitTxSession();
		}
		if(txQueueReceiver == null){
			txQueueReceiver = mitTxSession.createTxQueueReceiver(queueProperties);
		}
		
	}
	
	private void startProcessor()  {
		if(isRunning()) {
			logger.info("[{}] is already running ",getName());
		}
		else{
			logger.info( "Starting the Tx-Q receiver processor [{}] ",getName()  );
			setRunning(true);
			setTerminated(false);
			
			try {
				initTxQueueReceiver();
				while(isRunning()){
					try{
						List<MitMessage> mitMsgList = txQueueReceiver.receiveMessages(1);
						if(mitMsgList != null && mitMsgList.size()>0){
							MitMessage mitMsg = mitMsgList.get(0);
							
							MitSDTMap mitSDTMap = mitMsg.getProperties();
							Map<String, Object> propertyMap = null;
							
							if(mitSDTMap!= null && !mitSDTMap.isEmpty()){
								propertyMap = new HashMap<String, Object>();
								for(String key : mitSDTMap.keySet()){
									propertyMap.put(key, mitSDTMap.get(key));
								}
							}
							
							if(mitSDTMap != null && mitSDTMap.get(MessagingConstants.COMPRESSION) != null 
									&& mitSDTMap.get(MessagingConstants.COMPRESSION).equals(MessagingConstants.COMPRESSION_TRUE)){
								
								logger.info("Decompression required: [{}]",mitSDTMap.get(MessagingConstants.COMPRESSION));
								mitMsg = CommonUtils.unCompressMessage(mitMsg);
							}
							
							FxrMessage fxrMessage = new FxrMessage(mitMsg.getString(), // msg string 
																	propertyMap, // header map
																	mitMsg.getMessageId(), // message id
																	mitMsg.getCorrelationId(), // correlation id
																	mitMsg.getDestinationName()); // destination name
							processMessage(fxrMessage);
							
							mitTxSession.commit();
						}
						else{
							TimeUnit.MILLISECONDS.sleep(1);
						}
					} catch (Throwable t) {
						logger.error("Error/exception occured while processing message from transacted session Tx-Q:[{}]", queueProperties.getQueueName(), t);
						try {
							mitTxSession.rollback();
						} catch (Throwable t1) {
							logger.error("Error/exception occured while rolling back message to Tx-Q:[{}]", queueProperties.getQueueName(), t1);
							if(t1 instanceof Error){
								logger.error("Stopping receiver thread {} for Tx-Q:[{}]", getName(), queueProperties.getQueueName());
								setRunning(false);
							}
						}
						if(t instanceof Error){
							logger.error("Stopping receiver thread {} for Tx-Q:[{}]", getName(), queueProperties.getQueueName());
							setRunning(false);
						}
					}
				}
			}catch (Throwable t) {
				logger.error("Error/exception occured while initializing the Tx-Q receiver for Q:[{}] ",queueProperties.getQueueName(),t);
				if(t instanceof Error){
					logger.error("Stopping receiver thread {} for Tx-Q:[{}]", getName(), queueProperties.getQueueName());
					setRunning(false);
				}
			}
			
		}
	}
	
	public void destroy(){
		if(txQueueReceiver != null){
			txQueueReceiver.close();
		}
		if(mitTxSession != null){
			mitTxSession.close();
		}
	}
}