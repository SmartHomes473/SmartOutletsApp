package com.smrthaus.smartoutlets.bluetooth;

import java.util.ArrayList;
import java.util.UUID;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.smrthaus.smartoutlets.Outlet;
import com.smrthaus.smartoutlets.Outlet.State;

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
		Outlet outlet = mBluetoothTask.getUpdatedOutlet();

		// Does nothing if the outlet no longer exists
		if (outlet == null) {
			return;
		}
		
		// TODO: actually send the update command
		int status = BT_STATE_UPDATED_OUTLET;
		
		mBluetoothTask.handleState(status);
	}
}
