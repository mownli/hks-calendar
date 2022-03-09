package hks.calendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityEditEventType extends AppCompatActivity {
	int r = 0;
	int g = 0;
	int b = 0;
	String name = "";
	boolean usesSeparateNotificationChannel;

	TextView bullet;
	EditText nameInput;
	Switch uSNCField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_type_edit);

		getSupportActionBar().setTitle(R.string.titleActivityEditEventType);

		bullet = findViewById(R.id.fieldColorPreview);
		nameInput = findViewById(R.id.nameInput);
		uSNCField = findViewById(R.id.fieldUSNC);


		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		Button buttonSave = findViewById(R.id.buttonEventTypeSave);
		Button buttonDelete = findViewById(R.id.buttonEventTypeDelete);

		if(action != null && action.equals(ActivityEventTypesList.ACTION_EDIT_EVENT_TYPE)) {
			int color = extras.getInt(ActivityEventTypesList.EDIT_EVENT_PARAM_COLOR);
			r = Color.red(color);
			g = Color.green(color);
			b = Color.blue(color);
			name = extras.getString(ActivityEventTypesList.EDIT_EVENT_PARAM_NAME);
			usesSeparateNotificationChannel = extras.getBoolean(ActivityEventTypesList.EDIT_EVENT_PARAM_USNC);

			nameInput.setText(name);
			uSNCField.setChecked(usesSeparateNotificationChannel);

			if(extras.getBoolean(ActivityEventTypesList.EDIT_EVENT_PARAM_DELETION_ALLOWED))
				buttonDelete.setOnClickListener(view -> {
					Intent data = new Intent();
					data.putExtra(ActivityEventTypesList.ACTION_RESULT, ActivityEventTypesList.ACTION_RESULT_DELETED);
					setResult(RESULT_OK, data);
					finish();
				});
			else
				buttonDelete.setEnabled(false);
		}
		else {
			buttonDelete.setEnabled(false);
		}

		recolorBullet();

		EditText colorInputR = findViewById(R.id.eventTypeEditColorR);
		SeekBar colorInputRSeek = findViewById(R.id.eventTypeEditSeekColorR);
		EditText colorInputG = findViewById(R.id.eventTypeEditColorG);
		SeekBar colorInputGSeek = findViewById(R.id.eventTypeEditSeekColorG);
		EditText colorInputB = findViewById(R.id.eventTypeEditColorB);
		SeekBar colorInputBSeek = findViewById(R.id.eventTypeEditSeekColorB);

		colorInputR.setText(String.valueOf(r));
		colorInputRSeek.setProgress(r);
		colorInputG.setText(String.valueOf(g));
		colorInputGSeek.setProgress(g);
		colorInputB.setText(String.valueOf(b));
		colorInputBSeek.setProgress(b);

		colorInputRSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(!fromUser)
					return;
				colorInputR.setText(String.valueOf(r = progress));
				recolorBullet();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		colorInputR.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					r = Integer.valueOf(s.toString());
					if(r < 0) {
						r = 0;
						colorInputR.setText("0");
						colorInputR.setSelection(1);
					}
					else if(r > 255) {
						r = 255;
						colorInputR.setText("255");
						colorInputR.setSelection(3);
					}
					else
						colorInputR.setSelection(start + count);
				}
				catch(NumberFormatException E) {
					r = 0;
					colorInputR.setText("0");
					colorInputR.setSelection(1);
				}
				colorInputRSeek.setProgress(r);
				recolorBullet();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		colorInputGSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(!fromUser)
					return;
				colorInputG.setText(String.valueOf(g = progress));
				recolorBullet();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		colorInputG.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					g = Integer.valueOf(s.toString());
					if(g < 0) {
						g = 0;
						colorInputG.setText("0");
						colorInputG.setSelection(1);
					}
					else if(g > 255) {
						g = 255;
						colorInputG.setText("255");
						colorInputG.setSelection(3);
					}
					else
						colorInputG.setSelection(start + count);
				}
				catch(NumberFormatException E) {
					g = 0;
					colorInputG.setText("0");
					colorInputG.setSelection(1);
				}
				colorInputGSeek.setProgress(g);
				recolorBullet();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		colorInputBSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(!fromUser)
					return;
				colorInputB.setText(String.valueOf(b = progress));
				recolorBullet();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		colorInputB.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					b = Integer.valueOf(s.toString());
					if(b < 0) {
						b = 0;
						colorInputB.setText("0");
						colorInputB.setSelection(1);
					}
					else if(b > 255) {
						b = 255;
						colorInputB.setText("255");
						colorInputB.setSelection(3);
					}
					else
						colorInputB.setSelection(start + count);
				}
				catch(NumberFormatException E) {
					b = 0;
					colorInputB.setText("0");
					colorInputB.setSelection(1);
				}
				colorInputBSeek.setProgress(b);
				recolorBullet();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		buttonSave.setOnClickListener(view -> {
			name = nameInput.getText().toString().trim();
			if("".equals(name)) {
				Toast.makeText(HKSCalendarApp.appCtx, getResources().getString(R.string.notAllFieldsAreFilled), Toast.LENGTH_LONG).show();
				return;
			}

			Intent data = new Intent();
			data.putExtra(ActivityEventTypesList.ACTION_RESULT, ActivityEventTypesList.ACTION_RESULT_EDITED);
			data.putExtra(ActivityEventTypesList.EDIT_EVENT_PARAM_COLOR, Color.argb(255, r, g, b));
			data.putExtra(ActivityEventTypesList.EDIT_EVENT_PARAM_NAME, name);
			data.putExtra(ActivityEventTypesList.EDIT_EVENT_PARAM_USNC, uSNCField.isChecked());

			setResult(RESULT_OK, data);
			finish();
		});
	}

	void recolorBullet() {
		bullet.setTextColor(Color.argb(255, r, g, b));
	}
}
