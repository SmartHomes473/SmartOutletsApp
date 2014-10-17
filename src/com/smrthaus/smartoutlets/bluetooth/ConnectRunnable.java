package com.smrthaus.smartoutlets.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectRunnable implements Runnable
{
	// Sets a log tag for this class
	@SuppressWarnings("unused")
	private static final String	LOG_TAG					= "BTConnectRunnable";

	// Constants for indicating the state of the connection
	static final int			BT_STATE_UNSUPPORTED	= -1;
	static final int			BT_STATE_FAILED			= -2;
	static final int			BT_STATE_STARTED		= 0;
	static final int			BT_STATE_CONNECTED		= 1;
	static final int			BT_STATE_DISCONNECTED	= 2;

	private static final UUID	SPP_UUID				= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String	ADDRESS					= "E0:06:E6:BE:AB:9B";

	// Defines a field that contains the calling object of type BluetoothTask
	final BluetoothTask			mBluetoothTask;

	/**
	 * This constructor creates and instance of the BTConnectRunnable and stores
	 * and instance of the BluetoothTask that instantiated it.
	 * 
	 * @param bluetoothTask
	 *            The BluetoothTask
	 */
	ConnectRunnable(BluetoothTask bluetoothTask)
	{
		mBluetoothTask = bluetoothTask;
	}

	/**
	 * Defines the object's task, which is a set of instruction designed to be
	 * run on a thread.
	 */
	@SuppressWarnings("resource")
	@Override
	public void run()
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

		/*
		 * A try block that connects to a Bluetooth device.
		 */
		try {
			// Before continuing, make sure the thread hasn't been interrupted.
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			// Gets Bluetooth adapter
			BluetoothAdapter adapter = mBluetoothTask.getAdapter();

			// Gets the remote Bluetooth device
			BluetoothDevice device = adapter.getRemoteDevice(ADDRESS);

			// Open an RFCOMM socket to remote device
			BluetoothSocket socket = device
					.createInsecureRfcommSocketToServiceRecord(SPP_UUID);

			// Discovery is resource intensive. Disable it.
			adapter.cancelDiscovery();

			// Establish connection.
			socket.connect();

			// Get IO streams
			OutputStream output = socket.getOutputStream();
			InputStream input = socket.getInputStream();

			// Update the Bluetooth manager with new socket
			mBluetoothTask.setSocket(socket);

			// Signal other waiting threads that the socket has been created
			mBluetoothTask.getCondition().signalAll();
			
			mBluetoothTask.handleState(BT_STATE_CONNECTED);
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
