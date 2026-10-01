/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Properties for page layout, needed by many classes.
 * 
 * @author krein
 */
public final class PageProperties {

	// string with class name for logging
	private static final String TAG = PageProperties.class.getSimpleName();

	/** paint needed for size computations */
	private static Paint paint = new Paint();

	/** horizontal (time) grid of musical elements */
	public static final int HORIZONTAL_PLACING_GRID = 64;

	/** more coarse horizontal grid of musical element for user interaction */
	public static final int HORIZONTAL_ADDING_GRID = 32;

	/** horizontal margin between score outer frame and measures */
	// TODO: should be percentage of view width
	public static final int HORIZONTALMARGIN = 10;

	/** vertical margin between score outer frame and measures */
	// TODO: should be percentage of view width
	public static final int VERTICALMARGIN = 10;

	/** space between note lines and outer system frame */
	public static final int LINESMARGIN = 5;

	/** distance of aim crosshairs to view borders */
	public static final float CROSSHAIRS_MARGIN = 10.0f;

	/** factor in multiples of staff height for calculation of staff distance */
	public static final int STAVES_DISTANCE_FACTOR = 3;

    /** additional inter system space in multiples of staff line spacing */
    public static final int INTER_SYSTEM_EXTRA_DISTANCE_FACTOR = 2;

	/** minimum distance between staves in pixels */
	public static final int STAVES_MINIMUM_DISTANCE = 40;

	/** minimum number of pixel between staff lines */
	public static final int STAFF_MINIMUM_LINESPACING = 12;

	/** maximum number of pixel between staff lines */
	public static final int STAFF_MAXIMUM_LINESPACING = 40;

	/** standard ratio between width and height of staff */
	public static final int STAFF_WIDTH_TO_HEIGHT_RATIO = 20;

	/** standard ratio between width and line spacing of staff */
	public static final int STAFF_WIDTH_TO_SPACING_RATIO = 90;

	/**
	 * distance of chord from upper staff line in multiples of staff line
	 * spacing
	 */
	public static final float CHORDS_DISTANCE_FROM_STAFF_RATIO = 3.0f;

	/**
	 * distance of label annotation from upper staff line in multiples of staff
	 * line spacing
	 */
	public static final float LABEL_DISTANCE_FROM_STAFF_RATIO = 3.5f;

	/**
	 * distance of direction from upper staff line in multiples of staff line
	 * spacing
	 */
	public static final float DIRECTION_DISTANCE_FROM_STAFF_RATIO = 3.5f;

	/** distance of lyrics from lower staff line in multiples of line spacing */
	public static final float LYRICS_DISTANCE_FROM_STAFF_RATIO = 1.2f;

	/** distance between lyrics */
	public static final float LYRICS_LINE_DISTANCE_RATIO = -0.5f;

	/**
	 * distance of title block from upper edge in multiples of staff line
	 * spacing
	 */
	public static final float TITLEBLOCK_UPPER_MARGIN_TO_STAFF_LINE_SPACING_FACTOR = 2.0f;

	/**
	 * distance of text blocks from lower edge of last measure in multiples of
	 * staff line spacing
	 */
	public static final float TEXTBLOCK_UPPER_MARGIN_TO_STAFF_LINE_SPACING_FACTOR = 4.0f;

	/**
	 * distance of one column of text blocks to another in multiples of staff
	 * line spacing
	 */
	public static final float TEXTBLOCK_SPACING_TO_STAFF_LINE_SPACING_FACTOR = 2.0f;

	/** distance of clef from left staff line */
	public static final int CLEF_DISTANCE_FROM_STAFF = 10;

	/** distance of notes to bar line */
	// TODO >>krein<< verify, make variable?
	public static final int NOTES_OFFSET = 10;

	/** min allowed zoom factor for score */
	public static final float PAGE_MIN_ZOOM = 1.0f;

	/** max allowed zoom factor for score */
	// we get 'font size too large to fit in cache' errors from
	// OpenGLRenderer above this value!
	public static final float PAGE_MAX_ZOOM = 3.4f;

