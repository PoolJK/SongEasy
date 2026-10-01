package de.songeasy.android.fileactivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import de.songeasy.android.R;
import de.songeasy.android.ScoreView;
import de.songeasy.android.score.PageProperties;
import de.songeasy.android.score.Song;

import java.io.File;


/**
 * AsyncTask for exporting song. Shows a progress bar while exporting.
 */
public class AsyncTaskExport extends AsyncTask<Void, Integer, Integer> {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private Song songToExport;
    private File selectedFile;
    private FileType fileType;
    private FileExport fileExport;
    private int pageCount;
    private ProgressDialog progressBar;

    /**
     * Create new background task for exporting song file
     *  @param context      Context of main activity
     * @param songToExport song to export
     * @param selectedFile file to export song to
     * @param fileType     type of export
     */
    public AsyncTaskExport(Context context, Song songToExport, File selectedFile, FileType fileType) {
        this.context = context;
        this.songToExport = songToExport;
        this.selectedFile = selectedFile;
        this.fileType = fileType;
        this.pageCount = songToExport.getNumberOfPages();
        fileExport = new FileExport(
                selectedFile, fileType,
                FileExport.PaperType.A4, songToExport, pageCount);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        songToExport.setExportIsActive(true);

        if (progressBar == null){
            progressBar = new ProgressDialog(context);
        }
        progressBar.setTitle(context.getResources().getString(R.string.prg_bar_exporting_title));
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setIndeterminate(false);
        progressBar.setCancelable(true);
        progressBar.setProgressNumberFormat(null);
        progressBar.setProgressPercentFormat(null);
        progressBar.setProgress(0);
        progressBar.setMax(pageCount);
        progressBar.show();
    }



    @Override
    protected Integer doInBackground(Void... params) {

        for (int i = 0; i < pageCount; i++) {
            Log.d(TAG, String.format("export page: %d", i));
            fileExport.write(i);
            publishProgress(1);
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        // progressBar.setProgress(values[0]);
        progressBar.incrementProgressBy(values[0]);

    }


    @Override
    protected void onPostExecute(Integer integer) {

        if (progressBar.isShowing()){
            progressBar.dismiss();
        }
        songToExport.setExportIsActive(false);
    }

}
