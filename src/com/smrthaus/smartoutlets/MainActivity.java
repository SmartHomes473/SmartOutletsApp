package com.smrthaus.smartoutlets;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends FragmentActivity
{

	SmartOutletsPagerAdapter	mPagerAdapter;
	ViewPager					mViewPager;

	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		// Display the splash screen
		setContentView(R.layout.activity_main);

		/*
		 * TODO: Move this to it's own activity
		 */
		// ImageView myImageView = (ImageView) findViewById(R.id.splash_image);
		// Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,
		// R.anim.fadein);
		// myImageView.startAnimation(myFadeInAnimation); // Set animation to
		// your
		// // ImageView
		// findViewById(R.id.container).setVisibility(View.INVISIBLE);

		/*
		 * Setup the Pager
		 */
		mPagerAdapter = new SmartOutletsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
	    mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getActionBar().setSelectedNavigationItem(position);
	                }
	            });

		/*
		 * Configure the ActionBar
		 */
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected ( ActionBar.Tab tab,
					FragmentTransaction ft )
			{
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected ( ActionBar.Tab tab,
					FragmentTransaction ft )
			{
				// hide the given tab
			}

			@Override
			public void onTabReselected ( ActionBar.Tab tab,
					FragmentTransaction ft )
			{
				// probably ignore this event
			}
		};

		// Add the tabs
	    for (int position = 0; position < mPagerAdapter.getCount(); ++position) {
	        actionBar.addTab(
	                actionBar.newTab()
	                        .setText(mPagerAdapter.getTitle(position))
	                        .setTabListener(tabListener));
	    }
		
		/*
		 * Enable Bluetooth TODO: move this to the splash activity
		 */
		BluetoothManager.enableBluetooth(this);
	}

	@Override
	protected void onResume ( )
	{
		super.onResume();

		// connect to the server
		BluetoothManager.connect();
	}

	@Override
	protected void onPause ( )
	{
		super.onPause();

		// Cancel all ongoing Bluetooth operations
		BluetoothManager.cancelAll();
		BluetoothManager.disconnect();
	}

	@Override
	public boolean onOptionsItemSelected ( MenuItem item )
	{
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
	public static class PlaceholderFragment extends Fragment
	{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String	ARG_SECTION_NUMBER	= "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance ( int sectionNumber )
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment ( )
		{
		}

		@Override
		public View onCreateView ( LayoutInflater inflater,
				ViewGroup container, Bundle savedInstanceState )
		{
			View rootView = inflater.inflate(R.layout.fragment_outlets,
					container, false);
			return rootView;
		}

		@Override
		public void onAttach ( Activity activity )
		{
			super.onAttach(activity);
		}
	}
}
