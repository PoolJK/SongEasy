/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import de.songeasy.android.interfaces.Draggable;

/**
 * Parent class for all textual annotation elements in a score that have no time
 * reference, but are placed relatively inside a measure.
 * 
 * @author krein
 * 
 */
public class AnnotationElement extends ScoreElement implements Draggable,
		Comparable<AnnotationElement> {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** ratio between staff line spacing and text font */
	private float textSizeFactor = 2.0f;

	/** paint of element to draw with */
	protected Paint paint;

	/** height of string below baseline */
	protected float ascent;

	/** height of string above baseline */
	protected float descent;

	/** relative x-position in measure in percent */
	@Attribute(name = "relativePosition")
	protected double relativePosition;

	/** text to be displayed */
	@Attribute(name = "text")
	protected String text;


	// +++++++ getters and setters +++++++

	public double getRelativePosition() {
		return relativePosition;
	}

    /**
     * Set relative x-position inside measure
     * @param relativePosition
     */
	public void setRelativePosition(double relativePosition) {
		this.relativePosition = relativePosition;
	}


	public String getText() {
		return text;
	}


	/**
	 * Set text of element. If text is empty, return immediately.
	 * 
	 * @param text
	 *            text to draw
	 */
	public void setText(String text) {
		if (text.isEmpty() == true)
			return;
		this.text = text;
		computeSize(this.staffLineSpacing);
	}


	/**
	 * Constructor of class AnnotationElement
	 * 
	 * @param text
	 *            Text to be drawn by element
	 * @param relativePosition
	 *            relative position inside measure
	 * @param textSizeFactor
	 *            textsize factor of specific child class
	 */
	public AnnotationElement(
			String text,
			double relativePosition,
			float textSizeFactor) {

		paint = new Paint();

		setText(text);
		setRelativePosition(relativePosition);

		this.textSizeFactor = textSizeFactor;
	}


	@Override
	public void drawSelf(Canvas canvas) {

		canvas.drawText(text, getOrigin().x, getOrigin().y + height - descent,
				paint);

		if (selected == true)
			drawSelectionRect(canvas);

		// drawDebugRect(canvas);
	}


	/*
	 * Override of ScoreElement.setLayout We compute the size of our element
	 * ourselves.
	 * 
	 * @see com.escore.ScoreElement#setLayout(android.graphics.Point,
	 * android.graphics.Paint)
	 */
	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		computeSize(lineSpacing);
	}


	@Override
	public void drawDrag(Canvas canvas, Point position) {

		canvas.drawText(text, position.x, position.y + height, paint);
	}


	/**
	 * Recompute size of element.
	 * 
	 * @param lineSpacing
	 *            spacing between staff lines, used as zoom factor reference
	 */
	public void computeSize(int lineSpacing) {

		paint.setTextSize((float) lineSpacing * textSizeFactor);

		// recompute bounds
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		width = bounds.width();
		// height = bounds.height();

		ascent = -paint.ascent();
		descent = paint.descent();

		height = (int) (ascent + descent);
	}


	@Override
	public int compareTo(AnnotationElement another) {
		if (this.getRelativePosition() < another.getRelativePosition())
			return -1;

		if (this.getRelativePosition() > another.getRelativePosition())
			return 1;

		return 0;
	}
}
