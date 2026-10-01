/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.fileactivity;

/**
 * Names and numbering of possible song file versions.
 * 
 * @author krein
 * 
 */
public enum FileVersion {

	/** pre version for developing */
	V004("0.0.4"),

	/** first version available in Play Store */
	V100("1.0.0"),

	/**
	 * removed FIRST_IN_LINE and LAST_IN_LINE attributes, using LINE_BREAK
	 * instead
	 */
	V101("1.0.1");

	private String versionString;


	FileVersion(String version) {
		this.versionString = new String(version);
	}


	/**
	 * Get version String of file version.
	 * 
	 * @return
	 */
	public String getVersionString() {
		return this.versionString;
	}


	/**
	 * Check if string matches the version string defined for this version.
	 * 
	 * @return true if version string matches, false else
	 */
	public boolean isVersion(String versionString) {

		if (this.versionString.equals(versionString))
			return true;
		else
			return false;

	}
};