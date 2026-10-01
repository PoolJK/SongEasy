/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.songeasy.android.fileactivity.FileType;

/**
 * Compress or extract xml song file.
 * 
 * @author krein/fulong
 * 
 */
public class CompressedSongFile {

	private File fileToHandle;
	private File fileUnzipped;
	// private Context context;
	private static final String tmpZipFilePrefix = "sea_zip";
	private static final String tmpUnzipFilePrefix = "sea_unzip";


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	/**
	 * Get file uncompressed by extractProcedure.
	 * 
	 * @return uncompressed file
	 */
	public File getExtractedFile() {
		return this.fileUnzipped;
	}


	/**
	 * Constructor of class
	 * 
	 * @param file
	 *            File to compress or uncompress. File extension is that of
	 *            compressed song file (.sea).
	 * @param context
	 *            Context of calling activity
	 * 
	 */
	public CompressedSongFile(File file) {
		this.fileToHandle = file;
		// this.context = context;
	}


	/**
	 * Create compressed song file. Renames the original source and writes
	 * compressed file with same name and extension. Deletes the source file
	 * afterwards.
	 * 
	 * @throws IOException
	 * 
	 */
	public void compress() throws IOException {

		if (!fileToHandle.getName().endsWith(
				FileType.COMPRESSED_SONG.getExtension()))
			return;

		// buffer for stream operations
		byte[] readBuffer = new byte[1024];

		// remember source file name
		String orgFileName = fileToHandle.getName();
		// String orgFilePath = fileToHandle.getPath();

		// create new temporary compressed file
		File tmpZipFile = File.createTempFile(tmpZipFilePrefix, null,
				fileToHandle.getParentFile());

		// input stream to read from
		FileInputStream inputStream = new FileInputStream(fileToHandle);

		// output stream to write to
		ZipOutputStream zipOutputStream = new ZipOutputStream(
				new FileOutputStream(tmpZipFile));

		// create new entry in zip for song file
		// strip extension from name (.sea)
		if (orgFileName.contains("."))
			orgFileName = orgFileName
					.substring(0, orgFileName.lastIndexOf('.'));
		// add uncompressed extension to name (.xml)
		orgFileName += FileType.UNCOMPRESSED_SONG.getExtension();
		ZipEntry ze = new ZipEntry(orgFileName);
		zipOutputStream.putNextEntry(ze);

		// write content from source into compressed file
		int count = 0;

		while ((count = inputStream.read(readBuffer)) > 0) {
			zipOutputStream.write(readBuffer, 0, count);
		}

		zipOutputStream.closeEntry();
		zipOutputStream.close();
		inputStream.close();

		// delete source file
		fileToHandle.delete();

		// rename and move temporary file to source file name with correct
		// extension
		// File songFile = new File(orgFilePath);
		tmpZipFile.renameTo(fileToHandle);
	}


	/**
	 * Extract compressed file. Creates new temporarily file for extraction.
	 * Decompress source file contents into it. The temp file has to be deleted
	 * after reading from it.
	 * 
	 * @throws IOException
	 */
	public void extract() throws IOException {

		if (!fileToHandle.getName().endsWith(
				FileType.COMPRESSED_SONG.getExtension()))
			return;

		// buffer for reading in unzipped bytes from stream
		byte[] readBuffer = new byte[1024];

		FileInputStream in = new FileInputStream(fileToHandle);
		ZipInputStream zipIn = new ZipInputStream(in);

		ZipEntry ze = zipIn.getNextEntry();

		if (ze != null) {
			// create new temporary uncompressed file
			File tmpUnzipFile = File.createTempFile(tmpUnzipFilePrefix, null,
					fileToHandle.getParentFile());

			FileOutputStream outStream = new FileOutputStream(tmpUnzipFile);

			int count = 0;

			while ((count = zipIn.read(readBuffer)) > 0) {
				outStream.write(readBuffer, 0, count);
			}

			outStream.close();
			// save file link for reading
			fileUnzipped = tmpUnzipFile;
		}

		zipIn.closeEntry();
		zipIn.close();
		in.close();
	}


	/**
	 * Remove temporarily file with extracted content after reading from it.
	 */
	public void removeTmpFile() {

		fileUnzipped.delete();
		fileUnzipped = null;
	}
}
