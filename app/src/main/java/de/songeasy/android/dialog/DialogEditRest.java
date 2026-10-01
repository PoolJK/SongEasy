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
import android.widget.ImageButton;
import android.widget.ImageView;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnRestEditListener;
import de.songeasy.android.score.Rest;
import de.songeasy.android.score.TimedElement.NoteLength;

/**
 * Dialog for editing properties of a rest sign. Lets user change length value
 * and other attributes.
 * 
 * @author krein
 */
public class DialogEditRest extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private Rest rest;
	private OnRestEditListener onRestEditListener;


	// +++++++ getters and setters +++++++
	public void setOnRestEditListener(OnRestEditListener listener) {
		onRestEditListener = listener;
	}


	public DialogEditRest(Context context, Rest rest) {
		super(context);
		this.context = context;
		this.rest = rest;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.lay_dlg_edit_rests);

		setTitle(R.string.title_edit_rests);

		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						// evaluate checkbox dotted
						if (((CheckBox) findViewById(R.id.checkBoxDotted))
								.isChecked() == true) {
							rest.isDotted = true;
						} else {
							rest.isDotted = false;
						}

						// call listener to update edited values
						if (onRestEditListener != null) {
							onRestEditListener.onRestEdit(rest);
						}
						dismiss();
					}
				});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (onRestEditListener != null)
							onRestEditListener.onRestEdit(null);
						cancel();
					}
				});

		// set image with note length according to current rest value
		setLengthImage();

		// set dotted radio button according to current note value
		if (rest.isDotted)
			((CheckBox) findViewById(R.id.checkBoxDotted)).setChecked(true);

		// register OnClickListener for shorter button
		((ImageButton) findViewById(R.id.imageButtonShorter))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						switch (rest.getLength()) {

							case FULL:
								rest.setLength(NoteLength.HALF);
								setLengthImage();
								break;

							case HALF:
								rest.setLength(NoteLength.QUARTER);
								setLengthImage();
								break;

							case QUARTER:
								rest.setLength(NoteLength.EIGHTH);
								setLengthImage();
								break;

							case EIGHTH:
								rest.setLength(NoteLength.SIXTEENTH);
								setLengthImage();
								break;

							case SIXTEENTH:
								rest.setLength(NoteLength.THIRTYSECOND);
								setLengthImage();
								break;

							default:
								break;
						}

					}
				});

		// register OnClickListener for longer button
		((ImageButton) findViewById(R.id.imageButtonLonger))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						switch (rest.getLength()) {

							case THIRTYSECOND:
								rest.setLength(NoteLength.SIXTEENTH);
								setLengthImage();
								break;

							case SIXTEENTH:
								rest.setLength(NoteLength.EIGHTH);
								setLengthImage();
								break;

							case EIGHTH:
								rest.setLength(NoteLength.QUARTER);
								setLengthImage();
								break;

							case QUARTER:
								rest.setLength(NoteLength.HALF);
								setLengthImage();
								break;

							case HALF:
								rest.setLength(NoteLength.FULL);
								setLengthImage();
								break;

							default:
								break;
						}
					}
				});

		super.onCreate(savedInstanceState);
	}


	/**
	 * Set image of ImageView according to current note length.
	 */
	private void setLengthImage() {

		switch (rest.getLength()) {

			case FULL:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_full);
				break;

			case HALF:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_half);
				break;

			case QUARTER:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_4th);
				break;

			case EIGHTH:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_8th);
				break;

			case SIXTEENTH:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_16th);
				break;

			case THIRTYSECOND:
				((ImageView) findViewById(R.id.imageViewRestLength))
						.setImageResource(R.drawable.ic_note_32th);
				break;

			default:
				break;
		}
	}
}
