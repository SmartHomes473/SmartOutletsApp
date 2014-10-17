package com.smrthaus.smartoutlets;

import java.util.ArrayList;

import com.smrthaus.smartoutlets.Outlet.State;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Adapter for populating a ListView with a list of Outlets.
 * 
 * @author nick
 */
public class ControlListAdapter extends ArrayAdapter<Outlet>
{
	private ArrayList<Outlet>	mItems;
	private Vibrator sVibrator;

	public ControlListAdapter ( Context context, int textViewResourceId,
			ArrayList<Outlet> items, Vibrator vibrator )
	{
		super(context, textViewResourceId, items);
		mItems = items;
		sVibrator = vibrator;
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
		Outlet outlet = mItems.get(position);
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