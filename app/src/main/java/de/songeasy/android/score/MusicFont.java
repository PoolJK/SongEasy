/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Character Code defines for this font. As we need to deliver a char array to
 * Android's draw functions the enums are a little bit more sophisticated.
 * 
 * @author krein
 * 
 */
public final class MusicFont {

	// string with class name for logging
	private final static String TAG = "MusicFont";

	public static Typeface musicTypeface;

	/** the begin of the numbers section in this font */
	public static final int NUMBERS_OFFSET = 48;

	/** ratio of staff line spacing and music symbol size */
	public static final float SYMBOL_TO_STAFF_LINE_SPACING_FACTOR = 4.3f;

	/**
	 * note stem needs to be positioned a little closer to note head than just
	 * half of the heads width
	 */
	public static final float NOTE_STEM_X_OFFSET_CORRECTION = 0.9f;


	/**
	 * @return the musicTypeface
	 */
	public static Typeface getMusicTypeface() {
		return musicTypeface;
	}


	/** private constructor of class, we want only one object */
	private MusicFont() {

	}


	/**
	 * Load font we defined all the character positions for.
	 * 
	 * @param context
	 */
	public static void LoadFont(Context context) {

		musicTypeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/songeasyfont.otf");
	}

	/** character codes for different note symbols, flag, etc. */
	public static enum NoteSymbols {
		FULL((char) 71), HALF((char) 72), QUARTER((char) 69), UPFLAG((char) 70), DOWNFLAG(
				(char) 73), DOT((char) 84);

		private char symbolCode[];


		NoteSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

	/** character codes for rest symbols */
	public static enum RestSymbols {
		FULL((char) 74), HALF((char) 75), QUARTER((char) 76), EIGHTH((char) 77), SIXTEENTH(
				(char) 78), THIRTYSECOND((char) 79), SIXTYFOURTH((char) 80);

		private char symbolCode[];


		RestSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

	/** symbols for defining the octave */
	public static enum ClefSymbols {
		VIOLIN((char) 67), ALT((char) 66), TENOR((char) 66), BASS((char) 68);

		private char symbolCode[];


		ClefSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

	/** symbols for defining the key signature */
	public static enum AccidentalSymbols {
		CROSS((char) 64), BSIGN((char) 81), NATURAL((char) 59);

		private char symbolCode[];


		AccidentalSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

	/** symbols for repeat marks */
	public static enum RepeatSymbols {
		SEGNO((char) 113), CODA((char) 112);

		private char symbolCode[];


		RepeatSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

	/** symbols for special tasks */
	public static enum SpecialSymbols {
		DOUBLEBSIGN((char) 94), PAUSE((char) 92), SPACE((char) 96);

		private char symbolCode[];


		SpecialSymbols(char symbol) {
			this.symbolCode = new char[1];
			this.symbolCode[0] = symbol;
		}


		public char[] getCode() {
			return this.symbolCode;
		}
	};

} // class
