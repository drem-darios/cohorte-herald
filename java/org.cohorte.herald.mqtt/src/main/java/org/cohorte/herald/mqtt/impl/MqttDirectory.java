package org.cohorte.herald.mqtt.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.herald.Access;
import org.cohorte.herald.IConstants;
import org.cohorte.herald.IDirectory;
import org.cohorte.herald.ITransportDirectory;
import org.cohorte.herald.Peer;
import org.cohorte.herald.mqtt.IMqttConstants;
import org.cohorte.herald.mqtt.IMqttDirectory;
import org.cohorte.herald.mqtt.MqttAccess;

/**
 * Herald MQTT transport directory
 *
 * @author Drem Darios
 */
@Component
@Provides(specifications = { ITransportDirectory.class, IMqttDirectory.class })
@Instantiate(name = "herald-mqtt-directory")
public class MqttDirectory implements ITransportDirectory, IMqttDirectory {

	/** Access ID property */
	@ServiceProperty(name = IConstants.PROP_ACCESS_ID, value = IMqttConstants.ACCESS_ID)
	private String pAccessId;

	/** Herald core directory */
	@Requires
	private IDirectory pDirectory;

	/** Peer UID -&gt; Peer access */
	private final Map<String, MqttAccess> pUidAddress = new LinkedHashMap<>();

	@Override
	public Access loadAccess(Object aData) {
		return MqttAccess.load(aData);
	}

	@Override
	public void peerAccessSet(Peer aPeer, Access aData) {
		if (!aPeer.equals(pDirectory.getLocalPeer())
				&& aData instanceof MqttAccess) {
			pUidAddress.put(aPeer.getUid(), (MqttAccess) aData);
		}
	}

	@Override
	public void peerAccessUnset(Peer aPeer, Access aData) {
		pUidAddress.remove(aPeer.getUid());
	}

	/**
	 * Component invalidated
	 */
	@Invalidate
	public void invalidate() {
		// Clean up
		pUidAddress.clear();
	}

	/**
	 * Component validated
	 */
	@Validate
	public void validate() {
		pUidAddress.clear();
	}
}
