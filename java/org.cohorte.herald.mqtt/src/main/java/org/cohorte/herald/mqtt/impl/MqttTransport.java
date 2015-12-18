package org.cohorte.herald.mqtt.impl;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IConstants;
import org.cohorte.herald.IDirectory;
import org.cohorte.herald.IHeraldInternal;
import org.cohorte.herald.ITransport;
import org.cohorte.herald.InvalidPeerAccess;
import org.cohorte.herald.Message;
import org.cohorte.herald.Peer;
import org.cohorte.herald.Target;
import org.cohorte.herald.core.utils.MessageUtils;
import org.cohorte.herald.mqtt.IMqttConstants;
import org.cohorte.herald.mqtt.IMqttDirectory;
import org.cohorte.herald.mqtt.MqttAccess;
import org.cohorte.herald.mqtt.MqttExtra;
import org.cohorte.herald.transport.PeerContact;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.osgi.service.log.LogService;

/**
 * Implementation of the Herald MQTT transport
 * 
 * @author Drem Darios
 */
@Component
@Provides(specifications = ITransport.class)
@Instantiate(name = "herald-mqtt-transport")
public class MqttTransport implements ITransport {

	@ServiceProperty(name = IConstants.PROP_ACCESS_ID, value = IMqttConstants.ACCESS_ID)
	private String pAccessId;

    /** The peer contact handler */
    private PeerContact pContact;

    /** The transport service controller */
    @ServiceController
    private boolean pController;

	/** The Herald core directory */
	@Requires
	private IDirectory pDirectory;

	/** The Herald core service */
	@Requires
	private IHeraldInternal pHerald;

	/** MQTT server host */
	@Property(name = "mqtt.server", value = "localhost")
	private String pHost = "localhost";

	/** MQTT server port */
	@Property(name = "mqtt.port", value = "1883")
	private int pPort = 1883;
	
	/** MQTT clientId */
	@Property(name = "mqtt.clientId", mandatory = true)
	private String pClientId = "aClientId";
	
	/** The MQTT client */
	private MqttClient pClient = null;
	
	/** The log service */
	@Requires(optional = true)
	private LogService pLogger;

	/** The Jabsorb serializer */
	private JSONSerializer pSerializer;

	/** The MQTT directory */
	@Requires
	private IMqttDirectory pMqttDirectory;

	@Override
	public void fire(Peer aPeer, Message aMessage) throws HeraldException {
		fire(aPeer, aMessage, null);
	}

	@Override
	public void fire(Peer aPeer, Message aMessage, Object aExtra)
			throws HeraldException {

		// Get message extra information, if any
		final MqttExtra extra;
		if (aExtra instanceof MqttExtra) {
			extra = (MqttExtra) aExtra;
		} else {
			extra = null;
		}
		
		String url = getBrokerUrl(aPeer, extra); 
		String clientId = extra.getClientId();
		String topic = extra.getTopic();

		try {
			sendMessage(aPeer, url, clientId, topic, aMessage);	
		} catch (MqttException | MarshallException ex) {
			throw new HeraldException(new Target(aPeer),
					"Error converting the content of the message to JSON: "
							+ ex, ex);
		}
		
	}

	private String getBrokerUrl(Peer aPeer, MqttExtra aExtra) throws InvalidPeerAccess {
		String host = null;
		int port = 0;

		if (aExtra != null) {
			// Try to use extra information
			host = aExtra.getHost();
			port = aExtra.getPort();
		}

		if (aPeer != null && (host == null || host.isEmpty())) {
			// Use the peer description, if any
			final MqttAccess peerAccess = (MqttAccess) aPeer.getAccess(pAccessId);
			host = peerAccess.getHost();
			port = peerAccess.getPort();
		}

		// If we have nothing at this point, we can't compute an access
		if (host == null || host.isEmpty()) {
			throw new InvalidPeerAccess(new Target(aPeer), "No " + pAccessId + " access found");
		}

		// No port given, remove it from the URL
		if (port == 0) {
			port = -1;
		}
		
		return toUrl(host, port);
	}

	private void sendMessage(Peer aPeer, String serverUrl, String clientId,
			String topic, Message aMessage) throws MqttException, MarshallException {
		try {
			MqttMessage message = new MqttMessage();
			String content = MessageUtils.toJSON(aMessage);
			
			message.setPayload(content.getBytes());
			pClient.publish(topic, message);
		} catch (MarshallException ex) {
			throw ex;
		} finally {
			// Fine, I'll be nice too :-)
			if (pClient != null) {
//				client.disconnect();
			}
		}
	}

	@Override
	public Collection<Peer> fireGroup(String aGroup, Collection<Peer> aPeers,
			Message aMessage) throws HeraldException {
		return null;
	}
	
	/**
	 * Component invalidated
	 */
	@Invalidate
	public void invalidate() {
		try {
			pClient.disconnect();
		} catch (MqttException ex) {
			pLogger.log(LogService.LOG_ERROR,
                    "Error disconnecting from the MQTT server: " + ex, ex);
		}
		pClient = null;
		// Clean up members
        pContact.clear();
        pContact = null;
		pSerializer = null;
	}
	
	/**
	 * Component validated
	 */
	@Validate
	public void validate() {

		// Prepare the JSON serializer
		pSerializer = new JSONSerializer();
		try {
			pSerializer.registerDefaultSerializers();
		} catch (final Exception ex) {
			// Error setting up the serializer: abandon
			pLogger.log(LogService.LOG_ERROR,
					"Error setting up the JSON serializer: " + ex, ex);
			pController = false;
			return;
		}
		
		try {
			pClient = new MqttClient(toUrl(pHost, pPort), pClientId);
			pClient.connect();
        } catch (final MqttException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error connecting to the MQTT server: " + ex, ex);
        }
		
		// Prepare the peer contact handler
        pContact = new PeerContact(pDirectory, null, pLogger);

		// Everything is OK
		pController = true;
	}

	private String toUrl(String host, int port) {
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
