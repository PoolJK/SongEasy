package de.songeasy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnLabelEditListener;

/**
 interface OnLabelEditListener {

 abstract void onLabelEdit(String text);
 }
 **/

/**
 * Dialog for editing song annotations.
 * 
 * @author jorgkrein
 */
public class DialogEditAnnotation extends Dialog {

	private Context context;
	private String partName;
	private OnLabelEditListener labelListener = null;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	public void setLabelListener(OnLabelEditListener labelListener) {
		this.labelListener = labelListener;
	}


	public DialogEditAnnotation(Context context, String partName) {
		super(context);

		this.context = context;
		this.partName = partName;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_dlg_edit_label);
		// setTitle(context.getString(R.string.title_edit_annotation));

		// fill whole screen
		// getWindow().setLayout(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT);

		((EditText) findViewById(R.id.editTextAnnotationLabel))
				.setText(partName);

		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						partName = ((EditText) findViewById(R.id.editTextAnnotationLabel))
								.getText().toString();

						if (labelListener != null)
							labelListener.onLabelEdit(partName);

						dismiss();
					}
				});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (labelListener != null)
							labelListener.onLabelEdit(null);
						cancel();
					}
				});
	}

}
