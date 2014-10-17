package com.smrthaus.smartoutlets;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smrthaus.smartoutlets.Outlet.State;
import com.smrthaus.smartoutlets.bluetooth.BluetoothManager;

public class PowerFragment extends ListFragment
{
	private ArrayList<Outlet>	mOutletsList		= null;
	private Boolean				mListInitialized	= true;
	private PowerListAdapter	mAdapter;
	private Runnable			viewOutlets;
	private ProgressBar			mActivityIndicator;

	// Keys used for saving and restoring state
	final static private String	STATE_OUTLETS_LIST	= "state_outlets_list";

	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState )
	{
		super.onCreateView(inflater, container, savedInstanceState);

		View view;

		// log
		Log.i("FRAGMENT_CREATE_VIEW", "onCreateView() called");

		// Check if we need to re-initialize the list
		mListInitialized = true;
		if (mOutletsList == null) {
			mListInitialized = false;
			mOutletsList = new ArrayList<Outlet>();
		}

		// inflate the view using this fragment's layout
		view = inflater.inflate(R.layout.fragment_outlets, container, false);

		// Create and register the adapter for the ListView
		mAdapter = new PowerListAdapter(getActivity(),
				R.layout.power_list_item, mOutletsList);
		setListAdapter(mAdapter);

		return view;
	}

	@Override
	public void onViewCreated ( View view, Bundle savedInstanceState )
	{
		super.onViewCreated(view, savedInstanceState);

		// log
		Log.i("FRAGMENT_CREATE_VIEW", "onCreateView() called");

		// Load the list of outlets if it hasn't previously been initialized
		if (mListInitialized == false) {
			// Starts the activity indicator to indicate we are loading the
			// outlets
			// in the background
			mActivityIndicator = (ProgressBar) view
					.findViewById(R.id.outlets_list_progress);
			mActivityIndicator.setVisibility(View.VISIBLE);

			// Load outlets
			BluetoothManager.loadOutlets(getListView());
		}
	}

	/**
	 * 
	 */
	@Override
	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		Log.i("FRAGMENT_CREATE", "onCreate() called");
		
		// Display menu options
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu ( Menu menu, MenuInflater inflater )
	{
		inflater.inflate(R.menu.outlet_power, menu);
	}

	@Override
	public boolean onOptionsItemSelected ( MenuItem item )
	{
		// handle item selection
		switch (item.getItemId()) {
		case R.id.refresh_outlets:
			// Clear the outlet list
			mAdapter.clear();

			// Displays the loading animation
			ViewGroup parent = (ViewGroup) getListView().getParent();
			parent.findViewById(R.id.outlets_list_progress).setVisibility(
					View.VISIBLE);

			// Queues a Bluetooth task to fetch outlets
			BluetoothManager.loadOutlets(getListView());

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
