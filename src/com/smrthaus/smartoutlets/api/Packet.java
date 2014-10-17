package com.smrthaus.smartoutlets.api;

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

	private enum PacketType {
		ACK, RESP_OUTLETS, RESP_POWER_STATS, RESP_SCHEDULE
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
}
