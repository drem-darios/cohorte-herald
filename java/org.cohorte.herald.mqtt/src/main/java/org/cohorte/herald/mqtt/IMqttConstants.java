package org.cohorte.herald.mqtt;

/**
 * Constants used by the MQTT transport implementation
 * 
 * @author Drem Darios
 */
public interface IMqttConstants {

	/** MQTT access ID **/
	String ACCESS_ID = "mqtt";
	
	/** MQTT topic prefix **/
	String TOPIC_PREFIX = "cohorte/herald";
	
	/** MQTT topic for UID **/
	String UID_TOPIC = "uid";
	
	/** MQTT topic for Group **/
	String GROUP_TOPIC = "group";
	
	/** MQTT topic for RIP **/
	String RIP_TOPIC = "rip";
	
}
