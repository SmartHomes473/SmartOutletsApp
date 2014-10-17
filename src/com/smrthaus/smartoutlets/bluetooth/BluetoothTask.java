package com.smrthaus.smartoutlets.bluetooth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.smrthaus.smartoutlets.Outlet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.widget.ListView;

public class BluetoothTask
{
	/*
	 * Creates a weak reference to the ListView that this Task will populate.
	 * The weak reference prevents memory leaks and crashes, because it
	 * automatically tracks the "state" of the variable it backs. If the
	 * reference becomes invalid, the weak reference is garbage- collected. This
	 * technique is important for referring to objects that are part of a
	 * component lifecycle. Using a hard reference may cause memory leaks as the
	 * value continues to change; even worse, it can cause crashes if the
	 * underlying component is destroyed. Using a weak reference to a View
	 * ensures that the reference is more transitory in nature.
	 */
	private WeakReference<ListView>	mOutletsListWeakRef;
	private WeakReference<Outlet>	mUpdatedOutletWeakRef;

	// Runnables for Bluetooth tasks
	private Runnable				mConnectRunnable;
	private Runnable				mDisconnectRunnable;
	private Runnable				mLoadOutletsRunnable;
	private Runnable				mUpdateOutletRunnable;

	// Input buffer for reading Bluetooth responses
	byte[]							mInputBuffer;

	// The Thread on which the task is currently running
	private Thread					mCurrentThread;

	// A reference to a list of Outlets
	private ArrayList<Outlet>		mOutletList	= null;

	// An object that contains the ThreadPool singleton
	private static BluetoothManager	sBluetoothManager;

	// Create a class containing a connect object
	BluetoothTask ( )
	{
		mConnectRunnable = new ConnectRunnable(this);
		mDisconnectRunnable = new DisconnectRunnable(this);
		mLoadOutletsRunnable = new LoadOutletsRunnable(this);
		mUpdateOutletRunnable = new UpateOutletRunnable(this);

		sBluetoothManager = BluetoothManager.getInstance();
	}

	void initializeConnectTask ( BluetoothManager bluetoothManager )
	{
		sBluetoothManager = bluetoothManager;
	}

	void initializeDisconnectTask ( BluetoothManager bluetoothManager )
	{
		sBluetoothManager = bluetoothManager;
	}

	public void initializeLoadOutletsTask ( BluetoothManager bluetoothManager,
			ListView outletList )
	{
		sBluetoothManager = bluetoothManager;

		// Instantiates the weak reference to the ListView we'll be updating
		mOutletsListWeakRef = new WeakReference<ListView>(outletList);
	}

	public void initializeUpdateOutletTask ( BluetoothManager bluetoothManager,
			Outlet outlet )
	{
		sBluetoothManager = bluetoothManager;
		
		// Instantiates a weak reference to the Outlet we'll be updating
		mUpdatedOutletWeakRef = new WeakReference<Outlet>(outlet);
	}

	Runnable getConnectRunnable ( )
	{
		return mConnectRunnable;
	}

	Runnable getDisconnectRunnable ( )
	{
		return mDisconnectRunnable;
	}

	Runnable getLoadOutletsRunnable ( )
	{
		return mLoadOutletsRunnable;
	}

	Runnable getUpdateOutletRunnable ( )
	{
		return mUpdateOutletRunnable;
	}

	public byte[] getByteBuffer ( )
	{
		return mInputBuffer;
	}

	public void setByteBuffer ( byte[] inputBuffer )
	{
		mInputBuffer = inputBuffer;
	}

	void handleState ( int state )
	{
		sBluetoothManager.handleState(this, state);
	}

	void recycle ( )
	{
		mInputBuffer = null;

		// Deletes the weak reference to the listView
		if (null != mOutletsListWeakRef) {
			mOutletsListWeakRef.clear();
			mOutletsListWeakRef = null;
		}
	}

	public void setConnectThread ( Thread currentThread )
	{
		setCurrentThread(currentThread);
	}

	public Lock getLock ( )
	{
		return sBluetoothManager.getLock();
	}

	public Condition getCondition ( )
	{
		return sBluetoothManager.getCondition();
	}

	public BluetoothAdapter getAdapter ( )
	{
		return sBluetoothManager.getAdapter();
	}

	public void setSocket ( BluetoothSocket socket )
	{
		sBluetoothManager.setSocket(socket);
	}

	public BluetoothSocket getSocket ( )
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
	public Thread getCurrentThread ( )
	{
		synchronized (sBluetoothManager) {
			return mCurrentThread;
		}
	}

	/*
	 * Sets the identifier for the current Thread. This must be a synchronized
	 * operation; see the notes for getCurrentThread()
	 */
	public void setCurrentThread ( Thread thread )
	{
		synchronized (sBluetoothManager) {
			mCurrentThread = thread;
		}
	}

	public ListView getOutletListView ( )
	{
		return null != mOutletsListWeakRef ? mOutletsListWeakRef.get() : null;
	}
	

	public void setOutletList ( ArrayList<Outlet> outletList )
	{
		mOutletList = outletList;
	}

	public ArrayList<Outlet> getOutletList ( )
	{
		return mOutletList;
	}
	
	public Outlet getUpdatedOutlet ( )
	{
		return null != mUpdatedOutletWeakRef ? mUpdatedOutletWeakRef.get() : null;
	}
}
