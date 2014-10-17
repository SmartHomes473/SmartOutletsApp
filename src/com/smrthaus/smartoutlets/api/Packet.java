package com.smrthaus.smartoutlets.api;

import java.util.Random;


public class Packet
{
	/*
	 * Packet structure:
	 * 
	 *    +---------+--------------+
	 *    | bits    | field        |
	 *    +---------+--------------+
	 *    | 0 - 7   | device type  |
	 *    +---------+--------------+
	 *    | 8 - 15  | packet type  |
	 *    +---------+--------------+
	 *    | 16 - 23 | sequence id  |
	 *    +---------+--------------+
	 *    | 24 ...  | data segment |
	 *    +---------+--------------+
	 */

	// Packet parameters
	public final static int		MIN_PACKET_SIZE			= 3;
	public final static byte	SMART_OUTLETS_DEVICE_ID	= 0x03;
	public final static int		HEADER_SIZE				= 3;

	public enum PacketType {
		ACK, RESP_OUTLETS, RESP_POWER_STATS, RESP_SCHEDULE, REQ_POWER_STATS
	}

	// Packet fields
	private PacketType	pType;
	private byte pMessageId;
	private byte[] pData;

	public Packet ( byte[] packet ) throws PacketException
	{
		// Packets have a minimum size
		if (packet.length < MIN_PACKET_SIZE) {
			throw new PacketException();
		}

		// Only SmartOutlet packets are valid
		if (packet[0] != SMART_OUTLETS_DEVICE_ID) {
			throw new PacketException();
		}

		// Determine the packet type
		switch (packet[1]) {
		case (byte) 0x80:
			pType = PacketType.ACK;
			break;

		case (byte) 0x81:
			pType = PacketType.RESP_OUTLETS;
			break;

		case (byte) 0x82:
			pType = PacketType.RESP_POWER_STATS;
			break;

		case (byte) 0x83:
			pType = PacketType.RESP_SCHEDULE;
			break;

		default:
			throw new PacketException();
		}
		
		// Gets the message ID
		pMessageId = packet[2];
		
		// Copy the data segment, if it exists
		pData = new byte[packet.length - MIN_PACKET_SIZE];
		System.arraycopy(packet, MIN_PACKET_SIZE, pData, 0, packet.length - MIN_PACKET_SIZE);
	}

	/**
	 * Get the packet type.
	 * 
	 * @return Packet type
	 */
	public PacketType getPacketType ()
	{
		return pType;
	}
	
	/**
	 * Get the packet's message ID.
	 * 
	 * @return Message ID
	 */
	public byte getMessageId ()
	{
		return pMessageId;
	}
	
	/**
	 * Get the packet's data segment.
	 * 
	 * @return Data byte array
	 */
	public byte[] getData ()
	{
		return pData;
	}
	
	public static class PacketException extends Exception
	{

	}

	public static byte[] getHeader ( PacketType type )
	{
		byte[] header = new byte[HEADER_SIZE];

		// Sets the device ID to smart outlets
		header[0] = 0x03;

		// Gets the packet request type
		// FIXME: convert this into a dictionary-style lookup
		switch (type) {
		case ACK:
			header[1] = (byte) 0x80;
			break;

		case RESP_OUTLETS:
			header[1] = (byte) 0x81;
			break;

		case RESP_POWER_STATS:
			header[1] = (byte) 0x82;
			break;

		case RESP_SCHEDULE:
			header[1] = (byte) 0x83;
			break;

		case REQ_POWER_STATS:
			header[1] = (byte) 0x01;
			break;

		default:
			header[1] = (byte) 0xFF;
			break;
		}
		
		// Generates a random message ID
		byte[] mid = new byte[1];
		(new Random()).nextBytes(mid);
		header[2] = mid[0];
		
		return header;
	}
}
