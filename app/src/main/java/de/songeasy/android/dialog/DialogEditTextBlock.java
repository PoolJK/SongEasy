/*
 * Copyright (C) 2013 Joerg Krein
 */

package de.songeasy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnTextBlockEnteredListener;
import de.songeasy.android.score.TextBlock;

/**
 // Listener for dialog closed event 
 interface OnTextBlockEnteredListener {

 abstract void onTextBlockEntered(TextBlock textBlock);
 }
 **/

/**
 * Dialog for entering title and content of an text block placed below score.
 * 
 * @author Joerg Krein
 * 
 */
public class DialogEditTextBlock extends Dialog {

	private Context context;

	/** reference on TextBlock to edit */
	private TextBlock textBlock;
	/** title of text block */
	private String title;
	/** text content of text block */
	private String content;
	/** listener to call on text entered finished */
	private OnTextBlockEnteredListener listener = null;


	// +++++++ getters and setters +++++++

	public void setOnTextBlockEnteredListener(
			OnTextBlockEnteredListener listener) {
		this.listener = listener;
	}


	public DialogEditTextBlock(Context context, TextBlock textBlock) {
		super(context);
		this.context = context;
		this.textBlock = textBlock;
		this.title = textBlock.getTitle();
		this.content = textBlock.getContent();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_dlg_edit_textblock);
		// setTitle(context.getString(R.string.title_edit_textblock));

		// write predefined strings in text boxes
		((EditText) findViewById(R.id.editTextBlockContent)).setText(content);
		((EditText) findViewById(R.id.editTextBlockTitle)).setText(title);

		// set onClick actions for buttons
		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						title = ((EditText) findViewById(R.id.editTextBlockTitle))
								.getText().toString();
						content = ((EditText) findViewById(R.id.editTextBlockContent))
								.getText().toString();

						textBlock.setTitle(title);
						textBlock.setContent(content);

						if (listener != null)
							listener.onTextBlockEntered(textBlock);

						dismiss();

					}
				});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (listener != null)
							listener.onTextBlockEntered(null);
						cancel();
					}
				});

	}
} // class
