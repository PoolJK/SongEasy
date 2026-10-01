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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Time signature of System
 * 
 * @author jansen
 */
public class TimeSignature extends ScoreElement implements Parcelable {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** paint style for all TimeSignature objects */
	private static Paint paint = new Paint();

	// Private declarations
	@Attribute(name = "numerator")
	private int numerator = 4; // Default is 4/4

	@Attribute(name = "denominator")
	private int denominator = 4;

	private char[] text;

	/** static initialization of timesignature's paint */
	static {

		TimeSignature.paint.setAntiAlias(true);
		TimeSignature.paint.setColor(Color.BLACK);
		TimeSignature.paint.setTextSize(30);
		TimeSignature.paint.setTypeface(MusicFont.musicTypeface);
	}


	// +++++++ getters and setters +++++++

	public int getNumerator() {

		return numerator;
	}


	public int getDenominator() {

		return denominator;
	}


	/**
	 * Constructor of class
	 * 
	 * @param numerator
	 *            the numerator of the time signature division
	 * @param denominator
	 *            the numerator of the time signature division
	 */
	public TimeSignature(
			@Attribute(name = "numerator") int numerator,
			@Attribute(name = "denominator") int denominator) {

		this.numerator = numerator;
		this.denominator = denominator;

		text = new char[2];
		text[0] = (char) (numerator + MusicFont.NUMBERS_OFFSET);
		text[1] = (char) (denominator + MusicFont.NUMBERS_OFFSET);
	}


	/**
	 * Copy constructor of class
	 * 
	 * @param from
	 */
	public TimeSignature(TimeSignature from) {

		this(from.getNumerator(), from.getDenominator());
	}


	/**
	 * Constructor for reading from parcel.
	 * 
	 * @param in
	 *            Parcel to read data from
	 */
	public TimeSignature(Parcel in) {

		this(in.readInt(), in.readInt());
	}


	@Override
	public void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		// draw numerator
		canvas.drawText(
				text, 0, 1, getOrigin().x, getOrigin().y, TimeSignature.paint);

		// draw denominator one height below with number spacing
		canvas.drawText(
				text, 1, 1, getOrigin().x, getOrigin().y + height / 2,
				TimeSignature.paint);

		// drawDebugRect(canvas);
	}


	public void drawDebugRect(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);

		Rect bounds = new Rect();
		TimeSignature.paint.getTextBounds(text, 0, 1, bounds);

		Rect area = new Rect(getOrigin().x + width / 2, getOrigin().y - height
				/ 2 - bounds.height() / 4, getOrigin().x + width + width / 2,
				getOrigin().y + height / 2 - bounds.height() / 4);
		canvas.drawRect(area, paint);
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// set text size according to lines spacing
		TimeSignature.paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);

		// compute room of time signature
		Rect bounds = new Rect();

		TimeSignature.paint.getTextBounds(text, 0, 1, bounds);

		// set width to sign width
		width = bounds.width();

		// set height to two times the sign height + spacing
		height = bounds.height() * 2 + bounds.height() / 4;

		// adjust origin to right and middle of staff
		getOrigin().offset(width / 2, lineSpacing / 2);
	}


	/**
	 * Get the note value of measure. One is a whole note. 0.5 means a half
	 * note.
	 * 
	 * @return the relative length value of measure
	 */
	public float getFraction() {

		return (numerator / denominator);
	}


	@Override
	public int describeContents() {

		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(numerator);
		dest.writeInt(denominator);
	}

	public static final Parcelable.Creator<TimeSignature> CREATOR = new Parcelable.Creator<TimeSignature>() {

		public TimeSignature createFromParcel(Parcel in) {

			return new TimeSignature(in);
		}


		public TimeSignature[] newArray(int size) {

			return new TimeSignature[size];
		}
	};

} // class