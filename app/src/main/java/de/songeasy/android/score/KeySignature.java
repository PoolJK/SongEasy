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
 * The Key Signature of a NoteSystem. Draws the accidentals for the key, b-signs
 * or cross-signs.
 * 
 * @author krein
 * 
 */

public class KeySignature extends ScoreElement implements Parcelable {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** paint style for all KeySignature objects */
	private static Paint paint = new Paint();

	/**
	 * applicable keys for the staff, the circle of fifths, parallel minor keys
	 * are not listed
	 */
	public enum KeyName {

		AMAJOR, AMINOR, ASHARPMINOR, AFLATMAJOR, AFLATMINOR, BMAJOR, BMINOR,
		BFLATMAJOR, BFLATMINOR, CMAJOR, CMINOR, CSHARPMAJOR, CSHARPMINOR,
		CFLATMAJOR, DMAJOR, DMINOR, DSHARPMINOR, DFLATMAJOR, EMAJOR, EMINOR,
		EFLATMAJOR, EFLATMINOR, FMAJOR, FMINOR, FSHARPMAJOR, FSHARPMINOR,
		GMAJOR, GMINOR, GSHARPMINOR, GFLATMAJOR
	};

	@Attribute(name = "key")
	private KeyName key;

	static {

		KeySignature.paint.setAntiAlias(true);
		KeySignature.paint.setColor(Color.BLACK);
		KeySignature.paint.setTextSize(30);
		KeySignature.paint.setTypeface(MusicFont.musicTypeface);
	}


	public KeyName getKeyName() {
		return key;
	}


	/**
	 * Constructor of KeySignature
	 * 
	 * @param key
	 *            Identifier of key
	 */
	public KeySignature(@Attribute(name = "key") KeyName key) {

		this.key = key;
	}


	/**
	 * Copy constructor of class KeySignature
	 * 
	 * @param from
	 *            object to copy from
	 */
	public KeySignature(KeySignature from) {
		this(from.getKeyName());
	}


	/**
	 * Constructor for reading from parcel.
	 * 
	 * @param in
	 *            Parcel to read data from
	 */
	private KeySignature(Parcel in) {
		this(KeyName.values()[in.readInt()]);
	}


