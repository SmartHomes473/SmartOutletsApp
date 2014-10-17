package com.smrthaus.smartoutlets;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SmartOutletsPagerAdapter extends FragmentPagerAdapter
{
	ArrayList<Fragment>	mFragments;
	ArrayList<String>	mTitles;

	public SmartOutletsPagerAdapter ( FragmentManager fm )
	{
		super(fm);

		/*
		 * TODO: combine these into a single data structure to ensure they never
		 * get out of sync.
		 */
		mFragments = new ArrayList<Fragment>();
		mTitles = new ArrayList<String>();

		/*
		 * Add the power control Fragment.
		 */
		mFragments.add(new ControlFragment());
		mTitles.add("Control");

		/*
		 * Add the power consumption Fragment.
		 */
		mFragments.add(new PowerFragment());
		mTitles.add("Power");

		/*
		 * Add the power stats Fragment.
		 */
		mFragments.add(new StatsFragment());
		mTitles.add("Stats");
	}

	@Override
	public Fragment getItem ( int index )
	{
		return mFragments.get(index);
	}

	@Override
	public int getCount ( )
	{
		return mFragments.size();
	}

	public String getTitle ( int position )
	{
		return mTitles.get(position);
	}
}
