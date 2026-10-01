/*
 * Copyright (C) 2013 Joerg Krein
 */

package de.songeasy.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import de.songeasy.android.interfaces.OnElementDeleteListener;
import de.songeasy.android.interfaces.OnElementEditListener;
import de.songeasy.android.interfaces.OnElementMoveListener;
import de.songeasy.android.interfaces.OnElementSelectNextListener;
import de.songeasy.android.interfaces.OnSelectModeListener;
import de.songeasy.android.interfaces.OnOperationListener;
import de.songeasy.android.interfaces.OnToolSelectedListener;
import de.songeasy.android.score.Measure.Direction;
import de.songeasy.android.toolbar.OperationBarMain;
import de.songeasy.android.toolbar.OperationBarMove;
import de.songeasy.android.toolbar.OperationBarSelect;
import de.songeasy.android.toolbar.ToolBarMain;
import de.songeasy.android.toolbar.OperationBarMove;

/**
 * Main tool bar for the score. Shows sub tool bars for selecting more tools.
 * Displays dialogs according to the selected score element. Generates events
 * for selection of tools and actions.
 */
public class BottomBarEdit extends RelativeLayout {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

    private ToolBarMain toolBarMain;
    private OperationBarMain opBarMain;
	private OperationBarMove operationBarMove;
    //private ToolBarMove toolBarMove;
	private OperationBarSelect opBarSelect;


	// listener on various events
	private OnElementDeleteListener deleteElementListener = null;
	private OnElementEditListener editElementListener = null;
	private OnElementSelectNextListener selectNextElementListener = null;
	private OnElementMoveListener moveElementListener;
	private OnSelectModeListener selectModeListener = null;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

    /**
     * Return size of this widget
     *
     * @return size of bottom bar
     */
    public int getSize() {
        // return size of highest element, operationBarMove in this case
        return operationBarMove.getHeight();
    }

	public void setOnElementDeleteListener(OnElementDeleteListener listener) {

		this.deleteElementListener = listener;
	}


	public void setOnElementEditListener(OnElementEditListener listener) {

		this.editElementListener = listener;
	}


	public void setOnElementSelectNextListener( OnElementSelectNextListener listener) {

		this.selectNextElementListener = listener;
	}


	public void setOnElementMoveListener( OnElementMoveListener moveElementListener) {
		this.moveElementListener = moveElementListener;
	}

	public void setOnModeSelectListener(OnSelectModeListener listener) {
			this.selectModeListener = listener;
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);

        switch (visibility) {
            case VISIBLE:
                toolBarMain.show();
                opBarMain.show();
				operationBarMove.show();
				opBarSelect.show();
                break;

            case INVISIBLE:
                toolBarMain.hide();
                opBarMain.hide();
                operationBarMove.hide();
				opBarSelect.hide();
                break;
        }
	}

	// +++++++++++ constructors ++++++++++
	// +++++++++++++++++++++++++++++++++++

	public BottomBarEdit(Context context) {

		this(context, null, 0);
	}


	public BottomBarEdit(Context context, AttributeSet attrs) {

		this(context, attrs, 0);
	}


	/**
	 * Constructor of class BottomBarEdit.
	 * 
	 * @param context
	 *            Activity context
	 * @param attrs
	 *            attributes for RelativeLayout
     * @param defStyle
     *            style definition for RelativeLayout
	 */
	public BottomBarEdit(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

		View.inflate(context, R.layout.lay_bottom_bar_edit, this);

		toolBarMain = (ToolBarMain) findViewById(R.id.toolBarMain1);
        opBarMain = (OperationBarMain) findViewById(R.id.operationBarMain1);
		operationBarMove = (OperationBarMove) findViewById(R.id.operationBarMove1);
        //toolBarMove = (ToolBarMove) findViewById(R.id.toolBarMove1);
		opBarSelect = (OperationBarSelect) findViewById(R.id.operationBarSelect1);

        // opBarMain.show();

        toolBarMain.setOnSelectionListener(new OnToolSelectedListener() {
                                               @Override
                                               public void onToolSelected(int buttonEvent) {
                                                   ToolBarMain.ButtonEvent event = ToolBarMain.ButtonEvent.values()[buttonEvent];
                                                   switch (event) {
                                                       case SELECT:
                                                           if (selectModeListener != null) {
                                                               selectModeListener.onSelectMode(ScoreView.ScoreModes.EDIT_SELECT);
                                                           }
                                                           break;
                                                   }
                                               }
                                           }
        );

        // register for main operations buttons ++++
        opBarMain.setOnOperationListener(new OnOperationListener() {
            @Override
            public void onOperate(int buttonEvent) {
                OperationBarMain.ButtonEvent event = OperationBarMain.ButtonEvent.values()[buttonEvent];

                switch (event) {

                    case EDIT:
                        if (editElementListener != null)
                            editElementListener.onElementEdit();
                        break;

                    case DELETE:
						if (deleteElementListener != null)
							deleteElementListener.onElementDelete();
						break;

					default:
						break;
				}

			}
		});

		// register for selection bar buttons
		opBarSelect.setOnOperationListener(new OnOperationListener() {

            @Override
            public void onOperate(int buttonEvent) {
                OperationBarSelect.ButtonEvent event = OperationBarSelect.ButtonEvent
                        .values()[buttonEvent];

                switch (event) {

                    case NEXT:
                        if (selectNextElementListener != null)
                            selectNextElementListener
                                    .onSelectNext(Direction.RIGHT);
                        break;

                    case PREVIOUS:
                        if (selectNextElementListener != null)
                            selectNextElementListener
                                    .onSelectNext(Direction.LEFT);
                        break;

                    default:
                        break;
                }
            }
        });

		// register for move buttons
        operationBarMove.setOnOperationListener(new OnOperationListener() {
            @Override
            public void onOperate(int buttonEvent) {
                OperationBarMove.ButtonEvent event = OperationBarMove.ButtonEvent.values()[buttonEvent];

                switch (event) {
                    case UP:
                        if (moveElementListener != null)
                            moveElementListener.onMove(Direction.UP);
                        break;

                    case DOWN:
                        if (moveElementListener != null)
                            moveElementListener.onMove(Direction.DOWN);
                        break;

                    case LEFT:
                        if (moveElementListener != null)
                            moveElementListener.onMove(Direction.LEFT);
                        break;

                    case RIGHT:
                        if (moveElementListener != null)
                            moveElementListener.onMove(Direction.RIGHT);
                        break;

                    default:
                        break;

                }

            }
        });


	} // constructor


	public void deselectTool() {
		toolBarMain.deselectTool();
	}

} // class BottomBarEdit
