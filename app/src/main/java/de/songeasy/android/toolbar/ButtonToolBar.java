/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.toolbar;

/**
 * Button for tool bars.
 *  Consisting of id of drawable and event to triggered on click.
 * 
 * @author krein
 * 
 */
public class ButtonToolBar {

	/** id of button drawable */
	private int id;

	/** event to send at onClick */
	private int event;


	/** Constructor */
	ButtonToolBar(int id, int event )
    {
		this.id = id;
		this.event = event;
	}


	/**
	 * Get id of corresponding view
	 * 
	 * @return id of enum value
	 */
	public int getId() {
		return this.id;
	}


	/**
	 * Get event of corresponding view to send at onClick()
	 * 
	 * @return event of View
	 */
	public int getEvent() {
		return event;
	}
}
