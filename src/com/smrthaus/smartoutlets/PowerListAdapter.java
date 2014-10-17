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
public class PowerListAdapter extends ArrayAdapter<Outlet>
{
	private ArrayList<Outlet>	mItems;

	public PowerListAdapter ( Context context, int textViewResourceId,
			ArrayList<Outlet> items )
	{
		super(context, textViewResourceId, items);
		mItems = items;
	}

	@Override
	public View getView ( int position, View convertView, ViewGroup parent )
	{
		View view = convertView;

		// inflate the view if not defined
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.power_list_item, null);
		}

		// set view elements based on Outlet properties
		Outlet outlet = mItems.get(position);
		if (outlet != null) {
			TextView label = (TextView) view.findViewById(R.id.outlet_name);
			TextView power = (TextView) view.findViewById(R.id.outlet_power);

			// set the Outlet's label
			if (label != null) {
				label.setText(outlet.getName());
			}

			// set the toggle button state
			if (power != null) {
				power.setText(outlet.getPowerString());
			}

		}

		return view;
	}
}