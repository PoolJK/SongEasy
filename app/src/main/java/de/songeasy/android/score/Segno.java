/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

/**
 * A segno sign.
 * 
 * @author Joerg Krein
 */
public class Segno extends DirectionElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();


	public Segno(@Attribute(name = "relativePosition") double relativePosition) {
		super(relativePosition, MusicFont.RepeatSymbols.SEGNO.getCode());
		paint.setTypeface(MusicFont.musicTypeface);
	}

}
