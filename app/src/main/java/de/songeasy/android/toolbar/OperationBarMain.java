/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import de.songeasy.android.R;

/**
 * Operations bar for applying main actions on selected elements, as edit and delete.
 *
 * @author Joerg Krein
 *
 */
public class OperationBarMain extends OperationBar {
    static public enum ButtonEvent {
        DELETE, EDIT
    };

    static private ButtonToolBar buttons[] = {
            new ButtonToolBar(R.id.imageButtonDeleteElement, ButtonEvent.DELETE.ordinal()),
            new ButtonToolBar(R.id.imageButtonEditElement, ButtonEvent.EDIT.ordinal()) };


    public OperationBarMain(Context context) {
        this(context, null, 0);
    }


    public OperationBarMain(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public OperationBarMain(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setButtons(buttons);
        View.inflate(context, R.layout.lay_operation_bar_main, this);
    }
}
