package com.smrthaus.smartoutlets;

public class BTEnableRunnable implements Runnable
{
	// Sets a log tag for this class
	@SuppressWarnings("unused")
	private static final String	LOG_TAG					= "BTEnableRunnable";

	// Constants for indicating the state of the connection
	static final int			BT_STATE_UNSUPPORTED	= -1;
	static final int			BT_STATE_FAILED			= -2;
	static final int			BT_STATE_STARTED		= 0;
	static final int			BT_STATE_CONNECTED		= 1;
	static final int			BT_STATE_DISCONNECTED	= 2;

	// Defines a field that contains the calling object of type BluetoothTask
	final BluetoothTask			mBluetoothTask;

	/**
	 * This constructor creates and instance of the BTConnectRunnable and stores
	 * and instance of the BluetoothTask that instantiated it.
	 * 
	 * @param bluetoothTask
	 *            The BluetoothTask
	 */
	BTEnableRunnable(BluetoothTask bluetoothTask)
	{
		mBluetoothTask = bluetoothTask;
	}

	/**
	 * Defines the object's task, which is a set of instruction designed to be
	 * run on a thread.
	 */
	@Override
	public void run()
	{

	}
}