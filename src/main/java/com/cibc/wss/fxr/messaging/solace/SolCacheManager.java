package com.cibc.wss.fxr.messaging.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cibc.fx.template.realtime.AbstractProcessor;

import ca.cibcwb.mit.messaging.CacheLiveDataAction;
import ca.cibcwb.mit.messaging.SolCacheSubscriber;
import ca.cibcwb.mit.messaging.properties.TopicProperties;


public class SolCacheManager extends AbstractProcessor{

	private SolCacheListener solCacheListener;
	private TopicProperties solCacheTopicProperties;
	private SolaceSession solSession;
	private String solCacheName;
	private SolCacheSubscriber solCacheSubscriber;
	
	private  static Logger logger = LoggerFactory.getLogger(SolCacheManager.class);
	
	public SolCacheListener getSolCacheListener() {
		return solCacheListener;
	}

	public void setSolCacheListener(SolCacheListener solCacheListener) {
		this.solCacheListener = solCacheListener;
	}

	public TopicProperties getSolCacheTopicProperties() {
		return solCacheTopicProperties;
	}

	public void setSolCacheTopicProperties(TopicProperties solCacheTopicProperties) {
		this.solCacheTopicProperties = solCacheTopicProperties;
	}

	public String getSolCacheName() {
		return solCacheName;
	}

	public void setSolCacheName(String solCacheName) {
		this.solCacheName = solCacheName;
	}

	public SolaceSession getSolSession() {
		return solSession;
	}


	public void setSolSession(SolaceSession solSession) {
		this.solSession = solSession;
	}

	public SolCacheSubscriber getSolCacheSubscriber() {
		return solCacheSubscriber;
	}

	public void setSolCacheSubscriber(SolCacheSubscriber solCacheSubscriber) {
		this.solCacheSubscriber = solCacheSubscriber;
	}

	public void setupSolCacheListener() throws Throwable{
		solCacheTopicProperties.setCacheName(solCacheName);
		solCacheTopicProperties.setMessageListener(solCacheListener);

		solCacheSubscriber = solSession.getMitSession().createSolCacheSubscriber(solCacheTopicProperties);
		solCacheSubscriber.sendCacheRequest(0, CacheLiveDataAction.QUEUE, true, 3000);
		logger.info("Initialization completed successfulle for the solcache [{}] for the topic [{}]",solCacheName,solCacheTopicProperties.getTopicName());
	}
	
	public void run() {
		 
		try {
			setupSolCacheListener() ;
		} catch (Throwable e) {
			logger.error("Error while setting up the sol cache listener ",e);
		}
	}
	
	@Override
	public void start() {
	 
		if(isRunning()) {
			logger.info("The processor [{}] is already running ", getName()  );			
		} else {
			logger.info("Starting the processor [{}] ",getName());
			setRunning(true);		
			setTerminated(false);
			run();
		}
	}
	
	@Override
	public void stop() {
		
		try {		
			if(isRunning()) {
				logger.info(" Stopping the processor:[{}] ",getName() );
				setRunning(false);
				stopSolCacheListener();			
			} else {
				logger.warn("The processor:[{}] is not running",getName());
			}
		}catch(Exception e) {
			logger.error("Error stopping processor: "+ getName());
		}finally {
			setTerminated(true);
		}
	}
	
	public void stopSolCacheListener() {
		try {
			if(solCacheSubscriber != null){
				solCacheSubscriber.close();
			}
			setTerminated(true);
		}catch(Throwable e){
			logger.error("Error while stopping sol cache listener.", e);
		}
		
	}
}