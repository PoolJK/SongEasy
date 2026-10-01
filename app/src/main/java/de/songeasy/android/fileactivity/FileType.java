/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.fileactivity;

/** Types of files with their extensions */
public enum FileType {

	UNCOMPRESSED_SONG(".xml"), COMPRESSED_SONG(".sea"), PNG(".png"),
	PDF(".pdf");

	private String extension;


	FileType(String extension) {
		this.extension = new String(extension);
	}


	public String getExtension() {
		return this.extension;
	}
};