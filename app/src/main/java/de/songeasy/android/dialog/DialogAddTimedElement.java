/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnLengthSelectedListener;
import de.songeasy.android.score.TimedElement;
import de.songeasy.android.score.TimedElement.NoteLength;

/**
 * Little PopUp Dialog to select lenght of TimedElement to add.
 * 
 * @author krein
 * 
 */
public class DialogAddTimedElement extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private ListView listViewNotes;
	private ArrayList<String> noteValuesList = new ArrayList<String>();
	private OnLengthSelectedListener onLengthSelectedListener;
	// kind of TimedElment to show options for
	private Class<? extends TimedElement> timedType;

	/**
	 * Inner class, ArrayAdapter for holding images of note value icons.
	 */
	class NoteImageAdapter extends ArrayAdapter<String> {

		/**
		 * Get width of image items
		 * 
		 * @return width in pixels
		 */
		public int getWidth() {
			Drawable icon = context.getResources().getDrawable(
					R.drawable.ic_note_4th);
			return icon.getMinimumWidth();
		};


		/**
		 * Constructor of class
		 */
		public NoteImageAdapter() {

			super(context, R.layout.lay_timedelements_row, 0, noteValuesList);
		}


		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.lay_timedelements_row, parent,
					false);

			ImageView icon = (ImageView) row.findViewById(R.id.noteIcon);

			// TODO: hard coded!
			switch (position)
				{
				case 0:
					icon.setImageResource(R.drawable.ic_note_32th);
					break;

				case 1:
					icon.setImageResource(R.drawable.ic_note_16th);
					break;

				case 2:
					icon.setImageResource(R.drawable.ic_note_8th);
					break;

				case 3:
					icon.setImageResource(R.drawable.ic_note_4th);
					break;

				case 4:
					icon.setImageResource(R.drawable.ic_note_half);
					break;

				case 5:
					icon.setImageResource(R.drawable.ic_note_full);
					break;

				default:
					icon.setImageResource(R.drawable.ic_note_4th);
					break;
				}

			return (row);
		}
	} // class NoteImageAdapter


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++
	/**
	 * Register listener for note selected event.
	 * 
	 * @param listener
	 *            listener for note selected event
	 */
	public void setOnLengthSelectedListener(OnLengthSelectedListener listener) {

		this.onLengthSelectedListener = listener;
	}


	/**
	 * Constructor of class
	 * 
	 * @param context
	 *            Application context
	 */
	public DialogAddTimedElement(Context context,
			Class<? extends TimedElement> type) {
		super(context);
		this.context = context;
		this.timedType = type;
		for (TimedElement.NoteLength length : TimedElement.NoteLength.values()) {
			// sixtyfourth not supported here
			if (length == NoteLength.SIXTYFOURTH)
				continue;

			noteValuesList.add(length.toString());
			// Log.d(TAG, String.format("added: %s", length.toString()));
		}
	}


	/**
	 * Set position of dialog.
	 * 
	 * @param pos
	 *            upper left corner of dialog
	 */
	public void setPosition(Point pos) {

		WindowManager.LayoutParams wmlp = getWindow().getAttributes();
		wmlp.gravity = Gravity.TOP | Gravity.LEFT;
		wmlp.x = pos.x;
		wmlp.y = pos.y;
		getWindow().setAttributes(wmlp);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// we want no title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lay_dlg_add_timedelements);
		listViewNotes = (ListView) findViewById(R.id.listViewTimedElementsValues);
		NoteImageAdapter imageAdapter = new NoteImageAdapter();
		listViewNotes.setAdapter(imageAdapter);
		listViewNotes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				TimedElement.NoteLength length = NoteLength.QUARTER;

				// TODO: hard coded!
				switch (position)
					{
					case 0:
						length = NoteLength.THIRTYSECOND;
						break;

					case 1:
						length = NoteLength.SIXTEENTH;
						break;

					case 2:
						length = NoteLength.EIGHTH;
						break;

					case 3:
						length = NoteLength.QUARTER;
						break;

					case 4:
						length = NoteLength.HALF;
						break;

					case 5:
						length = NoteLength.FULL;
						break;

					default:
						length = NoteLength.QUARTER;
						break;
					}

				if (onLengthSelectedListener != null)
					onLengthSelectedListener.onLengthSelected(length);

				dismiss();
			}
		});

		// adjust width of list view to size of images
		listViewNotes
				.setLayoutParams(new LinearLayout.LayoutParams(imageAdapter
						.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT));

		// this.getWindow().setLayout(imageAdapter.getWidth() + 20,
		// ViewGroup.LayoutParams.WRAP_CONTENT);

		super.onCreate(savedInstanceState);
	}

} // class DialogAddNote
