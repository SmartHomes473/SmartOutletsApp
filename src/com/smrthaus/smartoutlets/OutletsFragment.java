package com.smrthaus.smartoutlets;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.smrthaus.smartoutlets.Outlet.State;

public class OutletsFragment extends ListFragment {
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Outlet> m_outlets = null;
	private OutletAdapter m_adapter;
	private Runnable viewOutlets;
	private ProgressBar mProgress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view;
		
		// log
		Log.i("FRAGMENT_CREATE_VIEW", "onCreateView() called");
		
		// inflate the view using this fragment's layout
		view = inflater.inflate(R.layout.fragment_outlets, container, false);

		// set up list of outlets
		m_outlets = new ArrayList<Outlet>();

		// set up list adapter
		this.m_adapter = new OutletAdapter(getActivity(),
				R.layout.outlets_list_item, m_outlets);
		setListAdapter(this.m_adapter);

		// start progress bar
		mProgress = (ProgressBar) view.findViewById(R.id.outlets_list_progress);
		mProgress.setVisibility(View.VISIBLE);
		
		// thread to get outlets
		viewOutlets = new Runnable() {
			@Override
			public void run() {
				getOutlets();
			}
		};
		Thread thread = new Thread(null, viewOutlets,
				"SmartOutletsFetchOutlets");
		thread.start();

		return view;
	}

	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("FRAGMENT_CREATE", "onCreate() called");
	}

	/**
	 * Build an array of mock Outlets to populate the ListView.  Delays for ~2 seconds to simulate loading.
	 */
	private void getOutlets() {
		try {
			
			// construct outlet list
			m_outlets = new ArrayList<Outlet>();
			m_outlets.add(new Outlet("outlet1", "Outlet 1", State.OFF));
			m_outlets.add(new Outlet("outlet2", "Outlet 2", State.ON));
			m_outlets.add(new Outlet("outlet3", "Outlet 3", State.OFF));

			// sleep the thread to simulate loading time
			Thread.sleep(2000);

			// log array initialization
			Log.i("ARRAY", "" + m_outlets.size());
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}

		getActivity().runOnUiThread(returnRes);
	}

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (m_outlets != null && m_outlets.size() > 0) {
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < m_outlets.size(); ++i) {
					m_adapter.add(m_outlets.get(i));
				}
				m_adapter.notifyDataSetChanged();
			}
			mProgress.setVisibility(View.INVISIBLE);
		}
	};

	/**
	 * Adapter for populating a ListView with a list of Outlets.
	 * 
	 * @author nick
	 */
	private class OutletAdapter extends ArrayAdapter<Outlet> {
		private ArrayList<Outlet> items;

		public OutletAdapter(Context context, int textViewResourceId,
				ArrayList<Outlet> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
				Switch toggle = (Switch) view.findViewById(R.id.togglebutton);

				// set the Outlet's label
				if (label != null) {
					label.setText(outlet.getName());
				}

				// set the toggle button state
				if (toggle != null) {
					toggle.setChecked(outlet.getState() == Outlet.State.ON ? true
							: false);
				}
			}

			return view;
		}
	}
}
