package com.smrthaus.smartoutlets.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import android.bluetooth.BluetoothSocket;

import com.smrthaus.smartoutlets.Outlet;
import com.smrthaus.smartoutlets.api.Packet.PacketException;
import com.smrthaus.smartoutlets.api.PacketMan;

public class UpateOutletRunnable implements Runnable
{
	// Sets a log tag for this class
	@SuppressWarnings("unused")
	private static final String	LOG_TAG						= "BTUdateOutletRunnable";

	// Constants for indicating the state of the connection
	static final int			BT_STATE_UNSUPPORTED		= -1;
	static final int			BT_STATE_FAILED				= -2;
	static final int			BT_STATE_STARTED			= 0;
	static final int			BT_STATE_CONNECTED			= 1;
	static final int			BT_STATE_DISCONNECTED		= 2;
	static final int			BT_STATE_UPDATED_LISTVIEW	= 3;
	static final int			BT_STATE_UPDATED_OUTLET		= 4;
	static final int			BT_STATE_UPDATED_FAILED		= 5;


	private static final UUID	SPP_UUID					= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String	ADDRESS						= "E0:06:E6:BE:AB:9B";

	// Defines a field that contains the calling object of type BluetoothTask
	final BluetoothTask			mBluetoothTask;

	/**
	 * This constructor creates and instance of the BTConnectRunnable and stores
	 * and instance of the BluetoothTask that instantiated it.
	 * 
	 * @param bluetoothTask
	 *            The BluetoothTask
	 */
	UpateOutletRunnable ( BluetoothTask bluetoothTask )
	{
		mBluetoothTask = bluetoothTask;
	}

	@Override
	public void run ( )
	{
		/*
		 * Stores the current Thread in the BluetoothTask instance so that the
		 * instance can interrupt the Thread.
		 */
		mBluetoothTask.setConnectThread(Thread.currentThread());

		// Moves the current Thread into the background
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		
		// Gets the input byte buffer from the BluetoothTask instance.
		byte[] byteBuffer = mBluetoothTask.getByteBuffer();
		
		Outlet outlet = mBluetoothTask.getUpdatedOutlet();

		// Does nothing if the outlet no longer exists
		if (outlet == null) {
			return;
		}
		
		Lock lock = mBluetoothTask.getLock();
		lock.lock();
		
		try {
			BluetoothSocket socket = mBluetoothTask.getSocket();
			
			while (socket == null) {
				mBluetoothTask.getCondition().await();
				socket = mBluetoothTask.getSocket();
			}
			
			OutputStream output = socket.getOutputStream();
			output.write(PacketMan.packageOutletUpdate(outlet));
			output.flush();
			
			InputStream input = socket.getInputStream();
			int readLen = input.read(byteBuffer);
			if (readLen < 0) {
				throw new IOException();
			}
			byte[] packet = new byte[readLen];
			System.arraycopy(byteBuffer, 0, packet, 0, readLen);
			
			if (!PacketMan.parseAck(packet)) {
				// handle no ack error
			}
		}
		catch (InterruptedException e) {
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PacketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lock.unlock();
		// TODO: actually send the update command
		int status = BT_STATE_UPDATED_OUTLET;
		
		mBluetoothTask.handleState(status);
	}
}
