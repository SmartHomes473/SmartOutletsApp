package com.smrthaus.smartoutlets.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class DisconnectRunnable implements Runnable
{
	// Sets a log tag for this class
	@SuppressWarnings("unused")
	private static final String	LOG_TAG					= "BTDisconnectRunnable";

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
	DisconnectRunnable ( BluetoothTask bluetoothTask )
	{
		mBluetoothTask = bluetoothTask;
	}

	/**
	 * Defines the object's task, which is a set of instruction designed to be
	 * run on a thread.
	 */
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

		// Acquires a lock on the Bluetooth resources
		Lock lock = mBluetoothTask.getLock();
		lock.lock();

		/*
		 * A try block that disconnects from Bluetooth device.
		 */
		try {
			// Before continuing, make sure the thread hasn't been interrupted.
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			// Gets the currently open socket
			BluetoothSocket socket = mBluetoothTask.getSocket();

			if (socket != null) {
				// Remove reference to old socket
				mBluetoothTask.setSocket(null);

				// Flush the output stream
				OutputStream output = socket.getOutputStream();
				if (output != null) {
					output.flush();
				}

				// Close the socket
				socket.close();
			}

			mBluetoothTask.handleState(BT_STATE_DISCONNECTED);
		}
		// Catches exceptions thrown in response to the thread being interrupted
		catch (InterruptedException e) {

			// Does nothing

		}
		// Catches exceptions thrown in response to IO errors
		catch (IOException e) {
			e.printStackTrace();
			mBluetoothTask.handleState(BT_STATE_FAILED);
		}
		finally {
			// Release our lock on the Bluetooth resources
			lock.unlock();
		}
	}
}
