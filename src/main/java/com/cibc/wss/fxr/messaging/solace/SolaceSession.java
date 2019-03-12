package com.cibc.wss.fxr.messaging.solace;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cibcwb.mit.messaging.MitSession;
import ca.cibcwb.mit.messaging.MitSessionFactory;
import ca.cibcwb.mit.messaging.interfaces.IPublishEventHandler;
import ca.cibcwb.mit.messaging.interfaces.ISessionEventHandler;
import ca.cibcwb.mit.messaging.properties.MitSessionConfiguration;

import com.cibc.wss.fxr.messaging.constants.MessagingConstants;
import com.cibc.wss.fxr.messaging.exception.SolaceMessagingException;
import com.cibc.wss.fxr.messaging.util.Constants;


public class SolaceSession {
	
	private MitSession mitSession;
	private MitSessionConfiguration mitSessionConfig;
	
	/**
	 * solace jcsmp properties from files -
	 */
	private String userName;
	private String password;
	private String host;
	private String vpnName;
	private String pubAckWindowSize;
	private String subAckWindowSize;
	private String reapplySubscriptions;
	private String generateSendTimestamps;
	private String ccpSendBuffer;
	private String ccpReceiveBuffer;
	private String ccpRecconectRetries;
	private String acceptCurrentDayMsgs;
	
	
	public ISessionEventHandler solSessionEvntHandler;
	public IPublishEventHandler publishEvntHandler;
	
	private  static Logger logger = LoggerFactory.getLogger(SolaceSession.class);
	
	@PostConstruct
	public void init() throws SolaceMessagingException {
		Properties credentialConfiguration = new Properties();
		credentialConfiguration.put(Constants.JCSMP_USERNAME, userName);
		credentialConfiguration.put(Constants.JCSMP_PASSWORD, password);
		
		Properties messagingConfiguration  = new Properties();
		messagingConfiguration.put(Constants.JCSMP_HOST, host);
		messagingConfiguration.put(Constants.JCSMP_VPNNAME, vpnName);
		messagingConfiguration.put(Constants.JCSMP_PUB_ACK_WINDOW_SIZE, pubAckWindowSize);
		messagingConfiguration.put(Constants.JCSMP_SUB_ACK_WINDOW_SIZE, subAckWindowSize);
		messagingConfiguration.put(Constants.JCSMP_REAPPLY_SUBSCRIPTIONS, reapplySubscriptions);
		messagingConfiguration.put(Constants.JCSMP_GENERATE_SEND_TIMESTAMPS, generateSendTimestamps);
		
		messagingConfiguration.put(Constants.JCSMP_CCP_SENDBUFFER, ccpSendBuffer);
		messagingConfiguration.put(Constants.JCSMP_CCP_RECEIVEBUFFER, ccpReceiveBuffer);
		messagingConfiguration.put(Constants.JCSMP_CCP_RECONNECTRETRIES, ccpRecconectRetries);
		messagingConfiguration.put(Constants.JCSMP_ACCEPT_CURRENT_DAY_MSGS, acceptCurrentDayMsgs);
		
		
		mitSessionConfig = new MitSessionConfiguration(Constants.JCSMP_MSGING_PROPERTIES, messagingConfiguration, Constants.JCSMP_CREDENTIAL_PROPERTIES, credentialConfiguration);
		mitSessionConfig.setSessionEventHandler(solSessionEvntHandler);
		mitSessionConfig.setPublishEventHandler(publishEvntHandler);
		logger.debug("Creating MIT session for user [{}]", userName);
		createNewMitSession();
		
		logger.info("Solace Session has been created successfully for the user id:[{}]",userName);
	}
	
	private synchronized void createNewMitSession() throws SolaceMessagingException{
		
		try {
			if(mitSession == null){
				mitSession = MitSessionFactory.getInstance().createMitSession(mitSessionConfig);
				logger.info("New MIT Session created successfully");
			}
		}
		 catch (Throwable t) {
			logger.error("Failed to create MIT Session from the given configuration", t);
			throw new SolaceMessagingException(MessagingConstants.SOLACE_INITIALIZATION_EXCEPTION,"Failed to create MIT Session from the given JCSMP configuration.",t);
		}
	}
	
	public void resetMitSession() throws SolaceMessagingException{
		if(mitSession != null){
			mitSession.close();
		}
		createNewMitSession();
	}
	
	public void destroy(){
		logger.info("Closing the MIT Session");
		if(mitSession != null){
			mitSession.close();
			logger.info(" MIT Session closed successfully for the user id:[{}]",userName);
		}
	}
	
	public MitSession getMitSession() {
		return mitSession;
	}
	
	public void setMitSession(MitSession mitSession) {
		this.mitSession = mitSession;
	}

	public MitSessionConfiguration getMitSessionConfig() {
		return mitSessionConfig;
	}

	public void setMitSessionConfig(MitSessionConfiguration mitSessionConfig) {
		this.mitSessionConfig = mitSessionConfig;
	}

	public ISessionEventHandler getSolSessionEvntHandler() {
		return solSessionEvntHandler;
	}

	public void setSolSessionEvntHandler(ISessionEventHandler solSessionEvntHandler) {
		this.solSessionEvntHandler = solSessionEvntHandler;
	}

	public IPublishEventHandler getPublishEvntHandler() {
		return publishEvntHandler;
	}

	public void setPublishEvntHandler(IPublishEventHandler publishEvntHandler) {
		this.publishEvntHandler = publishEvntHandler;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getVpnName() {
		return vpnName;
	}

	public void setVpnName(String vpnName) {
		this.vpnName = vpnName;
	}

	public String getPubAckWindowSize() {
		return pubAckWindowSize;
	}

	public void setPubAckWindowSize(String pubAckWindowSize) {
		this.pubAckWindowSize = pubAckWindowSize;
	}

	public String getSubAckWindowSize() {
		return subAckWindowSize;
	}

	public void setSubAckWindowSize(String subAckWindowSize) {
		this.subAckWindowSize = subAckWindowSize;
	}

	public String getReapplySubscriptions() {
		return reapplySubscriptions;
	}

	public void setReapplySubscriptions(String reapplySubscriptions) {
		this.reapplySubscriptions = reapplySubscriptions;
	}

	public String getGenerateSendTimestamps() {
		return generateSendTimestamps;
	}

	public void setGenerateSendTimestamps(String generateSendTimestamps) {
		this.generateSendTimestamps = generateSendTimestamps;
	}

	public String getCcpSendBuffer() {
		return ccpSendBuffer;
	}

	public void setCcpSendBuffer(String ccpSendBuffer) {
		this.ccpSendBuffer = ccpSendBuffer;
	}

	public String getCcpReceiveBuffer() {
		return ccpReceiveBuffer;
	}

	public void setCcpReceiveBuffer(String ccpReceiveBuffer) {
		this.ccpReceiveBuffer = ccpReceiveBuffer;
	}

	public String getCcpRecconectRetries() {
		return ccpRecconectRetries;
	}

	public void setCcpRecconectRetries(String ccpRecconectRetries) {
		this.ccpRecconectRetries = ccpRecconectRetries;
	}

	public String getAcceptCurrentDayMsgs() {
		return acceptCurrentDayMsgs;
	}

	public void setAcceptCurrentDayMsgs(String acceptCurrentDayMsgs) {
		this.acceptCurrentDayMsgs = acceptCurrentDayMsgs;
	}
	
	
}