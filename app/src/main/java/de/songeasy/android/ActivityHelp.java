/**
 * 
 */
package de.songeasy.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Simple html help viewer.
 * 
 * @author krein
 */
public class ActivityHelp extends Activity {

	private static final String CURRENT_URL_KEY = "currentUrl";

	private WebView webViewHelp;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.lay_activity_help);

		webViewHelp = (WebView) findViewById(R.id.webViewHelp);
		webViewHelp.getSettings().setBuiltInZoomControls(true);

		// load main help file or url saved on rotation
		if (savedInstanceState != null) {
			webViewHelp.restoreState(savedInstanceState);
		} else
			webViewHelp.loadUrl("file:///android_asset/www/songeasyhelp.html");
	}


	@Override
	public void onBackPressed() {
		// navigate back in html or call global back function
		if (webViewHelp.canGoBack() == true) {

			webViewHelp.goBack();
		} else
			super.onBackPressed();
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webViewHelp.saveState(outState);
	}
}
