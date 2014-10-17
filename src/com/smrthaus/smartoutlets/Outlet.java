package com.smrthaus.smartoutlets;

import android.os.Parcel;
import android.os.Parcelable;

public class Outlet implements Parcelable
{

	private String	mOutletId;
	private String	mOutletName;
	private State	mOutletState;
	private int		mCurrentOutletPower;
	private int		mLastOutletPower;

	public enum State {
		ON, OFF
	}

	/**
	 * Constructor. Initializes the Outlet's id, name and state.
	 * 
	 * @param id
	 *            Unique identifier for the outlet.
	 * @param name
	 *            Name describing the outlet.
	 * @param state
	 *            Power state of the outlet. Either State.ON or State.OFF.
	 */
	public Outlet ( String id, String name, int power, State state )
	{
		// initialize state
		mOutletId = id;
		mOutletName = name;
		mCurrentOutletPower = power;
		mLastOutletPower = power;
		mOutletState = state;
	}

	/**
	 * Constructor. Initializes the Outlet's id and name. Defaults power state
	 * to State.OFF.
	 * 
	 * @param id
	 *            Unique identifier for the outlet.
	 * @param name
	 *            Name describing the outlet.
	 */
	public Outlet ( String id, String name, int power )
	{
		this(id, name, power, State.OFF);
	}

	public String getId ( )
	{
		return mOutletId;
	}

	public String getName ( )
	{
		return mOutletName;
	}

	public void setName ( String outletName )
	{
		this.mOutletName = outletName;
	}

	public State getState ( )
	{
		return mOutletState;
	}

	public void setState ( State state )
	{
		if (mOutletState == state) {
			return;
		}

		mOutletState = state;
		
		if (state == State.OFF) {
			mLastOutletPower = mCurrentOutletPower;
			mCurrentOutletPower = -1;
		}
		
		BluetoothManager.updateOutlet(this);
	}
	
	public int getPower ( )
	{
		return mCurrentOutletPower;
	}
	
	public int getLastPower ( )
	{
		return mLastOutletPower;
	}
	
	public void setPower ( int power ) {
		mCurrentOutletPower = power;
	}

	/**
	 * Serializes the object as a Parcel
	 */
	public void writeToParcel ( Parcel out, int flags )
	{
		out.writeString(mOutletId);
		out.writeString(mOutletName);
		out.writeSerializable(mOutletState);
	}

	// Special CREATOR object that instantiates an Outlet from a Parcel
	public static final Parcelable.Creator<Outlet>	CREATOR;
	static {
		CREATOR = new Parcelable.Creator<Outlet>() {
			public Outlet createFromParcel ( Parcel in )
			{
				return new Outlet(in);
			}

			public Outlet[] newArray ( int size )
			{
				return new Outlet[size];
			}
		};
	}

	/**
	 * Special constructor for instantiating an Outlet from a Parcel.
	 * 
	 * @param in
	 */
	private Outlet ( Parcel in )
	{
		mOutletId = in.readString();
		mOutletName = in.readString();
		mOutletState = (State) in.readSerializable();
	}

	/**
	 * Implemented for Parcelable interface
	 */
	@Override
	public int describeContents ( )
	{
		return 0;
	}
}