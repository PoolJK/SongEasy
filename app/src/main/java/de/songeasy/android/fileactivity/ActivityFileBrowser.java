/*
 * Copyright (C) 2014 Joerg Krein
 */

package de.songeasy.android.fileactivity;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.songeasy.android.R;

/**
 * Activity for browsing the file system for files of a defined type. Base class
 * for load and save browser. Child classes need to override onFileSelected()
 * 
 * @author krein
 */
public abstract class ActivityFileBrowser extends ListActivity {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private final String stringDirUp = new String("..");

	/** key for saving file type enum in bundle */
	public static final String KEY_FILE_TYPE = "FileType";

	/** key for saving file name string in bundle */
	public static final String KEY_SELECTED_FILE = "SelectedFile";

	/** key for providing directory to use in bundle */
	public static final String KEY_SELECTED_FOLDER = "SelectedFolder";

	/** List with only the names of listed files */
	private ArrayList<String> itemList = new ArrayList<String>();

	/** List with paths to listed files */
	private ArrayList<String> pathList = new ArrayList<String>();

	/** directory selected by user */
	protected File selectedFolder;

	/** directory selected by user */
	protected File selectedFile;

	/** type of file to use */
	protected FileType fileType = FileType.COMPRESSED_SONG;

	/**
	 * ArrayAdapter for holding names of files and directories Shows different
	 * icons for files and directories
	 */
	class FileAdapter extends ArrayAdapter<String> {

		public FileAdapter() {

			super(ActivityFileBrowser.this, R.layout.lay_explorer_row, itemList);
		}


		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.lay_explorer_row, parent,
					false);

			TextView label = (TextView) row.findViewById(R.id.rowText);
			ImageView icon = (ImageView) row.findViewById(R.id.rowIcon);

			// extra handling for first entry, this is for navigate one level up
			if (itemList.get(position).equals(stringDirUp)) {
				icon.setImageResource(R.drawable.ic_navigation_higher);
				label.setText(itemList.get(position));
				return (row);
			}

			label.setText(itemList.get(position));

			File path = new File(pathList.get(position));

			if (path.isDirectory()) {

				icon.setImageResource(R.drawable.ic_explorer_row_folder);
			} else {

				switch (fileType)
					{
					case UNCOMPRESSED_SONG:
						icon.setImageResource(R.drawable.ic_explorer_row_music_file);
						break;

					case PNG:
						icon.setImageResource(R.drawable.ic_explorer_row_png_file);
						break;

					case PDF:
						icon.setImageResource(R.drawable.ic_explorer_row_pdf_file);
						break;

					default:
						icon.setImageResource(R.drawable.ic_explorer_row_music_file);
						break;
					}
			}

			return (row);
		}
	}


	/**
	 * Expect folder to start browsing and type of file (extension) in bundle.
	 * Starts searching in standard path otherwise.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_activity_file_browser);

		// get variables from saved state if device was rotated etc.
		if (savedInstanceState != null) {

			fileType = (FileType) savedInstanceState
					.getSerializable(KEY_FILE_TYPE);

			selectedFolder = (File) savedInstanceState
					.getSerializable(KEY_SELECTED_FOLDER);

			// make sure selectedFolder is a directory
			if (!selectedFolder.isDirectory()) {
				selectedFolder = selectedFolder.getParentFile();
				if (selectedFolder == null)
					selectedFolder = getFilesDir();
			}

		}
		// get variables from bundle if called from other activity
		else {

			Bundle b = getIntent().getExtras();

			// get file type from bundle
			if (b.containsKey(KEY_FILE_TYPE)) {
				fileType = (FileType) b.getSerializable(KEY_FILE_TYPE);
			}

			if (b.containsKey(KEY_SELECTED_FOLDER)) {
				// get storage location from bundle
				selectedFolder = (File) b.getSerializable(KEY_SELECTED_FOLDER);

				// make sure selectedFile is a directory
				if (!selectedFolder.isDirectory()) {
					selectedFolder = selectedFolder.getParentFile();
					if (selectedFolder == null)
						selectedFolder = getFilesDir();
				}

			} else {
				// use standard files location
				selectedFolder = getFilesDir();
			}
		}

		fillListWithFileNames();
	}


	/**
	 * Called if user selected a file in list. Needs to be implemented by child
	 * class.
	 */
	protected abstract void onFileSelected();


	/**
	 * Sets the list adapter with filenames and paths found in given path.
	 * 
	 * @param currentFile
	 *            File with current path to scan for files
	 */
	public void fillListWithFileNames() {

		// show current directory in actionbar
		getActionBar().setSubtitle(selectedFolder.getAbsolutePath());

		itemList.clear();
		pathList.clear();

		// first entry is always for navigate level up
		if (selectedFolder.getParent() != null) {
			itemList.add(stringDirUp);
			pathList.add(selectedFolder.getParent());
		}

		// load files list with files in this directory
		File[] files = selectedFolder.listFiles();

		// get list of files if directory is not empty
		if (files != null) {
			for (int i = 0; i < files.length; i++) {

				// overread hidden files
				if (files[i].isHidden())
					continue;

				// only directories which can be accessed or files with selected
				// type will be stored in path list and item list
				if ((files[i].isDirectory() && files[i].canRead())
						|| files[i].getPath().endsWith(fileType.getExtension())) {
					pathList.add(files[i].getPath());
					itemList.add(files[i].getName());
				}
			}
		}
		// update listview
		setListAdapter(new FileAdapter());
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		selectedFile = new File(pathList.get(position));

		if (selectedFile.isDirectory()) {

			selectedFolder = selectedFile;

			// user selected directory, fill list with it's content
			fillListWithFileNames();

		} else {

			// file selected, call action handler
			onFileSelected();
		}
	}


	public void onSaveInstanceState(Bundle savedInstanceState) {

		savedInstanceState.putSerializable(KEY_FILE_TYPE, fileType);
		savedInstanceState.putSerializable(KEY_SELECTED_FOLDER, selectedFolder);

		super.onSaveInstanceState(savedInstanceState);
	}

}
