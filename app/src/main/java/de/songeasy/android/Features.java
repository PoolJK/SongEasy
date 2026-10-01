/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android;

/**
 * State of plannded features in this project.
 * Features that are worked on or should not be enabled by default are defined as not enabled here.
 * Checking of state has to be done in the relevant code sections.
 * 
 * @author krein
 * 
 */
/**
 * Enabled or disabled features.
 * 
 * @author krein
 * 
 */
public enum Features {

	/** playing song */
	PLAY(false),
	/** export song as pdf */
	PDF_EXPORT(true),
	/** sync position and version via WLAN */
	WIRELESS_SYNC(false),
	/** export song as MusicXML */
	MUSICXML_EXPORT(false),
	/** transpose song */
	TRANSPOSE(false),
	/** optical character recognition for musical symbols */
	OCR(false);

	private boolean state;


	Features(boolean state) {
		this.state = state;
	}


	public boolean isEnabled() {
		return this.state;
	}
};
