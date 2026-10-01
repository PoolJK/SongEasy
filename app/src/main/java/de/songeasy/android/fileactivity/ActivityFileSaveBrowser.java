/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.fileactivity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import de.songeasy.android.R;

/**
 * A file browser for saving files. Lets the user create directory an name of
 * file to save.
 * 
 * @author krein
 * 
 */
public class ActivityFileSaveBrowser extends ActivityFileBrowser {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** modes of saving, save or export */
	public static enum SaveMode {

		/** save file */
		SAVE,
		/** export file */
		EXPORT,
	};

	/** name of the song, suggested as filename */
	public static final String KEY_MODE = "Mode";

	/** name of the song, suggested as filename */
	public static final String KEY_SONG_NAME = "SongName";

	// ids for inner dialogs
	private static final int DIALOG_CREATE_FOLDER_ID = 0;
	private static final int DIALOG_ENTER_FILENAME_ID = 1;

	/** existing name of song, suggestion for filename to save */
	private String songName;

	/** mode of save, needed for correct naming of menu entries */
	private SaveMode mode = SaveMode.SAVE;


	/**
	 * Inner Dialog class. Let the user enter name of file to save. Will be
	 * shown, when the save file menu is clicked
	 * 
	 * @return
	 */
	private Dialog enterFileNameDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		View content = getLayoutInflater().inflate(R.layout.lay_dlg_file_save,
				(ViewGroup) findViewById(R.id.layout_root));
		builder.setTitle(R.string.title_save_file_select);
		EditText textir = (EditText) content.findViewById(R.id.typeName);
		textir.setText(songName);
		builder.setView(content);

		builder.setPositiveButton(getResources().getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						Dialog source = (Dialog) dialog;
						EditText nameField = (EditText) source
								.findViewById(R.id.typeName);
						String name = nameField.getText().toString();
						String fileName = name + fileType.getExtension();

						// a file has been selected, store name with directory
						// into result
						selectedFile = new File(selectedFolder, fileName);

						// return from this activity and save filname in result
						final Intent intent = new Intent();
						intent.putExtra(KEY_SELECTED_FILE, selectedFile);

						intent.putExtra(KEY_FILE_TYPE, fileType);

						if (getParent() == null) {
							setResult(Activity.RESULT_OK, intent);
						} else {
							getParent().setResult(Activity.RESULT_OK, intent);
						}

						// end activity
						finish();
					}

				});

		builder.setNegativeButton(
				getResources().getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		return builder.create();
	}


	/**
	 * Inner dialog class. Let the user enter name of new directory to be
	 * created. Will be shown,when create folder menu button is clicked.
	 * 
	 * @return
	 */
	private Dialog createFolderDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_save_folder_create);

		View content = getLayoutInflater().inflate(
				R.layout.lay_dlg_create_folder,
				(ViewGroup) findViewById(R.id.layout_root));
		builder.setView(content);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Dialog source = (Dialog) dialog;
				EditText nameField = (EditText) source
						.findViewById(R.id.typeName);
				String name = nameField.getText().toString();

				File folder = new File(selectedFolder.getAbsolutePath() + "/"
						+ name);

				if (!folder.exists()) {
					folder.mkdirs();
					fillListWithFileNames();
				}
				// dialog.dismiss();
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		return builder.create();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		// get variables from saved state if device was rotated etc.
		if (savedInstanceState != null) {

			songName = savedInstanceState.getString(KEY_SONG_NAME);
		}
		// get variables from bundle if called from other activity
		else {

			Bundle b = getIntent().getExtras();
			// get song name from bundle needed for save
			if (b.containsKey(KEY_SONG_NAME))
				songName = b.getString(KEY_SONG_NAME);
			else
				songName = "";

			// get mode of browsing
			if (b.containsKey(KEY_MODE))
				mode = (SaveMode) b.getSerializable(KEY_MODE);
			else
				mode = SaveMode.SAVE;

		}

		super.onCreate(savedInstanceState);
	}


	@Override
	protected void onFileSelected() {
		// show saveFileDialog with selected filename without extension
		songName = selectedFile.getName();

		// strip extension from name
		if (songName.contains("."))
			songName = songName.substring(0, songName.lastIndexOf('.'));

		enterFileNameDialog().show();
	}


	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id)
			{

			case DIALOG_CREATE_FOLDER_ID:
				return createFolderDialog();

			case DIALOG_ENTER_FILENAME_ID:
				return enterFileNameDialog();
			}
		return null;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		switch (mode)
			{
			case SAVE:
				getMenuInflater().inflate(R.menu.options_menu_save, menu);
				break;

			case EXPORT:
				getMenuInflater().inflate(R.menu.options_menu_export, menu);
				break;

			default:
				getMenuInflater().inflate(R.menu.options_menu_save, menu);
				break;
			}

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
			{

			case R.id.opt_save_create_folder:
				showDialog(DIALOG_CREATE_FOLDER_ID);
				break;

			case R.id.opt_save_file:
				showDialog(DIALOG_ENTER_FILENAME_ID);
				break;
			}

		return false;
	}


	public void onSaveInstanceState(Bundle savedInstanceState) {

		savedInstanceState.putString(KEY_SONG_NAME, songName);
		savedInstanceState.putSerializable(KEY_MODE, mode);

		super.onSaveInstanceState(savedInstanceState);
	}

} // class

