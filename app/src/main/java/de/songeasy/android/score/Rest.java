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
import android.graphics.RectF;

/**
 * Musical rest
 * 
 * @author krein
 * 
 */
public class Rest extends TimedElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** default text size for notes */
	public final static int RESTREFERENCETEXTSIZE = 35;

	/** paint style for all Rest objects */
	private static Paint paint;

	/** y-position of rest signs are predefined in font, we need to compensate */
	// TODO: as this is font specific put offsets in MusicFont class better?
	private int yOffset = 0;

	private int xOffset = 0;

	static {

		Rest.paint = new Paint();
		Rest.paint.setAntiAlias(true);
		Rest.paint.setTypeface(MusicFont.musicTypeface);
		Rest.paint.setColor(Color.BLACK);
		Rest.paint.setTextSize((float) RESTREFERENCETEXTSIZE);
	}


	public Rest(@Attribute(name = "length") NoteLength length,
			@Attribute(name = "timePosition") int timePosition,
			@Attribute(name = "staffNumber") int staffNumber) {

		this.setLength(length);
		this.setTimePosition(timePosition);
		this.setStaffNumber(staffNumber);

	}


	@Override
	public void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		char[] sign = new char[1];

		// TODO insert character code directly with defines
		switch (getLength())
			{
			case FULL:
				canvas.drawText(MusicFont.RestSymbols.FULL.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case HALF:
				canvas.drawText(MusicFont.RestSymbols.HALF.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case QUARTER:
				canvas.drawText(MusicFont.RestSymbols.QUARTER.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case EIGHTH:
				canvas.drawText(MusicFont.RestSymbols.EIGHTH.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case SIXTEENTH:
				canvas.drawText(MusicFont.RestSymbols.SIXTEENTH.getCode(), 0,
						1, getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case THIRTYSECOND:
				canvas.drawText(MusicFont.RestSymbols.THIRTYSECOND.getCode(),
						0, 1, getOrigin().x, getOrigin().y, Rest.paint);
				break;

			case SIXTYFOURTH:
				canvas.drawText(MusicFont.RestSymbols.SIXTYFOURTH.getCode(), 0,
						1, getOrigin().x, getOrigin().y, Rest.paint);
				break;
			}

		// draw augmentation dot
		if (isDotted == true)
			canvas.drawText(MusicFont.NoteSymbols.DOT.getCode(), 0, 1,
					getOrigin().x + width / 2 + width / 4, getOrigin().y,
					Rest.paint);

		// draw selection rectangle
		if (selected == true)
			drawSelectionRect(canvas);
	}


	/**
	 * Set position of rest.
	 * 
	 * Compute room taken by note.
	 * 
	 * @param start
	 *            Origin of note.
	 */
	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		char[] sign = new char[1];

		switch (getLength())
			{
			case FULL:
				sign = MusicFont.RestSymbols.FULL.getCode();
				yOffset = staffLineSpacing / 4 + staffLineSpacing;
				break;

			case HALF:
				sign = MusicFont.RestSymbols.HALF.getCode();
				yOffset = staffLineSpacing / 4 + staffLineSpacing / 2;
				break;

			case QUARTER:
				sign = MusicFont.RestSymbols.QUARTER.getCode();
				break;

			case EIGHTH:
				sign = MusicFont.RestSymbols.EIGHTH.getCode();
				break;

			case SIXTEENTH:
				sign = MusicFont.RestSymbols.SIXTEENTH.getCode();
				break;

			case THIRTYSECOND:
				sign = MusicFont.RestSymbols.THIRTYSECOND.getCode();
				break;

			case SIXTYFOURTH:
				sign = MusicFont.RestSymbols.SIXTYFOURTH.getCode();
				break;
			}

		// adjust y origin of rests to second staff line
		getOrigin().offset(0, (lineSpacing * 5) / 2);

		// adjust size of rest symbol corresponding to space between staff lines
		Rest.paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);

		// compute room of rest
		// TODO add space for possible dot!
		Rect bounds = new Rect();
		Rest.paint.getTextBounds(sign, 0, sign.length, bounds);

		width = bounds.width();
		height = bounds.height();

		if (getLength() == NoteLength.FULL)
			xOffset = width / 2;
		// draw symbol from left, not from middle
		// this.origin.x += width / 2;

	};


	@Override
	public void drawSelectionRect(Canvas canvas) {

		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true);
		paint.setAlpha(PageProperties.SELECT_RECT_ALPHA);

		RectF area = new RectF(
				(float) (getOrigin().x + xOffset - width - PageProperties
						.getDrawselectionextension()),
				(float) (getOrigin().y - yOffset - height / 2 - PageProperties
						.getDrawselectionextension()),
				(float) (getOrigin().x + xOffset + PageProperties
						.getDrawselectionextension()), (float) (getOrigin().y
						- yOffset + height / 2 + PageProperties
						.getDrawselectionextension()));

		canvas.drawRoundRect(area, PageProperties.SELECT_RECT_ROUND_RADIUS,
				PageProperties.SELECT_RECT_ROUND_RADIUS, paint);
	}


	@Override
	public void drawDebugRect(Canvas canvas) {

		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);

		Rect area = new Rect(getOrigin().x + xOffset - width, getOrigin().y
				- yOffset - (height / 2), getOrigin().x + xOffset,
				getOrigin().y - yOffset + (height / 2));

		canvas.drawRect(area, paint);
	}


	@Override
	public boolean isIn(Point p) {

		// define area around element
		Rect area = new Rect(getOrigin().x + xOffset - width
				- SELECTIONEXTENSION, getOrigin().y - yOffset - (height / 2)
				- SELECTIONEXTENSION, getOrigin().x + xOffset
				+ SELECTIONEXTENSION, getOrigin().y - yOffset + (height / 2)
				+ SELECTIONEXTENSION);

		return area.contains(p.x, p.y);
	}
} // class
