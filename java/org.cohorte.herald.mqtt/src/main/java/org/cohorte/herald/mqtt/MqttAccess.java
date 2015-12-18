package org.cohorte.herald.mqtt;

import org.cohorte.herald.Access;

/**
 * @author Drem Darios
 */
public class MqttAccess extends Access {

	/**
	 * Creates a bean from the result {@link #dump()}
	 *
	 * @param aDump
	 *            The result of {@link #dump()}
	 * @return The created bean, or null
	 */
	public static MqttAccess load(final Object aDump) {

		if (aDump instanceof Object[] && ((Object[]) aDump).length == 3) {

			final Object[] dump = (Object[]) aDump;
			final String host = (String) dump[0];
			final Object rawPort = dump[1];
			final String clientId = (String) dump[2];
			final String topic = (String) dump[3];

			// Convert the port to an integer
			final int port;
			if (rawPort instanceof Integer) {
				port = (Integer) rawPort;
			} else {
				port = Integer.valueOf((String) rawPort);
			}

			return new MqttAccess(host, port, clientId, topic);
		}

		// Unreadable content
		return null;
	}

	/** The MQTT host name */
	private final String pHost;

	/** MQTT server port */
	private final int pPort;

	/** Client id to the MQTT broker */
	private final String pClientId;

	/** MQTT broker topic */
	private final String pTopic;

	/**
	 * Sets up the access
	 *
	 * @param aHost
	 *            MQTT server host
	 * @param aPort
	 *            MQTT server port
	 * @param aClientId
	 *            Client Id of the MQTT broker
	 * @param aTopic
	 *            Topic to communicate on
	 */
	public MqttAccess(final String aHost, final int aPort,
			final String aClientId, final String aTopic) {
		pHost = aHost;
		pPort = aPort;
		pClientId = aClientId;
		pTopic = aTopic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Access aOther) {
		return this.equals(aOther) ? 0 : -1;
	}

	@Override
	public Object dump() {
		return new Object[] { pHost, pPort, pClientId };
	}

	@Override
	public String getAccessId() {
		return IMqttConstants.ACCESS_ID;
	}

	/**
	 * Retrieves the host address of the associated peer
	 *
	 * @return the host
	 */
	public String getHost() {

		return pHost;
	}

	/**
	 * Retrieves the client id to the MQTT broker.
	 *
	 * @return the path
	 */
	public String getClientId() {

		return pClientId;
	}

	/**
	 * Retrieves the host port of the associated peer
	 *
	 * @return the port
	 */
	public int getPort() {

		return pPort;
	}

	/**
	 * Retrieves the broker topic of the associated peer
	 *
	 * @return the topic
	 */
	public String getTopic() {

		return pTopic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((pClientId == null) ? 0 : pClientId.hashCode());
		result = prime * result + ((pHost == null) ? 0 : pHost.hashCode());
		result = prime * result + pPort;
		result = prime * result + ((pTopic == null) ? 0 : pTopic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MqttAccess other = (MqttAccess) obj;
		if (pClientId == null) {
			if (other.pClientId != null)
				return false;
		} else if (!pClientId.equals(other.pClientId))
			return false;
		if (pHost == null) {
			if (other.pHost != null)
				return false;
		} else if (!pHost.equals(other.pHost))
			return false;
		if (pPort != other.pPort)
			return false;
		if (pTopic == null) {
			if (other.pTopic != null)
				return false;
		} else if (!pTopic.equals(other.pTopic))
			return false;
		return true;
	}

}
