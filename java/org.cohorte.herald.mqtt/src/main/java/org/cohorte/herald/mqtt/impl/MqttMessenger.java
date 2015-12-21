package org.cohorte.herald.mqtt.impl;

import org.cohorte.herald.Message;
import org.cohorte.herald.Peer;
import org.cohorte.herald.core.utils.MessageUtils;
import org.cohorte.herald.mqtt.IMqttConstants;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jabsorb.ng.serializer.MarshallException;

/**
 * @author Drem Darios
 */
public class MqttMessenger {

	private Peer pPeer;
	private MqttClient pClient;
	private String pClientId;
	private MqttConnectOptions pOptions;

	public MqttMessenger(Peer aPeer) {
		this.pPeer = aPeer;
		pOptions = new MqttConnectOptions();
	}

	
	public void fire(String peerUid, Message aMessage)
			throws MqttException, MarshallException {
		MqttMessage message = new MqttMessage();
		String content = MessageUtils.toJSON(aMessage);

		message.setPayload(content.getBytes());
		sendMessage(getUidTopic(peerUid), message);
	}
	
	public void fireGroup(String groupId, Message aMessage)
			throws MqttException, MarshallException {
		MqttMessage message = new MqttMessage();
		String content = MessageUtils.toJSON(aMessage);

		message.setPayload(content.getBytes());
		sendMessage(getGroupTopic(groupId), message);
	}
	
	public void setCallbackHandler(MqttCallback aCallback) {
		if (pClient != null) {
			pClient.setCallback(aCallback);
		} else {
			// FIXME: Add logging here
		}
	}
	
	public void login(String username, String password) {
		this.pOptions.setUserName(username);
		this.pOptions.setPassword(password.toCharArray());
	}
	
	public void connect(String host, Integer port) throws MqttException {

		this.pClientId = MqttClient.generateClientId();
		pOptions.setWill(getWillTopic(), pPeer.getUid().getBytes(), 1, false);
		pClient = new MqttClient(getConnectionUrl(host, port), pClientId);
		pClient.connect(pOptions);
	}

	public void disconnect() throws MqttException {
		pClient.disconnect();
	}
	
	private void sendMessage(String topic, MqttMessage message)
			throws MqttException {
		pClient.publish(topic, message);
	}
	
	private String getWillTopic() {
		StringBuilder topic = new StringBuilder(IMqttConstants.TOPIC_PREFIX);
		topic.append("/");
		topic.append(pPeer.getApplicationId());
		topic.append("/");
		topic.append(IMqttConstants.RIP_TOPIC);
		return topic.toString();
	}

	private String getUidTopic(String subtopic) {
		StringBuffer topic = new StringBuffer("/");
		topic.append(IMqttConstants.TOPIC_PREFIX);
		topic.append("/");
		topic.append(pPeer.getApplicationId());
		topic.append("/");
		topic.append(IMqttConstants.UID_TOPIC);
		topic.append("/");
		topic.append(subtopic);

		return topic.toString();
	}

	private String getGroupTopic(String subtopic) {
		StringBuffer topic = new StringBuffer("/");
		topic.append(IMqttConstants.TOPIC_PREFIX);
		topic.append("/");
		topic.append(pPeer.getApplicationId());
		topic.append("/");
		topic.append(IMqttConstants.GROUP_TOPIC);
		topic.append("/");
		topic.append(subtopic);

		return topic.toString();
	}
	
	private String getConnectionUrl(String host, int port) {
		// FIXME: Handle SSL connection
		StringBuilder builder = new StringBuilder("tcp://");

		// Craft the URL
		builder.append(host);
		if (port > 0) {
			builder.append(':');
			builder.append(port);
		}

		return builder.toString();
	}
}
