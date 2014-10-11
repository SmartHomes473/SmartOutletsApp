package com.smrthaus.smartoutlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    
    /**
     * Bluetooth state.
     */
	private static final int REQUEST_ENABLE_BT = 1;
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String address = "E0:06:E6:BE:AB:9B";
	private BluetoothAdapter mBTAdapter = null;
	private BluetoothSocket mBTSocket = null;
	private OutputStream mBTOutput = null;
	private InputStream mBTInput = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        // Set up Bluetooth
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
        	abortWithAlert("Fatal Error", "Bluetooth is not supported on your device.  Aborting.");
        }
        if (mBTAdapter.isEnabled() == false) {
        	// Ask to turn on Bluetooth
        	Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);	
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Create Bluetooth socket
    	BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
    	try {
    		mBTSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
    	}
    	catch (IOException e) {
    		abortWithAlert("Fatal Error", "socket creation failed" + e.getMessage() + ".");
    	}
    	
    	// Discovery is resource intensive.  Disable it.
    	mBTAdapter.cancelDiscovery();
    	
    	// Establish connection.
    	// XXX: This action blocks until it connects.  We want to have a
    	//      loading screen and have this happen in the background.
    	try {
    		mBTSocket.connect();
    	}
    	catch (IOException e) {
    		try {
    			mBTSocket.close();
    			abortWithAlert("Fatal Error", "Openning socket failed: " + e.getMessage() + ".");
    		}
    		catch (IOException e2) {
    			abortWithAlert("Fatal Error", "Closing socket failed after connection failure: "
    					+ e2.getMessage() + ".");
    		}
    	}
    	
    	// Get input and output streams
    	try {
    		mBTOutput = mBTSocket.getOutputStream();
    		mBTInput = mBTSocket.getInputStream();
    	}
    	catch (IOException e) {
    		abortWithAlert("Fatal Error",
    				"Stream creation failed: " + e.getMessage() + ".");
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	// Flush the output buffer
    	if (mBTOutput != null) {
    		try {
				mBTOutput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	// Close the socket
    	try {
			mBTSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
    	Fragment newFragment;
    	switch (position) {
    	case 0:
    		newFragment = new OutletsFragment();
    		break;
		default:
			newFragment = PlaceholderFragment.newInstance(position);
    	
    	}
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_outlets);
                break;
            case 2:
                mTitle = getString(R.string.title_stats);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_outlets, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * Displays an AlertDialog containing an error message.  Forces app to finish.
     */
    public void abortWithAlert ( String title, String message ) {
    	new AlertDialog.Builder(this).setTitle(title)
    	.setMessage(message)
    	.setPositiveButton("Exit", new OnClickListener() {
    		public void onClick(DialogInterface arg0, int arg1) {
    			finish();
    		}
    	}).show();
    }
}
