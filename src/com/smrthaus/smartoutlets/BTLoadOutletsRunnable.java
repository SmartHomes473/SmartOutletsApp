package com.smrthaus.smartoutlets;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.smrthaus.smartoutlets.Outlet.State;

public class BTLoadOutletsRunnable implements Runnable
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
	BTLoadOutletsRunnable ( BluetoothTask bluetoothTask )
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

		// Acquires a lock on the Bluetooth resources
		Lock lock = mBluetoothTask.getLock();
		lock.lock();

		ListView listView = mBluetoothTask.getOutletListView();

		// Does nothing if the view no longer exists
		if (listView == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayAdapter<Outlet> adapter = (ArrayAdapter<Outlet>) listView
				.getAdapter();

		/*
		 * TODO: actually read the list of outlets from the server
		 * 
		 * Right now these outlets are hard coded for development purposes. As
		 * soon as we've implemented the communication protocol and parser we
		 * can read actual data from the remote device.
		 */
		ArrayList<Outlet> outletList = new ArrayList<Outlet>();
		outletList.add(new Outlet("1", "Outlet 1", 1200, State.ON));
		outletList.add(new Outlet("2", "Outlet 2", 300, State.ON));
		outletList.add(new Outlet("3", "Outlet 3", 6000, State.ON));

		for (int oid = 4; oid < 12; ++oid) {
			outletList.add(new Outlet(Integer.toString(oid), "Outlet "
					+ Integer.toString(oid), 0, State.OFF));
		}

		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Release the lock on Bluetooth resources
		lock.unlock();

		// Save the outlet list
		mBluetoothTask.setOutletList(outletList);

		mBluetoothTask.handleState(BT_STATE_UPDATED_LISTVIEW);
	}

}
