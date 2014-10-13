package com.smrthaus.smartoutlets;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

public class BluetoothTask
{
	// Runnables for Bluetooth tasks
	private Runnable				mConnectRunnable;
	private Runnable				mDisconnectRunnable;

	// Input buffer for reading Bluetooth responses
	byte[]							mInputBuffer;

	// The Thread on which the task is currently running
	private Thread					mCurrentThread;

	// An object that contains the ThreadPool singleton
	private static BluetoothManager	sBluetoothManager;

	// Create a class containing a connect object
	BluetoothTask()
	{
		mConnectRunnable = new BTConnectRunnable(this);
		mDisconnectRunnable = new BTDisconnectRunnable(this);
		sBluetoothManager = BluetoothManager.getInstance();
	}

	void initializeConnectTask(BluetoothManager bluetoothManager)
	{
		sBluetoothManager = bluetoothManager;
	}
	
	void initializeDisconnectTask(BluetoothManager bluetoothManager)
	{
		sBluetoothManager = bluetoothManager;
	}

	Runnable getConnectRunnable()
	{
		return mConnectRunnable;
	}
	
	Runnable getDisconnectRunnable()
	{
		return mDisconnectRunnable;
	}

	public byte[] getByteBuffer()
	{
		return mInputBuffer;
	}

	public void setByteBuffer(byte[] inputBuffer)
	{
		mInputBuffer = inputBuffer;
	}

	void handleState(int state)
	{
		sBluetoothManager.handleState(this, state);
	}

	void recycle()
	{
		mInputBuffer = null;
	}

	public void setConnectThread(Thread currentThread)
	{
		setCurrentThread(currentThread);
	}

	public Lock getLock()
	{
		return sBluetoothManager.getLock();
	}

	public Condition getCondition()
	{
		return sBluetoothManager.getCondition();
	}

	public BluetoothAdapter getAdapter()
	{
		return sBluetoothManager.getAdapter();
	}

	public void setSocket(BluetoothSocket socket)
	{
		sBluetoothManager.setSocket(socket);
	}

	public BluetoothSocket getSocket()
	{
		return sBluetoothManager.getSocket();
	}

	/*
	 * Returns the Thread that this Task is running on. The method must first
	 * get a lock on a static field, in this case the ThreadPool singleton. The
	 * lock is needed because the Thread object reference is stored in the
	 * Thread object itself, and that object can be changed by processes outside
	 * of this app.
	 */
	public Thread getCurrentThread()
	{
		synchronized (sBluetoothManager) {
			return mCurrentThread;
		}
	}

	/*
	 * Sets the identifier for the current Thread. This must be a synchronized
	 * operation; see the notes for getCurrentThread()
	 */
	public void setCurrentThread(Thread thread)
	{
		synchronized (sBluetoothManager) {
			mCurrentThread = thread;
		}
	}
}
