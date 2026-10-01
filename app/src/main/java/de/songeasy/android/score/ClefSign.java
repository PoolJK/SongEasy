/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Class holding the clef sign of the staff.
 * 
 * @author krein
 * 
 */
public class ClefSign extends ScoreElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** paint style for all ClefSign objects */
	private static Paint paint = new Paint();

	/** available clef names */
	public static enum ClefName {
		VIOLIN, ALT, TENOR, BASS
	};

	/** name of this clef */
	private ClefName clefName;

	/** character sign for this clef */
	private char[] sign;

	/** initialise paint for all objects */
	static {

		ClefSign.paint.setAntiAlias(true);
		ClefSign.paint.setColor(Color.BLACK);
		ClefSign.paint.setTextSize(30);
		ClefSign.paint.setTypeface(MusicFont.musicTypeface);
	}


	// +++++++ getters and setters +++++++

	@Attribute(name = "clefName")
	public ClefName getClef() {
		return clefName;
	}


	@Attribute(name = "clefName")
	public void setClef(ClefName clefName) {
		this.clefName = clefName;

		switch (clefName) {

			case VIOLIN:
				sign = MusicFont.ClefSymbols.VIOLIN.getCode();
				break;

			case ALT:
				sign = MusicFont.ClefSymbols.ALT.getCode();
				break;

			case TENOR:
				sign = MusicFont.ClefSymbols.TENOR.getCode();
				break;

			case BASS:
				sign = MusicFont.ClefSymbols.BASS.getCode();
				break;

			default:
				break;
		}

	}


	/**
	 * Constructor of class ClefSign
	 * 
	 * @param clefName
	 *            name of clef sign
	 */
	public ClefSign(@Attribute(name = "clefName") ClefName clefName) {

		setClef(clefName);

	}


	/**
	 * Copy constructor of class ClefSign
	 * 
	 * @param from
	 *            object to copy from
	 */
	public ClefSign(ClefSign from) {
		this(from.getClef());
	}


	@Override
	public void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		canvas.drawText(
				sign, 0, 1, getOrigin().x, getOrigin().y, ClefSign.paint);

		// drawDebugRect(canvas);
	}


	@Override
	public void drawDebugRect(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		Rect bounds = new Rect();
		ClefSign.paint.getTextBounds(sign, 0, sign.length, bounds);

		Rect area = new Rect(getOrigin().x - bounds.width() / 2, getOrigin().y
				- bounds.height() / 2 - staffLineSpacing / 2, getOrigin().x
				+ bounds.width() / 2, getOrigin().y + bounds.height() / 2
				- staffLineSpacing / 2);
		canvas.drawRect(area, paint);
	}


	/**
	 * Set position of clef sign.
	 * 
	 * @param origin
	 *            origin of staff
	 */
	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// compute room of sign
		ClefSign.paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);
		Rect bounds = new Rect();
		ClefSign.paint.getTextBounds(sign, 0, sign.length, bounds);

		width = bounds.width();
		height = bounds.height();

		// adjust position in staff dependent on type of clef sign
		switch (clefName) {
			case VIOLIN:
				getOrigin().x += width / 2;
				getOrigin().y += (lineSpacing * 5) / 2;
				break;

			case ALT:
				break;

			case TENOR:
				break;

			case BASS:
				getOrigin().x += width / 2;
				getOrigin().y += (lineSpacing * 5) / 2;
				break;

			default:
				break;
		}
		// Log.d("ClefSign", String.format("positioned at: X: %d   Y: %d",
		// this.origin.x, this.origin.y));
	}

} // class
