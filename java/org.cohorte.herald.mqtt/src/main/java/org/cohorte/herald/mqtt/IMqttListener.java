package org.cohorte.herald.mqtt;

import org.cohorte.herald.Message;

/**
 * @author Drem Darios
 */
public interface IMqttListener {

	/**
     * An MQTT message has been received
     *
     * @param aMessage
     *            The received message
     */
    void onMessage(Message aMessage);
}
