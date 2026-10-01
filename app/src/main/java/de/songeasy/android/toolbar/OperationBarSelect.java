/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import de.songeasy.android.R;

/**
 * Operation bar for selecting elements relativ to another.
 * 
 * @author Joerg Krein
 * 
 */
public class OperationBarSelect extends OperationBar {

	static public enum ButtonEvent {
		PREVIOUS, NEXT
	};

	static private ButtonToolBar buttons[] = {
			new ButtonToolBar(R.id.imageButtonPrevious,
					ButtonEvent.PREVIOUS.ordinal()),
			new ButtonToolBar(R.id.imageButtonNext, ButtonEvent.NEXT.ordinal()) };


	public OperationBarSelect(Context context) {
		this(context, null, 0);
	}


	public OperationBarSelect(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public OperationBarSelect(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setButtons(buttons);
		View.inflate(context, R.layout.lay_bottom_bar_select, this);
	}
}
