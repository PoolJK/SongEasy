package de.songeasy.android.fileactivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import de.songeasy.android.interfaces.OnXmlNextMeasureListener;
import de.songeasy.android.score.Song;
import de.songeasy.android.xml.CompressedSongFile;
import de.songeasy.android.xml.XMLVisitor;

/**
 * AsyncTask for saving song. Shows a progress bar while saving.
 */
public class AsyncTaskSave extends AsyncTask<Void, Integer, Integer> {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private static final int RESULT_OK = 0;
	private static final int RESULT_IO_ERROR = 1;
	private static final int RESULT_SERIALIZER_ERR = 2;
	private static final int INCRIMENTVALUE = 1;

	private ProgressDialog progressBar;
	private Context context;
	private File fileToSave;
	private String fileName;
	private Song song;
	private XMLVisitor xmlVisitor;
	private int numberOfMeasures = 0;


	/**
	 * Create new task to save file.
	 * 
	 * @param context
	 *            Context of calling activity
	 * @param song
	 *            Song to safe in file
	 * @param file
	 *            File to safe song in
	 */
	public AsyncTaskSave(Context context, Song song, File file) {

		this.context = context;
		this.song = song;
		this.fileToSave = file;
		this.fileName = fileToSave.getName();
		numberOfMeasures = song.getNumberOfMeasures();
		// Log.d(TAG, String.format("measureNumber: %d", numberOfMeasures));
		xmlVisitor = new XMLVisitor();
		xmlVisitor.setOnNextMeasureListener(new OnXmlNextMeasureListener() {

			@Override
			public void onNextMeasure() {
				publishProgress(INCRIMENTVALUE);

			}
		});
	}


	@Override
	protected void onPreExecute() {

		progressBar = new ProgressDialog(context);
		progressBar.setTitle(context.getResources().getString(R.string.prg_bar_saving_title));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setCancelable(true);
		progressBar.setProgressNumberFormat(null);
		progressBar.setProgressPercentFormat(null);
		progressBar.setProgress(0);
		progressBar.setMax(numberOfMeasures);
		// progressBar.setMax((int) len);
		progressBar.show();

		// settings = getSharedPreferences(PREFS_NAME, 0);
		// load = settings.getString("loadFile", "");

	}


	@Override
	protected void onPostExecute(Integer result) {

		switch (result)
			{
			case RESULT_OK:
				// set possibly changed songname in actionbar
				ActionBar bar = ((Activity) context).getActionBar();
				bar.setSubtitle(fileName);
				break;

			case RESULT_IO_ERROR:
				Builder builderNoFile = new AlertDialog.Builder(context);
				builderNoFile.setMessage(context
						.getString(R.string.error_file_not_writeable));
				builderNoFile.setCancelable(true);
				builderNoFile.setTitle(R.string.title_file_save_error);
				builderNoFile.setPositiveButton("Ok", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				AlertDialog dialogNoFile = builderNoFile.create();
				dialogNoFile.show();
				break;

			case RESULT_SERIALIZER_ERR:
				Builder builderNotReadable = new AlertDialog.Builder(context);
				builderNotReadable.setMessage(context
						.getString(R.string.error_file_not_saved));
				builderNotReadable.setCancelable(true);
				builderNotReadable.setTitle(R.string.title_file_save_error);
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

		try {

			// store song in new xml file
			VisitorStrategy strategy = new VisitorStrategy(xmlVisitor);
			BufferedWriter buff = new BufferedWriter(new FileWriter(fileToSave));
			Serializer serializer = new Persister(strategy);

			if (song != null)
				serializer.write(song, buff);
			buff.close();

			// write file into compressed one with extension
			CompressedSongFile compressedSong = new CompressedSongFile(
					fileToSave);
			compressedSong.compress();
			return RESULT_OK;

		} catch (IOException e) {
			e.printStackTrace();
			return RESULT_IO_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_SERIALIZER_ERR;
		}
	}


	@Override
	protected void onProgressUpdate(Integer... values) {

		// progressBar.setProgress(values[0]);
		progressBar.incrementProgressBy(values[0]);

	}
} // class

