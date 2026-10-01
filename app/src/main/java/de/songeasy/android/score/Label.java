/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Text for structuring song into parts.
 * 
 * @author krein
 * 
 */
// TODO: maybe better child of MusicXML's Directive class
public class Label extends AnnotationElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** ratio of staff line spacing and part font */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.8f;


	// +++++++ getters and setters +++++++

	public Label(@Attribute(name = "text") String text, @Attribute(
			name = "relativePosition") double relativePosition) {

		super(text, relativePosition, TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR);

		// set paint for label
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextSize(25);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

	}
}
