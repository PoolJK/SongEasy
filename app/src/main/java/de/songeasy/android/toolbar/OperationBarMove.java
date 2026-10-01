/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import de.songeasy.android.R;

/**
 * Tool bar for moving elements.
 *
 * @author jorgkrein
 */
public class OperationBarMove extends OperationBar {

    static public enum ButtonEvent {
        UP, DOWN, LEFT, RIGHT
    }

    ;

    static private ButtonToolBar buttons[] = {
            new ButtonToolBar(R.id.imageButtonUp,
                    ButtonEvent.UP.ordinal()),
            new ButtonToolBar(R.id.imageButtonDown,
                    ButtonEvent.DOWN.ordinal()),
            new ButtonToolBar(R.id.imageButtonLeft,
                    ButtonEvent.LEFT.ordinal()),
            new ButtonToolBar(R.id.imageButtonRight,
                    ButtonEvent.RIGHT.ordinal())};


    public OperationBarMove(Context context) {
        this(context, null, 0);
    }


    public OperationBarMove(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public OperationBarMove(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setButtons(buttons);
        View.inflate(context, R.layout.lay_operation_bar_move, this);
    }

}
