package hks.calendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

class DialogMonthSelection {
	private EditText yearEdit = null;

	private int year = -1;

	DialogMonthSelection(Context ctx, boolean enableYearEdit, int y) {
		final Dialog d = new Dialog(ctx);
		d.setCanceledOnTouchOutside(true);

		d.setContentView(R.layout.dialog_month_selection);

		yearEdit = d.findViewById(R.id.monthSelectionYearEdit);
		if(!enableYearEdit) {
			LinearLayout l = d.findViewById(R.id.monthSelectionLayout);
			l.removeView(yearEdit);
		}
		else {
			yearEdit.setText(String.valueOf(year = y));
			yearEdit.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					try {
						int val = Integer.valueOf(s.toString());
						year = val;
						if(val < Consts.MIN_YEAR || val > Consts.MAX_YEAR)
							yearEdit.setTextColor(Color.RED);
						else
							yearEdit.setTextColor(Color.BLACK);
					}
					catch(NumberFormatException E) {
						year = -1;
						yearEdit.setTextColor(Color.RED);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void afterTextChanged(Editable s) {}
			});
		}

		d.show();

		for(int i = 0; i < 12; i++) {
			final int month = i;
			d.findViewById(ctx.getResources().getIdentifier("monthSelection" + i, "id", ctx.getPackageName())).setOnClickListener(v -> {
				if(enableYearEdit && (year < Consts.MIN_YEAR || year > Consts.MAX_YEAR)) {
					Toast.makeText(ctx, HKSCalendarApp.appCtx.getResources().getString(R.string.errorsInFieldValues), Toast.LENGTH_LONG).show();
					return;
				}
				selected(month, year);
				d.dismiss();
			});
		}

		// Move to top of the screen
		Window window = d.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		wlp.gravity = Gravity.TOP;
		window.setAttributes(wlp);
	}

	void selected(int val, int y) {
	}
}
