package org.cohorte.herald.mqtt;

/**
 * @author Drem Darios
 */
public class MqttExtra {

	/** MQTT broker host */
	private final String pHost;

	/** UID of the message we reply to */
	private final String pParentUid;

	/** The client ID of the MQTT connection */
	private final String pClientId;

	/** The topic to send messages */
	private final String pTopic;

	/** Sender MQTT port */
	private final int pPort;

	public MqttExtra(final String aHost, final int aPort,
			final String aClientId, final String aTopic, final String aParentUid) {
		pHost = aHost;
		pPort = aPort;
		pClientId = aClientId;
		pTopic = aTopic;
		pParentUid = aParentUid;
	}

	/**
	 * @return the host
	 */
	public String getHost() {

		return pHost;
	}

	/**
	 * @return the parentUid
	 */
	public String getParentUid() {

		return pParentUid;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {

		return pTopic;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {

		return pClientId;
	}

	/**
	 * @return the port
	 */
	public int getPort() {

		return pPort;
	}
}