	/**
	 * Draw KeySignature with its elements
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 */
	@Override
	public void drawSelf(Canvas canvas) {

		// reusable coordinates of sign to draw
		int yPos;
		int xPos;

		if (visible != true)
			return;

		// compute room of cross sign, assuming b sign is the same
		Rect signsRoom = new Rect();
		KeySignature.paint.getTextBounds(
				MusicFont.AccidentalSymbols.CROSS.getCode(), 0,
				MusicFont.AccidentalSymbols.CROSS.getCode().length, signsRoom);

		// offset from left border to draw key-signs
		// TODO: >>Norman<< offset should be calculated dynamically
		// TODO: >>Norman<< respect clef-sign and clef-sign y-position
		switch (key) {

		// 7 b
			case CFLATMAJOR:
			case AFLATMINOR:
				xPos = getOrigin().x + 6 * signsRoom.width();
				yPos = getOrigin().y + (staffLineSpacing / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 6 b
			case GFLATMAJOR:
			case EFLATMINOR:
				xPos = getOrigin().x + (5 * signsRoom.width());
				yPos = getOrigin().y - ((staffLineSpacing * 3) / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 5 b
			case DFLATMAJOR:
			case BFLATMINOR:
				xPos = getOrigin().x + (4 * signsRoom.width());
				yPos = getOrigin().y;
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 4 b
			case AFLATMAJOR:
			case FMINOR:
				xPos = getOrigin().x + (3 * signsRoom.width());
				yPos = getOrigin().y - (2 * staffLineSpacing);
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 3 b
			case EFLATMAJOR:
			case CMINOR:
				xPos = getOrigin().x + (2 * signsRoom.width());
				yPos = getOrigin().y - (staffLineSpacing / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 2 b
			case BFLATMAJOR:
			case GMINOR:
				xPos = getOrigin().x + signsRoom.width();
				yPos = getOrigin().y - ((staffLineSpacing * 5) / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next b

				// 1 b
			case FMAJOR:
			case DMINOR:
				xPos = getOrigin().x;
				yPos = getOrigin().y - staffLineSpacing;
				canvas.drawText(
						MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				break;

			// no signature
			case CMAJOR:
			case AMINOR:
				// draw nothing
				break;

			// 7 #
			case CSHARPMAJOR:
			case ASHARPMINOR:
				xPos = getOrigin().x + (signsRoom.width() * 6);
				yPos = getOrigin().y - staffLineSpacing;
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 6 #
			case FSHARPMAJOR:
			case DSHARPMINOR:
				xPos = getOrigin().x + (signsRoom.width() * 5);
				yPos = getOrigin().y - ((staffLineSpacing * 5) / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 5 #
			case BMAJOR:
			case GSHARPMINOR:
				xPos = getOrigin().x + (signsRoom.width() * 4);
				yPos = getOrigin().y - (staffLineSpacing / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 4 #
			case EMAJOR:
			case CSHARPMINOR:
				xPos = getOrigin().x + (signsRoom.width() * 3);
				yPos = getOrigin().y - (staffLineSpacing * 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 3 #
			case AMAJOR:
			case FSHARPMINOR:
				xPos = getOrigin().x + (signsRoom.width() * 2);
				yPos = getOrigin().y - ((staffLineSpacing * 7) / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 2 #
			case DMAJOR:
			case BMINOR:
				xPos = getOrigin().x + signsRoom.width();
				yPos = getOrigin().y - ((staffLineSpacing * 3) / 2);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				// fall through to next #

				// 1 #
			case GMAJOR:
			case EMINOR:
				xPos = getOrigin().x;
				yPos = getOrigin().y - (staffLineSpacing * 3);
				canvas.drawText(
						MusicFont.AccidentalSymbols.CROSS.getCode(), 0, 1,
						xPos, yPos, KeySignature.paint);
				break;

			default:
				break;
		}

		// drawDebugRect(canvas);
	}


	@Override
	public void drawDebugRect(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		Rect area = new Rect(getOrigin().x, getOrigin().y - height,
				getOrigin().x + width, getOrigin().y);
		canvas.drawRect(area, paint);
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		KeySignature.paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);

		// compute room of cross sign, assuming b sign is the same
		Rect bounds = new Rect();
		KeySignature.paint.getTextBounds(
				MusicFont.AccidentalSymbols.CROSS.getCode(), 0,
				MusicFont.AccidentalSymbols.CROSS.getCode().length, bounds);

		switch (key) {

		// 7 symbols
			case CSHARPMAJOR:
			case CFLATMAJOR:
			case AFLATMINOR:
			case ASHARPMINOR:
				width = bounds.width() * 7;
				height = 3 * staffLineSpacing + bounds.height();
				break;

			// 6 symbols
			case FSHARPMAJOR:
			case GFLATMAJOR:
			case EFLATMINOR:
			case DSHARPMINOR:
				width = bounds.width() * 6;
				height = 3 * staffLineSpacing + bounds.height();
				break;

			// 5 symbols
			case BMAJOR:
			case DFLATMAJOR:
			case BFLATMINOR:
			case GSHARPMINOR:
				width = bounds.width() * 5;
				height = 3 * staffLineSpacing + bounds.height();
				break;

			// 4 symbols
			case EMAJOR:
			case AFLATMAJOR:
			case CSHARPMINOR:
			case FMINOR:
				width = bounds.width() * 4;
				height = 5 * staffLineSpacing / 2 + bounds.height();
				break;

			// 3 symbols
			case AMAJOR:
			case EFLATMAJOR:
			case FSHARPMINOR:
			case CMINOR:
				width = bounds.width() * 3;
				height = 5 * staffLineSpacing / 2 + bounds.height();
				break;

			// 2 symbols
			case DMAJOR:
			case BFLATMAJOR:
			case BMINOR:
			case GMINOR:
				width = bounds.width() * 2;
				height = 2 * staffLineSpacing + bounds.height();
				break;

			// 1 symbol
			case GMAJOR:
			case FMAJOR:
			case EMINOR:
			case DMINOR:
				width = bounds.width();
				height = bounds.height();
				break;

			// no symbols
			case CMAJOR:
			case AMINOR:
				// there is no key signature for C / Am
				width = 0;
				height = 0;
				break;

			default:
				break;
		}

	}


	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(key.ordinal());
	}

	public static final Parcelable.Creator<KeySignature> CREATOR = new Parcelable.Creator<KeySignature>() {
		public KeySignature createFromParcel(Parcel in) {
			return new KeySignature(in);
		}


		public KeySignature[] newArray(int size) {
			return new KeySignature[size];
		}
	};

} // class
