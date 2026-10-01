/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnLyricsEnteredListener;

/**
 interface OnLyricsEnteredListener {

 abstract void onLyricsEntered(String text, int lineNumber);
 }
 **/

/**
 * Dialog to enter or edit lyrics
 * 
 * @author heisse
 */
public class DialogEditLyric extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private String lyrics;
	private int lineNumber;
	private OnLyricsEnteredListener lyricsListener;


	public DialogEditLyric(Context context, String lyrics, int lineNumber) {

		super(context);
		this.context = context;
		this.lyrics = lyrics;
		this.lineNumber = lineNumber;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_dlg_edit_lyrics);
		// setTitle(context.getString(R.string.title_edit_lyrics));
		((EditText) findViewById(R.id.editTextLyrics)).setText(lyrics);
		if (lineNumber != 0)
			((CheckBox) findViewById(R.id.checkBoxLyricsLine)).setChecked(true);

		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						lyrics = ((EditText) findViewById(R.id.editTextLyrics))
								.getText().toString();
						if (((CheckBox) findViewById(R.id.checkBoxLyricsLine))
								.isChecked() == true)
							lineNumber = 1;
						else
							lineNumber = 0;

						if (lyricsListener != null)
							lyricsListener.onLyricsEntered(lyrics, lineNumber);

						dismiss();
					}
				});
		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (lyricsListener != null)
							lyricsListener.onLyricsEntered(null, 0);
						cancel();
					}
				});
	}


	public void setOnLyricsEnteredListener(OnLyricsEnteredListener listener) {

		lyricsListener = listener;
	}

}
