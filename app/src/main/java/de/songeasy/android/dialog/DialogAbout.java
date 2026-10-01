package de.songeasy.android.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.TextView;
import de.songeasy.android.R;

public class DialogAbout extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;


	public DialogAbout(Context context) {
		super(context);
		this.context = context;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_dlg_about);

		// get app version string
		PackageInfo pInfo = null;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String appVersion = null;

		// provoke error
		// String errorString = null;
		// Log.d(TAG, errorString.substring(2));

		if (pInfo != null)
			appVersion = pInfo.versionName;

		// set title of about dialog
		String appName = context.getResources().getString(R.string.app_name);
		setTitle(appName + "  " + appVersion);

		// try to center title
		TextView titleView = (TextView) findViewById(android.R.id.title);
		if (titleView != null) {
			titleView.setGravity(Gravity.CENTER);
		}

		// setFeatureDrawableResource(
		// Window.FEATURE_LEFT_ICON, R.drawable.launcher);

		TextView aboutText = (TextView) findViewById(R.id.textViewAbout);
		// make links clickable
		aboutText.setMovementMethod(LinkMovementMethod.getInstance());
		// set html content
		aboutText.setText(Html.fromHtml(readRawTextFile("www/about.html")));
	}


	private String readRawTextFile(String filename) {
		InputStream inputStream = null;

		try {
			inputStream = this.context.getResources().getAssets()
					.open(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
			return new String();
		}

		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;

		StringBuilder text = new StringBuilder();
		try {
			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e2) {
			e2.printStackTrace();
			return new String();
		}

		return text.toString();
	}
}
