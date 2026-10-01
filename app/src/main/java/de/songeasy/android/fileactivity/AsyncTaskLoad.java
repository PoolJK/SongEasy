/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.fileactivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.VisitorStrategy;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import de.songeasy.android.R;
import de.songeasy.android.ScoreView;
import de.songeasy.android.interfaces.OnXmlNextMeasureListener;
import de.songeasy.android.interfaces.OnXmlNumberOfMeasuresListener;
import de.songeasy.android.score.Song;
import de.songeasy.android.xml.CompressedSongFile;
import de.songeasy.android.xml.XMLVersionConvert;
import de.songeasy.android.xml.XMLVisitor;

/**
 * AsyncTask for loading song. Displays a progress bar while saving.
 */
public class AsyncTaskLoad extends AsyncTask<Void, Integer, Integer> {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private static final int RESULT_OK = 0;
	private static final int RESULT__FILE_NOT_FOUND = 1;
	private static final int RESULT_FILE_NOT_READABLE = 2;
	private static final int INCRIMENTVALUE = 1;
	private static final int DIALOG_ERROR = 5;

	private ProgressDialog progressBar;
	private Context context;
	private Song songRead;
	private File fileToLoad;
	private String fileName;

	private XMLVisitor xmlVisitor;


	// TODO: why give existing song as parameter?
	public AsyncTaskLoad(Context context, Song song, File file) {

		this.context = context;
		this.songRead = song;
		this.fileToLoad = file;
		this.fileName = fileToLoad.getName();
		xmlVisitor = new XMLVisitor();
		xmlVisitor.setOnNextMeasureListener(new OnXmlNextMeasureListener() {

			@Override
			public void onNextMeasure() {
				publishProgress(INCRIMENTVALUE);

			}
		});

		xmlVisitor
				.setOnNumberOfMeasuresListener(new OnXmlNumberOfMeasuresListener() {

					@Override
					public void onNumberOfMeasures(int numberOfMeasures) {
						progressBar.setMax(numberOfMeasures);

					}
				});

	}


	@Override
	protected void onPreExecute() {

		progressBar = new ProgressDialog(context);
		progressBar.setTitle(context.getResources().getString(R.string.prg_bar_loading_title));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setCancelable(true);
		progressBar.setProgressNumberFormat(null);
		progressBar.setProgressPercentFormat(null);
		progressBar.setProgress(0);
		progressBar.setMax(20);
		progressBar.show();

		// settings = getSharedPreferences(PREFS_NAME, 0);
		// load = settings.getString("loadFile", "");

	}


	@Override
	protected void onPostExecute(Integer result) {

		switch (result)
			{
			case RESULT_OK:
				songRead.updateNumberOfMeasures();
				songRead.updatePageBreaks();
				ScoreView scoreView = (ScoreView) ((Activity) context)
						.findViewById(R.id.scoreViewMain);
				scoreView.setSong(songRead);
				// reset view
				scoreView.setZoom(1.f);
				scoreView.scrollTo(0, 0);
				scoreView.setCurrentPage(0);
				// set song name in actionbar
				ActionBar bar = ((Activity) context).getActionBar();
				bar.setSubtitle(fileName);
				break;

			case RESULT__FILE_NOT_FOUND:
				Builder builderNoFile = new AlertDialog.Builder(context);
				builderNoFile.setMessage(context
						.getString(R.string.error_file_not_found));
				builderNoFile.setCancelable(true);
				builderNoFile.setTitle(R.string.title_file_load_error);
				builderNoFile.setPositiveButton("Ok", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				AlertDialog dialogNoFile = builderNoFile.create();
				dialogNoFile.show();
				break;

			case RESULT_FILE_NOT_READABLE:
				Builder builderNotReadable = new AlertDialog.Builder(context);
				builderNotReadable.setMessage(context
						.getString(R.string.error_file_not_readable));
				builderNotReadable.setCancelable(true);
				builderNotReadable.setTitle(R.string.title_file_load_error);
				builderNotReadable.setPositiveButton("Ok",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

				AlertDialog dialogNotReadable = builderNotReadable.create();
				dialogNotReadable.show();
				break;

			default:
				break;
			}

			progressBar.dismiss();
	}


	@Override
	protected Integer doInBackground(Void... params) {

		BufferedReader buff = null;
		CompressedSongFile extract = new CompressedSongFile(fileToLoad);

		try {

			VisitorStrategy strategy = new VisitorStrategy(xmlVisitor);

			/** extract the compressed source file */
			extract.extract();
			fileToLoad = extract.getExtractedFile();

			/** convert xml file before load **/
			XMLVersionConvert xmlConvert = new XMLVersionConvert(fileToLoad);
			xmlConvert.convert();
			fileToLoad = xmlConvert.getConvertedXml();

			// deserialize xml file
			buff = new BufferedReader(new FileReader(fileToLoad));
			Serializer serializer = new Persister(strategy);
			songRead = serializer.read(Song.class, buff);

			return RESULT_OK;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return RESULT__FILE_NOT_FOUND;

		} catch (java.lang.Exception e) {
			e.printStackTrace();
			return RESULT_FILE_NOT_READABLE;

		} finally {
			if (buff != null) {
				try {
					buff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// delete the extracted file
			extract.removeTmpFile();
		}
	}


	@Override
	protected void onProgressUpdate(Integer... values) {

		progressBar.incrementProgressBy(values[0]);
		// Log.d(TAG, String.format("pb: %d", progressBar.getProgress()));
	}
} // class 