/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import de.songeasy.android.interfaces.OnToolDeselectedListener;
import de.songeasy.android.interfaces.OnToolSelectedListener;


/**
 * A tool bar consisting of some toggle buttons. A clicked button stays selected
 * until another one is clicked. Each tool selection calls a registered listener
 * with the button event as parameter.
 * 
 * @author Joerg Krein
 * 
 */
public abstract class ToolBar extends RelativeLayout implements
		View.OnClickListener {

	/** Buttons of this toolbar with resource id and event */
	private ButtonToolBar buttons[];

	/** remember selected tool id */
	private int selectedToolId;

	/** listener to be called on selection of tool */
	protected OnToolSelectedListener onSelectionListener = null;

	/** listener to be called on deselection of tool */
	protected OnToolDeselectedListener onDeselectionListener = null;


	// ++++++++++ setters, getters +++++++
	// +++++++++++++++++++++++++++++++++++

	/**
	 * Set listener for deselection of tool.
	 * 
	 * @param listener
	 *            listener to be called on deseslection
	 */
    public void setOnDeselectionListener(OnToolDeselectedListener listener) {

		this.onDeselectionListener = listener;
	}


	/**
	 * Set buttons to handle by tool bar. Can't be put as constructor parameter
	 * as inflater uses standard constructors of RelativeLayout for child
	 * classes.
	 * 
	 * @param buttons
	 *            ToolBarButton array with toggle buttons
	 */
	protected void setButtons(ButtonToolBar buttons[]) {
		this.buttons = buttons;
	}


	/**
	 * Register the same onClick listener for all buttons. Needs to be called
	 * after the view is inflated.
	 * 
	 * @param listener
	 *            OnToolSelectedListener to get called on button click
	 */
    public void setOnSelectionListener(OnToolSelectedListener listener) {

		this.onSelectionListener = listener;

		// set onClick listener for all buttons
		for (ButtonToolBar b : buttons) {
			ToggleButton tbut = (ToggleButton) findViewById(b.getId());
			tbut.setOnClickListener(this);
		}
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
	}


	// +++++++++++ constructors ++++++++++
	// +++++++++++++++++++++++++++++++++++

	public ToolBar(Context context) {
		this(context, null, 0);
	}


	public ToolBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	/**
	 * Constructor of class ToolBar.
	 * 
	 * @param context
	 *            context to pass to parent class
	 * @param attrs
	 *            attributes of relative layout from xml
	 * @param defStyle
	 *            style definition of relative layout
	 */
	public ToolBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		//setVisibility(INVISIBLE);
	}


	/**
	 * Uncheck all buttons but the one given as parameter.
	 * 
	 * @param id
	 *            id of button to be checked only
	 */
	private void unCheckAllButtonBut(int id) {

		for (ButtonToolBar b : buttons) {
			if (b.getId() == id)
				((ToggleButton) findViewById(b.getId())).setChecked(true);
			else
				((ToggleButton) findViewById(b.getId())).setChecked(false);
		}
	}


	/**
	 * Call tool bar listener to activate selected tool.
	 * 
	 * @param id
	 *            view id of clicked button
	 */
	private void callSelectedListener(int id) {

		if (onSelectionListener == null)
			return;

		for (ButtonToolBar b : buttons) {
			if (b.getId() == id) {
				onSelectionListener.onToolSelected(b.getEvent());
				break;
			}
		}
	}


	/**
	 * Call tool bar listener to deactivate previous selected tool.
	 * 
	 * @param id
	 *            view id of clicked button, -1 for button is unchecked
	 */
	private void callDeselectedListener(int id) {

		if (onDeselectionListener == null)
			return;

		for (ButtonToolBar b : buttons) {
			if (b.getId() == id) {
				onDeselectionListener.onToolDeselected(b.getEvent());
				break;
			}
		}
	}


	/**
	 * Select tool to be checked directly.
	 * 
	 * @param event
	 *            event parameter of ToolBarButton
	 */
    public void selectTool(int event) {

		for (ButtonToolBar b : buttons) {
			if (b.getEvent() == event) {

				selectedToolId = b.getId();

				unCheckAllButtonBut(b.getId());
			}
		}
	}

	/** Deselect all ToolBarButton
	 * Should only be used in case of emergency!
	 */
	public void deselectTool() {

		for (ButtonToolBar b : buttons) {
				((ToggleButton) findViewById(b.getId())).setChecked(false);
		}
	}

	/**
	 * Show tool bar. Call listener with currently checked button, to activate
	 * selected tool.
	 */
    public void show() {

		// show this tool bar
		setVisibility(View.VISIBLE);

		// call listener with event of currently checked button
		for (ButtonToolBar b : buttons) {
			if (b.getId() == selectedToolId) {

				if (onSelectionListener != null)
					onSelectionListener.onToolSelected(b.getEvent());
			}
		}
	}


	/**
	 * Hide tool bar.
	 */
    public void hide() {
		setVisibility(View.INVISIBLE);
	}


	/**
	 * onClick listener for all toggle buttons of tool bar unchecks all buttons
	 * but selected one calls listener
	 */
	@Override
	public void onClick(View v) {
		if(buttons.length > 1) {
			if (((ToggleButton) v).isChecked() == true) {
				// only one button can be checked at a time
				unCheckAllButtonBut(v.getId());
				selectedToolId = v.getId();
				// call listener for selection
				callSelectedListener(v.getId());
			} else {
				// call listener for deselection if registered
				if (onDeselectionListener != null)
					callDeselectedListener(v.getId());
				else
					// select last tool
					unCheckAllButtonBut(selectedToolId);
			}
		} else {
			// if only one tool is available, always call selected listener
			callSelectedListener(v.getId());
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {

		// save state of superior class
		Parcelable superState = super.onSaveInstanceState();
		SavedState myState = new SavedState(superState);

		// save last selected tool
		myState.selectedToolId = selectedToolId;

		// save state of visibility
		if (this.getVisibility() == View.VISIBLE)
			myState.isVisible = true;
		else
			myState.isVisible = false;

		return myState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {

		if (!state.getClass().equals(SavedState.class)) {
			// didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;

		// get state including superior class
		super.onRestoreInstanceState(myState.getSuperState());

		// restore last selected tool
		selectedToolId = myState.selectedToolId;

		unCheckAllButtonBut(selectedToolId);

		// restore state of visibility
		if (myState.isVisible)
			show();
		else
			hide();
	}

	/**
	 * SavedState, a subclass of {@link BaseSavedState}, will store the state of
	 * this View.
	 */
	private static class SavedState extends BaseSavedState {

		private int selectedToolId;
		private boolean isVisible;


		/**
		 * Constructor used when reading from a parcel. Reads the state of the
		 * superclass.
		 * 
		 * @param source
		 *            parcel to read from
		 */
		public SavedState(Parcel source) {

			super(source);

			// read state variables from parcel
			selectedToolId = source.readInt();
			isVisible = (Boolean) source.readValue(null);
		}


		/**
		 * Constructor called by derived classes when creating their SavedState
		 * objects.
		 * 
		 * @param superState
		 *            state of superior class of this view
		 */
		public SavedState(Parcelable superState) {

			super(superState);
		}


		@Override
		public void writeToParcel(Parcel dest, int flags) {

			super.writeToParcel(dest, flags);

			// save state variables in parcel
			dest.writeInt(selectedToolId);
			dest.writeValue(isVisible);
		}

		/**
		 * Interface for creating instance of Parcelable.
		 */
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel(Parcel in) {

				return new SavedState(in);
			}


			public SavedState[] newArray(int size) {

				return new SavedState[size];
			}
		};
	}

} // class ToolBar
