/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

/**
 * A coda sign
 * 
 * @author Joerg Krein
 * 
 */
public class Coda extends DirectionElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();


	public Coda(@Attribute(name = "relativePosition") double relativePosition) {
		super(relativePosition, MusicFont.RepeatSymbols.CODA.getCode());
		paint.setTypeface(MusicFont.musicTypeface);
	}

}
