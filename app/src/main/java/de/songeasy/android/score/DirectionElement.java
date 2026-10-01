/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import de.songeasy.android.interfaces.Draggable;

/**
 * ScoreElement for showing musical directives, as repeats, coda, segno, etc.
 * 
 * @author Joerg Krein
 * 
 */
public class DirectionElement extends ScoreElement implements Draggable,
		Comparable<DirectionElement> {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** paint style, changeable for child classes */
	protected Paint paint = new Paint();

	/** relative x-position in measure in percent */
	@Attribute(name = "relativePosition")
	protected double relativePosition;

	/** text to be displayed */
	protected char[] text;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	public double getRelativePosition() {
		return relativePosition;
	}


	public void setRelativePosition(double relativePosition) {
		this.relativePosition = relativePosition;
	}


	/**
	 * Constructor of class DirectionElement
	 * 
	 * @param relativePosition
	 *            relative position of element inside measure
	 * @param text
	 *            text to be drawn by element
	 */
	public DirectionElement(
			@Attribute(name = "relativePosition") double relativePosition,
			char[] text) {

		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		this.relativePosition = relativePosition;
		this.text = text;
	}


	@Override
	public void drawSelf(Canvas canvas) {

		canvas.drawText(text, 0, 1, getOrigin().x + width / 2, getOrigin().y
				+ height / 2, paint);

		if (isSelected() == true)
			drawSelectionRect(canvas);
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// compute room of sign
		paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, 1, bounds);

		width = bounds.width();
		height = bounds.height();
	}


	@Override
	public void drawDrag(Canvas canvas, Point position) {
		canvas.drawText(text, 0, 1, position.x + width / 2, position.y + height
				/ 2, paint);
	}


	@Override
	public int compareTo(DirectionElement another) {
		if (this.getRelativePosition() < another.getRelativePosition())
			return -1;

		if (this.getRelativePosition() > another.getRelativePosition())
			return 1;

		return 0;
	}

} // class
