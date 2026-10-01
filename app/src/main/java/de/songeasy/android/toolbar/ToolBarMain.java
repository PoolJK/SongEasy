/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import de.songeasy.android.R;

/**
 * Main editing tool bar for manipulating the score. The layout contains
 * ImageButtons too, that need to be handled separately.
 * 
 * @author Joerg Krein
 * 
 */
public class ToolBarMain extends ToolBar {

	static public enum ButtonEvent {
		SELECT
	};

	static private ButtonToolBar buttons[] = {
			new ButtonToolBar(R.id.toggleButtonSelectElement,
					ButtonEvent.SELECT.ordinal()) };


	public ToolBarMain(Context context) {
		this(context, null, 0);

	}


	public ToolBarMain(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public ToolBarMain(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setButtons(buttons);

		View.inflate(context, R.layout.lay_toolbar_main, this);

		// preselect tool
		selectTool(ButtonEvent.SELECT.ordinal());
	}

}
