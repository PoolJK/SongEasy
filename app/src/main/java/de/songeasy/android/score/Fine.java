/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * The Fine directive, marking the end of a song. As there is no fine Symbol,
 * it's untypical for a DirectionElement and needs to text instead.
 * 
 * @author Joerg Krein
 * 
 */
public class Fine extends DirectionElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** ratio of staff line spacing and chord font */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.4f;

	private static String text = new String("Fine");

	/** height of string below baseline */
	private float ascent;

	/** height of string above baseline */
	private float descent;


	public Fine(@Attribute(name = "relativePosition") double relativePosition) {
		super(relativePosition, text.toCharArray());
	}


	@Override
	public void drawSelf(Canvas canvas) {
		canvas.drawText(
				text, getOrigin().x, getOrigin().y + height - descent, paint);

		if (selected == true)
			drawSelectionRect(canvas);
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {
		super.setLayout(origin, lineSpacing);

		paint.setTextSize((float) lineSpacing
				* TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR);
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

		computeSize();
	}


	/**
	 * Recompute size of element.
	 */
	private void computeSize() {

		// recompute bounds
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		width = bounds.width();
		// height = bounds.height();

		ascent = -paint.ascent();
		descent = paint.descent();

		height = (int) (ascent + descent);
	}

}