	/** radius of selection rectangle corners */
	public static final float SELECT_RECT_ROUND_RADIUS = 5.0f;

	/** alpha value of selection rectangle, 255 = opaque */
	public static final int SELECT_RECT_ALPHA = 100;

	/** alpha value of selection rectangle, 255 = opaque */
	public static final int SELECT_COLOR = 0xff0000b0;

	/** width of margin in pixels, for drawing of selection frame */
	private final static int DRAWSELECTIONEXTENSION = 3;

	/** distance between staff lines */
	private static int staffLineSpacing;

	/** reference horizontal spacing between musical symbols */
	private static int symbolSpacing;

	/** factor to zoom with */
	private static float zoomFactor;

	/** Initialization of static fields */
	static {
		PageProperties.paint.setTextSize(30);
		PageProperties.paint.setTypeface(MusicFont.musicTypeface);

		PageProperties.staffLineSpacing = PageProperties.STAFF_MINIMUM_LINESPACING;
		PageProperties.zoomFactor = 1.0f;

		Rect bounds = new Rect();
		// compute symbolspacing dependent on the width of a musical sign
		// the cross sign is taken here, but could be other
		PageProperties.paint.getTextBounds(new String(
				MusicFont.AccidentalSymbols.CROSS.getCode()), 0, 1, bounds);
		PageProperties.symbolSpacing = (int) (bounds.width() * 1.5);
	}


    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++

	/**
	 * Get current spacing of staff lines.
	 * 
	 * @return space between staff lines in pixels
	 */
	public static int getStaffLineSpacing() {
		return PageProperties.staffLineSpacing;
	}


	/**
	 * Get horizontal spacing between symbols
	 * 
	 * @return the symbolSpacing
	 */
	public static int getSymbolSpacing() {
		return PageProperties.symbolSpacing;
	}

	/**
	 * Get the zoom factor the song currently is displayed with.
	 * 
	 * @return the zoom factor
	 */
	public static float getZoomFactor() {
		return PageProperties.zoomFactor;
	}


	/**
	 * Get drawing extension of selection rectangle.
	 * 
	 * @return drawing extension in pixel
	 */
	public static int getDrawselectionextension() {
		return PageProperties.DRAWSELECTIONEXTENSION;
	}


	/**
	 * Set the zoom factor the song should be displayed with.
	 * 
	 * @param zoomFactor
	 *            the zoom factor to be used
	 */
	public static void setZoomFactor(float zoomFactor) {
		PageProperties.zoomFactor = zoomFactor;
	}


	/**
	 * Parameterless constructor of class.
	 */
	// public PageProperties() {
	//
	// }

	/**
	 * Calculate staff line spacing, dependent on available width for staff.
	 * Sets static height and width of Staff class. Takes zoom factor in
	 * account.
	 * 
	 * @param availableWidth
	 *            width of score sheet
	 */
	public static void calculateStaffLineSpacing(int availableWidth) {

		// calculate staff height by using standard width to height ratio
		// needs a change width by 80 to change linespacing by one!
		int staffHeight = Math
				.round(((float) availableWidth / PageProperties.STAFF_WIDTH_TO_HEIGHT_RATIO));

		staffLineSpacing = Math
				.round(staffHeight / (Staff.NUMBER_OF_LINES - 1));

		// set static variables for all staves
		Staff.setStavesHeight(staffLineSpacing * (Staff.NUMBER_OF_LINES - 1));
		Staff.setStavesWidth(availableWidth - (2 * LINESMARGIN));

		Staff.setLineSpacing(staffLineSpacing);

		if (Staff.getStavesWidth() < 0 || Staff.getStavesHeight() < 0
				|| staffLineSpacing < 0) {
			Log.d(TAG, "negative value!");
		}

		// Log.d(TAG, String.format(
		// "calculated staff -width -height -spacing: %d  %d %d",
		// Staff.getStavesWidth(), Staff.getStavesHeight(),
		// staffLineSpacing));
	}
} // class
