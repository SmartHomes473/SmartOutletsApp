package com.smrthaus.smartoutlets;

public class Outlet {

	private String outletId;
	private String outletName;
	private State outletState;

	public enum State {
		ON, OFF
	}

	/**
	 * Constructor.  Initializes the Outlet's id, name and state.
	 * 
	 * @param id  Unique identifier for the outlet.
	 * @param name  Name describing the outlet.
	 * @param state  Power state of the outlet.  Either State.ON or State.OFF.
	 */
	public Outlet (String id, String name, State state) {
		// initialize state
		this.outletId = id;
		this.outletName = name;
		this.outletState = state;
	}
	
	/**
	 * Constructor.  Initializes the Outlet's id and name.  Defaults power state to State.OFF.
	 * 
	 * @param id  Unique identifier for the outlet.
	 * @param name  Name describing the outlet.
	 */
	public Outlet (String id, String name) {
		this(id, name, State.OFF);
	}
	
	public String getId() {
		return outletId;
	}

	public String getName() {
		return outletName;
	}

	public void setName(String outletName) {
		this.outletName = outletName;
	}

	public State getState() {
		return outletState;
	}

	public void setState(State state) {
		if (this.outletState == state) {
			return;
		}
		
		this.outletState = state;
		// TODO: API call to change state
	}
}