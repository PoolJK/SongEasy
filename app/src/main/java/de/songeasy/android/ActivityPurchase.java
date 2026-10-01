/*
 * Copyright (C) 2015 Joerg Krein
 */

package de.songeasy.android;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class ActivityPurchase extends Activity {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Keys for identifying values in an intent
     */
    public static final String KEY_OPTION_SELECTED = "IsOptionSelected";

    /**
     * whether we were canceled or not
     */
    private Boolean canceled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_activity_purchase);

        // get parameters for this activity
        Bundle bundle = getIntent().getExtras();

        // either mode must be chosen
        if (bundle.containsKey(KEY_OPTION_SELECTED)) {
            if (bundle.getBoolean(KEY_OPTION_SELECTED))
                ((CheckBox) findViewById(R.id.checkBoxPurchasePdfExport)).setChecked(true);
        } else
            ((CheckBox) findViewById(R.id.checkBoxPurchasePdfExport)).setChecked(false);


        // register onClick for Ok button
        ((Button) findViewById(R.id.button_ok))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        canceled = false;
                        finish();
                    }
                });

        ((Button) findViewById(R.id.button_cancel))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        canceled = true;
                        finish();
                    }
                });
    }

    @Override
    public void finish() {

        if (canceled == false) {
            // Prepare data intent
            Intent data = new Intent();

            if (((CheckBox) findViewById(R.id.checkBoxPurchasePdfExport)).isChecked()) {
                data.putExtra(KEY_OPTION_SELECTED, true);
            }

            setResult(RESULT_OK, data);

        } else {
            setResult(RESULT_CANCELED);
        }

        super.finish();
    }

}

