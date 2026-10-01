/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import de.songeasy.android.interfaces.OnOperationListener;

/**
 * A bar with image buttons to carry out operations to selected elements.
 * 
 * @author Joerg Krein
 * 
 */
public abstract class OperationBar extends RelativeLayout implements
		View.OnClickListener {

	/** Buttons of this event bar with resource id and event */
	private ButtonToolBar buttons[];

	/** listener to be called on selection of tool */
	protected OnOperationListener onOperationListener = null;


	// ++++++++++ setters, getters +++++++
	// +++++++++++++++++++++++++++++++++++

	public void setOnOperationListener(OnOperationListener onOperationListener) {
		this.onOperationListener = onOperationListener;

		for (ButtonToolBar button : buttons) {

			// using View class for supporting different Button classes
			View ibut = findViewById(button.getId());
			ibut.setOnClickListener(this);
		}
	}


	/**
	 * Set buttons to handle by operation bar. Can't be put as constructor
	 * parameter as inflater uses standard constructors of RelativeLayout for
	 * child classes.
	 * 
	 * @param buttons
	 *            ToolBarButton array with toggle buttons
	 */
	protected void setButtons(ButtonToolBar buttons[]) {
		this.buttons = buttons;
	}


	// ++++++++++ Constructors +++++++++++
	// +++++++++++++++++++++++++++++++++++

	public OperationBar(Context context) {
		this(context, null, 0);
	}


	public OperationBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	/**
	 * Constructor of class ExecutionBar For parameters see parent class
	 * RelativeLayout.
	 */
	public OperationBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	/**
	 * Show operation bar.
	 */
    public void show() {

		setVisibility(VISIBLE);
	}


	/**
	 * Hide operation bar.
	 */
    public void hide() {

		setVisibility(INVISIBLE);
	}


	@Override
	public void onClick(View v) {

		// find button with same view id
		for (ButtonToolBar button : buttons) {
			if (button.getId() == v.getId()) {

				// call listener with button event
				if (onOperationListener != null)
					onOperationListener.onOperate(button.getEvent());
			}
		}
	}

}
