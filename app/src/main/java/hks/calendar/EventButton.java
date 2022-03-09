package hks.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

public class EventButton extends androidx.appcompat.widget.AppCompatButton {
	private Event event = null;

	Event getEvent() {
		return event;
	}

	void setEvent(Event event) {
		this.event = event;
		setText(event.toStringShort());
		if(event.ts.getTimeInMillis() < System.currentTimeMillis())
			setAlpha(.5f);
		else
			setAlpha(1f);
	}

	private void init(Context ctx) {
		TypedValue outValue = new TypedValue();
		ctx.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
		setBackgroundResource(outValue.resourceId);

		setAllCaps(false);
		setGravity(Gravity.LEFT | Gravity.CENTER);
		setMinHeight(0);
		setMinimumHeight(0);
		//setPadding(getPaddingLeft()/4*3, getPaddingTop()/4*3, getPaddingRight()/4*3, getPaddingBottom()/4*3);
	}

	public EventButton(Context ctx) {
		super(ctx);
		init(ctx);
	}
	public EventButton(Context ctx, AttributeSet set) {
		super(ctx, set);
		init(ctx);
	}
	public EventButton(Context ctx, AttributeSet set, int i) {
		super(ctx, set, i);
		init(ctx);
	}

	public EventButton(Context ctx, Event ev) {
		super(ctx);
		init(ctx);

		if(ev != null)
			setEvent(ev);
	}
}
