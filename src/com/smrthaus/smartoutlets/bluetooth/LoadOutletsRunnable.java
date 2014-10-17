package com.smrthaus.smartoutlets.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import android.bluetooth.BluetoothSocket;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.smrthaus.smartoutlets.Outlet;
import com.smrthaus.smartoutlets.Outlet.State;
import com.smrthaus.smartoutlets.api.Packet.PacketException;
import com.smrthaus.smartoutlets.api.PacketMan;

public class LoadOutletsRunnable implements Runnable
{
	// Sets a log tag for this class
	@SuppressWarnings("unused")
	private static final String	LOG_TAG						= "BTConnectRunnable";

	// Constants for indicating the state of the connection
	static final int			BT_STATE_UNSUPPORTED		= -1;
	static final int			BT_STATE_FAILED				= -2;
	static final int			BT_STATE_STARTED			= 0;
	static final int			BT_STATE_CONNECTED			= 1;
	static final int			BT_STATE_DISCONNECTED		= 2;
	static final int			BT_STATE_UPDATED_LISTVIEW	= 3;

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
	LoadOutletsRunnable ( BluetoothTask bluetoothTask )
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

		ListView listView = mBluetoothTask.getOutletListView();

		// Does nothing if the view no longer exists
		if (listView == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayAdapter<Outlet> adapter = (ArrayAdapter<Outlet>) listView
				.getAdapter();
		ArrayList<Outlet> outletList = new ArrayList<Outlet>();

		// Acquires a lock on the Bluetooth resources
		Lock lock = mBluetoothTask.getLock();
		lock.lock();

		try {
			// Get the Bluetooth socket
			BluetoothSocket socket = mBluetoothTask.getSocket();
			
			// Verify a socket has been established
			while (socket == null) {
				// Sleep until we are signaled a socket is created
				mBluetoothTask.getCondition().await();

				// Re-test the socket
				socket = mBluetoothTask.getSocket();
			}
			
			// Requests list of outlets and their power consumption from the server
			OutputStream output = socket.getOutputStream();
			output.write(PacketMan.packageOutletRequest());
			output.flush();
			
			// Get the response
			InputStream input = socket.getInputStream();
			int readLen = input.read(byteBuffer);
			if (readLen < 0) {
				throw new IOException();
			}
			byte[] packet = new byte[readLen];
			System.arraycopy(byteBuffer, 0, packet, 0, readLen);
			
			outletList = PacketMan.parseOutletUpdate(packet);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PacketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			// XXX: not sure if we should release the mutex here
		}

		// Release the lock on Bluetooth resources
		lock.unlock();

		// Save the outlet list
		mBluetoothTask.setOutletList(outletList);

		mBluetoothTask.handleState(BT_STATE_UPDATED_LISTVIEW);
	}

}
