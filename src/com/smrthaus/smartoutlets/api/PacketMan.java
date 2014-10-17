package com.smrthaus.smartoutlets.api;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.smrthaus.smartoutlets.Outlet;
import com.smrthaus.smartoutlets.Outlet.State;
import com.smrthaus.smartoutlets.api.Packet.PacketException;
import com.smrthaus.smartoutlets.api.Packet.PacketType;

public class PacketMan
{
	// Singleton instance
	private static PacketMan	sInstance;
	static {
		sInstance = new PacketMan();
	}

	private PacketMan ( )
	{

	}

	/**
	 * Parse an a RESP_POWER_STATS response.
	 * 
	 * @param raw
	 *            Raw response byte stream
	 * @return An ArrayList of outlets
	 * @throws PacketException
	 */
	static public ArrayList<Outlet> parseOutletUpdate ( byte[] raw )
			throws PacketException
	{
		// Parse and validate the packet
		Packet packet = new Packet(raw);

		byte[] data = packet.getData();

		// The data segment must contain at least one byte (the size of the
		// list)
		if (data.length == 0) {
			throw new PacketException();
		}

		// The remaining data segment must have a length that is a multiple of
		// three
		if (((data.length - 1) % 3) != 0) {
			throw new PacketException();
		}

		// XXX: consider using a Map or a Set instead of an ArrayList

		// Construct the array of outlets
		ArrayList<Outlet> outlets = new ArrayList<Outlet>();
		int listLength = Byte.valueOf(data[0]).intValue();
		for (int index = 0; index < listLength; ++index) {
			byte[] element = new byte[3];

			// FIXME: There are a lot of magic numbers here

			System.arraycopy(data, 1 + 3 * index, element, 0, 3);

			// Get the outlet ID
			Byte oid = Byte.valueOf((byte) (0x7F & element[0]));

			// Read the last two bytes of each three byte chunk (skipping the
			// list length)
			ByteBuffer buffer = ByteBuffer.wrap(element, 1, 2);

			int power = buffer.getShort();
			Outlet.State state = (element[0] & 0x7F) == element[0] ? State.OFF
					: State.ON;

			outlets.add(new Outlet(oid, "Outlet " + oid.toString(), power,
					state));
		}

		return outlets;
	}

	static public boolean parseAck ( byte[] raw ) throws PacketException
	{
		// Parse and validate the packet
		Packet packet = new Packet(raw);

		return packet.getPacketType() == PacketType.ACK;
	}

	static public byte[] packageOutletRequest ( )
	{
		byte[] header = Packet.getHeader(PacketType.REQ_POWER_STATS);
		byte[] data = { 0x0 };

		byte[] request = new byte[header.length + data.length];
		System.arraycopy(header, 0, request, 0, header.length);
		System.arraycopy(data, 0, request, header.length, data.length);

		return request;
	}

	static public byte[] packageOutletUpdate ( Outlet outlet )
	{
		byte[] header = Packet.getHeader(PacketType.SET_POWER_STATE);
		byte[] data = new byte[1];

		data[0] = (byte) ((outlet.getId() & 0x7F) | (outlet.isPowered() ? 0x80
				: 0x00));

		byte[] request = new byte[header.length + data.length];
		System.arraycopy(header, 0, request, 0, header.length);
		System.arraycopy(data, 0, request, header.length, data.length);

		return request;
	}

	public static PacketMan getInstance ( )
	{
		return sInstance;
	}
}
