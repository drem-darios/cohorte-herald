package org.cohorte.herald.mqtt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
import org.cohorte.herald.ITransport;
import org.cohorte.herald.InvalidPeerAccess;
import org.cohorte.herald.Message;
import org.cohorte.herald.MessageReceived;
import org.cohorte.herald.Peer;
import org.cohorte.herald.Target;
import org.cohorte.herald.mqtt.IMqttConstants;
import org.cohorte.herald.mqtt.MqttAccess;
import org.cohorte.herald.mqtt.MqttExtra;
import org.cohorte.herald.transport.IContactHook;
import org.cohorte.herald.transport.PeerContact;
import org.eclipse.paho.client.mqttv3.MqttException;
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
public class MqttTransport implements ITransport, IContactHook {

	@ServiceProperty(name = IConstants.PROP_ACCESS_ID, value = IMqttConstants.ACCESS_ID)
	private String pAccessId;

	/** The transport service controller */
	@ServiceController
	private boolean pController;

    /** The peer contact handler */
    private PeerContact pContact;
    
	/** MQTT server host */
	@Property(name = "mqtt.server", value = "localhost")
	private String pHost;

	/** MQTT server port */
	@Property(name = "mqtt.port", value = "1883")
	private int pPort;

	/** MQTT username */
	@Property(name = "mqtt.username")
	private String pUsername;
	
	/** MQTT password */
	@Property(name = "mqtt.password")
	private String pPassword;
	
	/** The MQTT messanger client */
	private MqttMessenger pMessenger;
	
	private Peer pPeer;
	
	/** The log service */
	@Requires
	private LogService pLogger;

	/** The Jabsorb serializer */
	private JSONSerializer pSerializer;
	
	/** Herald core directory */
	@Requires
	private IDirectory pDirectory;

	@Override
	public void fire(Peer aPeer, Message aMessage) throws HeraldException {
		fire(aPeer, aMessage, null);
	}

	@Override
	public void fire(Peer aPeer, Message aMessage, Object aExtra)
			throws HeraldException {
		fire(aPeer, aMessage, aExtra, false);
	}

	private void fire(Peer aPeer, Message aMessage, Object aExtra,
			Boolean isGroup) throws HeraldException, InvalidPeerAccess {

		// Get message extra information, if any
		final MqttExtra extra;
		if (aExtra instanceof MqttExtra) {
			extra = (MqttExtra) aExtra;
		} else {
			extra = null;
		}
		
		// TODO: Add logging before sending message
		try {
			String topic = getTopic(aPeer, extra);
			pMessenger.fire(topic, aMessage);

		} catch (MqttException | MarshallException ex) {
			throw new HeraldException(new Target(aPeer),
					"Error converting the content of the message to JSON: "
							+ ex, ex);
		}
	}

	@Override
	public Collection<Peer> fireGroup(String aGroup, Collection<Peer> aPeers,
			Message aMessage) throws HeraldException {
		Collection<Peer> reachedPeers = new ArrayList<Peer>();
		for (Peer aPeer : aPeers) {
			if (aPeer.getGroups().contains(aGroup)) {
				reachedPeers.add(aPeer);
				fire(aPeer, aMessage, null, true);
			}
		}

		return reachedPeers;
	}

	/**
	 * Component invalidated
	 * @throws MqttException 
	 */
	@Invalidate
	public void invalidate() {
		try {
			pMessenger.disconnect();
		} catch (final Exception ex) {
			pLogger.log(LogService.LOG_ERROR,
					"Error disconnecting from MQTT server: " + ex, ex);
		}
		pPeer.unsetAccess(pAccessId);
		pPeer = null;
		// Clean up members
		pSerializer = null;
		pController = false;
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
		
		this.pPeer = this.pDirectory.getLocalPeer();
		this.pMessenger = new MqttMessenger(pPeer);
//		this.pMessenger.setCallbackHandler(this);
		if (this.pUsername != null && !this.pUsername.isEmpty()) {
			this.pMessenger.login(pUsername, pPassword);
		}
		
		try {
			this.pMessenger.connect(pHost, pPort);
		} catch (MqttException ex) {
			pLogger.log(LogService.LOG_ERROR,
					"Error setting MQTT messanger: " + ex, ex);
			pController = false;
			return;
		}
		
		
		this.pContact = new PeerContact(pDirectory, this, pLogger);
		
		// Everything is OK
		pController = true;
	}

	@Override
	public Map<String, Object> updateDescription(MessageReceived aMessage,
			Map<String, Object> aDescription) {
		if (aMessage.getAccess().equals(pAccessId)) {
            // Forge the access to the HTTP server using extra information
            final MqttExtra extra = (MqttExtra) aMessage.getExtra();
            final MqttAccess updatedAccess = new MqttAccess(extra.getHost(),
                    extra.getPort(), this.pPeer.getUid(), null);

            // Update the remote peer description with its MQTT access
            @SuppressWarnings("unchecked")
            final Map<String, Object> accessDump = (Map<String, Object>) aDescription
                    .get("accesses");
            accessDump.put(pAccessId, updatedAccess.dump());
        }

        return aDescription;
	}

	private String getTopic(Peer aPeer, MqttExtra aExtra)
			throws InvalidPeerAccess {
		String topic = null;
		if (aExtra != null) {
			// Try to use extra information
			topic = aExtra.getTopic();
		}

		if (aPeer != null && (topic == null || topic.isEmpty())) {
			// Use the peer description, if any
			final MqttAccess peerAccess = (MqttAccess) aPeer
					.getAccess(pAccessId);
			topic = peerAccess.getTopic();
		}

		// If we have nothing at this point, we can't compute an access
		if (topic == null || topic.isEmpty()) {
			throw new InvalidPeerAccess(new Target(aPeer), "No " + pAccessId
					+ " access found");
		}
		
		return topic;

	}
}
