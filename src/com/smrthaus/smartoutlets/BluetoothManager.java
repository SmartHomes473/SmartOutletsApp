package com.smrthaus.smartoutlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class BluetoothManager
{

	// Singleton instance
	private static BluetoothManager			sInstance;

	private Handler							mHandler;

	/*
	 * Gets the number of available cores (not always the same as the maximum
	 * number of cores)
	 */
	private static int						NUMBER_OF_CORES			= Runtime
																			.getRuntime()
																			.availableProcessors();
	private static final int				REQUEST_ENABLE_BT		= 1;
	
	// A queue of Runnables
	private final BlockingQueue<Runnable>	mBluetoothWorkQueue;
	// Sets the amount of time an idle thread waits before terminating
	private static final int				KEEP_ALIVE_TIME			= 1;
	// Sets the Time Unit to seconds
	private static final TimeUnit			KEEP_ALIVE_TIME_UNIT	= TimeUnit.SECONDS;

	private final ThreadPoolExecutor		mBluetoothThreadPool;

	// Only one thread should be operating on the Bluetooth at a time
	private static final Lock				mBTLock					= new ReentrantLock();
	private static final Condition			mBTWait					= mBTLock
																			.newCondition();

	// Bluetooth stuffs
	private static BluetoothAdapter			mBTAdapter				= BluetoothAdapter
																			.getDefaultAdapter();
	private static BluetoothSocket			mBTSocket				= null;
	private static OutputStream				mBTOutput				= null;
	private static InputStream				mBTInput				= null;

	// Constants for indicating the state of the connection
	static final int			BT_STATE_UNSUPPORTED	= -1;
	static final int			BT_STATE_FAILED			= -2;
	static final int			BT_STATE_STARTED		= 0;
	static final int			BT_STATE_CONNECTED		= 1;
	static final int			BT_STATE_DISCONNECTED	= 2;
	
	static {
		sInstance = new BluetoothManager();
	}

	private BluetoothManager()
	{
		mHandler = new Handler(Looper.getMainLooper()) {
			/**
			 * Defines the operations to perform when the Handler receives a new
			 * Message to process.
			 */
			@Override
			public void handleMessage(Message inputMessage)
			{

			}
		};

		// Instantiates the queue of Runnables as a LinkedBlockingQueue
		mBluetoothWorkQueue = new LinkedBlockingQueue<Runnable>();

		// Creates a thread pool manager
		mBluetoothThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES,
				NUMBER_OF_CORES, // Max pool size
				KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mBluetoothWorkQueue);

	}

	public void handleState(BluetoothTask btTask, int state)
	{
		switch (state) {
		case BT_STATE_FAILED:
			// Creates a message saying a Bluetooth operation failed
			Message failedMessage = mHandler.obtainMessage(state, btTask);
			failedMessage.sendToTarget();	
			break;
			
		case BT_STATE_UNSUPPORTED:
			// Creates a message saying Bluetooth is no supported
			Message unsupportedMesage = mHandler.obtainMessage(state, btTask);
			unsupportedMesage.sendToTarget();
			break;
			
		default:
		}
	}

	static public BluetoothManager getInstance()
	{
		return sInstance;
	}

	static public void enableBluetooth(Activity activity)
	{
		// TODO: Convert this into a runnable
		try {
			// Set up Bluetooth
			if (mBTAdapter == null) {
				throw new BTException(
						"Bluetooth is not supported on your device");
			}

			if (mBTAdapter.isEnabled() == false) {
				// Ask to turn on Bluetooth
				Intent enableBTIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				activity.startActivityForResult(enableBTIntent,
						REQUEST_ENABLE_BT);
			}
		}
		catch (BTException e) {
			abortWithAlert(e.getMessage(), activity);
		}
	}
	
	static public BluetoothTask connect() {
		// Create a new BluetoothTask
		BluetoothTask connectTask = new BluetoothTask();
		
		// Initialize the connection task
		connectTask.initializeConnectTask(BluetoothManager.sInstance);
		
		/*
		 *  Execute the task's connect Runnable in order to connect to
		 *  the remote Bluetooth device.
		 */
		BluetoothManager.sInstance.mBluetoothThreadPool.execute(connectTask.getConnectRunnable());
		
		// TODO: add a 'connecting' animation
		
		return connectTask;
	}

	static public BluetoothTask disconnect() {
		BluetoothTask disconnectTask = new BluetoothTask();
		
		// Initialize the disconnection task
		disconnectTask.initializeDisconnectTask(BluetoothManager.sInstance);
		
		/*
		 * Execute the task's disconnect Runnable in order to disconnect from
		 * remote Bluetooth device.
		 */
		BluetoothManager.sInstance.mBluetoothThreadPool.execute(disconnectTask.getDisconnectRunnable());
		
		return disconnectTask;
	}
	
	
	static public void cancelAll() {
		/*
		 * Creates an array of tasks that's the same size as the work queue.
		 */
		BluetoothTask[] taskArray = new BluetoothTask[sInstance.mBluetoothWorkQueue.size()];
		
		// Populates the array with the task objects in the queue
		sInstance.mBluetoothWorkQueue.toArray(taskArray);
		
		// Stores the array length so we can iterate over the array
		int taskArrayLen = taskArray.length;
		
		/*
		 * Locks on the singleton to ensure that other processes aren't mutating Threads,
		 * then iterates over the array of tasks and interrupts the task's current Thread.
		 */
		synchronized (sInstance) {
			// Iterates over the task array
			for (int taskArrayIndex = 0; taskArrayIndex < taskArrayLen; ++taskArrayIndex) {
				Thread thread = taskArray[taskArrayIndex].getCurrentThread();
				
				if (thread != null) {
					thread.interrupt();
				}
			}
		}
	}
	
//	private static byte[] execute(byte[] request)
//	{
//		byte[] response = null;
//
//		mBTLock.lock();
//
//		// Wait for the Bluetooth socket to be created and connected
//		while (mBTSocket == null || !mBTSocket.isConnected()) {
//			try {
//				mBTWait.await();
//			}
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		try {
//			// Request all outlets
//			byte[] req_outlets = new byte[] { 0x03, 0x00, 0x00, 0x00 };
//			mBTOutput.write(req_outlets);
//
//			// Read response
//			byte[] response = new byte[1024];
//			int bytes_read = mBTInput.read(response);
//			Log.i("SMART_OUTLETS_BT", bytesToHex(response, bytes_read));
//
//		}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		mBTLock.unlock();
//
//		return response;
//	}

	public static class BTException extends Exception
	{
		public BTException(String message)
		{
			super(message);
		}
	}

	public static void abortWithAlert(final String message,
			final Activity activity)
	{
		final Builder dialog = new AlertDialog.Builder(activity);

		dialog.setTitle("Fatal Error").setMessage(message)
				.setPositiveButton("Exit", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1)
					{
						activity.finish();
					}
				});

		activity.runOnUiThread(new Runnable() {
			public void run()
			{
				dialog.show();
			}
		});
	}

	public Lock getLock()
	{
		return mBTLock;
	}

	public Condition getCondition()
	{
		return mBTWait;
	}

	public BluetoothAdapter getAdapter()
	{
		return mBTAdapter;
	}

	public void setSocket(BluetoothSocket socket)
	{
		mBTSocket = socket;
	}
	
	public BluetoothSocket getSocket() {
		return mBTSocket;
	}
}
