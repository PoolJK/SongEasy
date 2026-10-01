/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Text to sing, located under NoteSystem
 * 
 * @author krein
 * 
 */
public class Lyric extends AnnotationElement {

	/** ratio between staff line spacing and lyrics font for all lyrics */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.0f;
	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();
    /** the number of the staff this lyric is placed under */
	@Attribute(name = "staffNumber")
	private int staffNumber;

	/** the line number of this lyric, 0 is for first line */
	@Attribute(name = "lineNumber")
	private int lineNumber;


	// +++++++ getters and setters +++++++

    public int getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(int staffNumber) {
        this.staffNumber = staffNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

	/** static initialization of lyric's paint */
	// static {
	//
	// }

	public Lyric(@Attribute(name = "text") String text, @Attribute(
			name = "relativePosition") double relativePosition, @Attribute(
			name = "staffNumber") int staffNumber, @Attribute(
			name = "lineNumber") int lineNumber) {

		super(text, relativePosition, TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR);

		this.staffNumber = staffNumber;
		this.lineNumber = lineNumber;

		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setTypeface(Typeface.SERIF);
		paint.setTextSize(20);
	}

}
