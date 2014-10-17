package com.smrthaus.smartoutlets;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smrthaus.smartoutlets.Outlet.State;

public class ControlFragment extends ListFragment
{
	private ArrayList<Outlet>	mOutletsList		= null;
	private Boolean				mListInitialized	= true;
	private OutletAdapter		mAdapter;
	private Runnable			viewOutlets;
	private ProgressBar			mActivityIndicator;
	
	
	// Keys used for saving and restoring state
	final static private String	STATE_OUTLETS_LIST	= "state_outlets_list";

	private Vibrator sVibrator;
	
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
		mAdapter = new OutletAdapter(getActivity(), R.layout.outlets_list_item,
				mOutletsList);
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
		
		sVibrator = (Vibrator) getActivity().getSystemService(android.content.Context.VIBRATOR_SERVICE); 
	}

	/**
	 * Adapter for populating a ListView with a list of Outlets.
	 * 
	 * @author nick
	 */
	public class OutletAdapter extends ArrayAdapter<Outlet>
	{
		private ArrayList<Outlet>	items;

		public OutletAdapter ( Context context, int textViewResourceId,
				ArrayList<Outlet> items )
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView ( int position, View convertView, ViewGroup parent )
		{
			View view = convertView;

			// inflate the view if not defined
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.outlets_list_item, null);
			}

			// set view elements based on Outlet properties
			Outlet outlet = items.get(position);
			if (outlet != null) {
				TextView label = (TextView) view.findViewById(R.id.outlet_name);
				ToggleButton toggle = (ToggleButton) view.findViewById(R.id.togglebutton);

				// set the Outlet's label
				if (label != null) {
					label.setText(outlet.getName());
				}

				// set the toggle button state
				if (toggle != null) {
					toggle.setTag(R.id.togglebutton_outlet, outlet);
					
					toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged (
								CompoundButton buttonView, boolean isChecked )
						{
							Outlet outlet = (Outlet) buttonView
									.getTag(R.id.togglebutton_outlet);

							outlet.setState(isChecked ? State.ON : State.OFF);
						}
					});
					
					toggle.setOnClickListener(new OnClickListener() {
						public void onClick(View view) {
							sVibrator.vibrate(20);
						}
					});
					
					toggle.setChecked(outlet.getState() == Outlet.State.ON ? true
							: false);
				}

			}

			return view;
		}
	}
}
