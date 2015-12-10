package de.unisaarland.UniApp.settings;

import android.app.TimePickerDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaNotificationTimes;
import de.unisaarland.UniApp.restaurant.notifications.MensaNotifications;
import de.unisaarland.UniApp.utils.Util;

public class MensaNotificationsPreference extends DialogPreference {

    private final static int[] weekDays = {
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY};

    private DayListAdapter lastAdapter = null;

    public MensaNotificationsPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
        setDialogLayoutResource(R.layout.mensa_notification_settings);
    }

    @Override
    protected View onCreateDialogView() {
        View v = super.onCreateDialogView();
        ListView dayList = (ListView) v.findViewById(R.id.dayList);
        lastAdapter = new DayListAdapter(getContext(),
                getPersistedLong(MensaNotificationTimes.DEFAULT_TIMES));
        dayList.setAdapter(lastAdapter);
        return v;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            persistLong(lastAdapter.times.getRaw());
            setSummary(getSummary());
            new MensaNotifications(getContext()).setNext();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (!restorePersistedValue && shouldPersist())
            persistLong(MensaNotificationTimes.DEFAULT_TIMES);
    }

    @Override
    public CharSequence getSummary() {
        String[] weekDayNames = null;
        StringBuilder sb = new StringBuilder();
        MensaNotificationTimes times = new MensaNotificationTimes(
                getPersistedLong(MensaNotificationTimes.DEFAULT_TIMES));
        for (int i = 0; i < 5; ++i) {
            if (!times.isEnabled(i))
                continue;
            if (sb.length() > 0)
                sb.append(", ");

            if (weekDayNames == null)
                weekDayNames = new DateFormatSymbols().getShortWeekdays();
            String weekday = weekDayNames[weekDays[i]];
            sb.append(weekday).append(' ').append(getLocalizedTime(getContext(), times, i));
        }
        if (sb.length() == 0)
            return getContext().getString(R.string.none);
        return sb.toString();
    }

    protected static String getLocalizedTime(Context context, MensaNotificationTimes times, int day) {
        Calendar  cal = Calendar.getInstance();
        cal.set(0, 0, 0, times.getHour(day), times.getMinute(day), 0);
        return DateFormat.getTimeFormat(context).format(cal.getTime());
    }

    private static class DayListAdapter extends BaseAdapter {
        private final Context context;
        private final MensaNotificationTimes times;

        private DayListAdapter(Context context, long times) {
            this.context = context;
            this.times = new MensaNotificationTimes(times);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < 0 || position > 5)
                throw new AssertionError("position " + position);
            if (convertView == null)
                convertView = View.inflate(context, R.layout.mensa_notification_day_item, null);
            String weekday = new DateFormatSymbols().getWeekdays()[weekDays[position]];
            TextView titleView = (TextView) convertView.findViewById(R.id.dayTitle);
            titleView.setText(weekday);
            boolean enabled = times.isEnabled(position);
            TextView summaryView = (TextView) convertView.findViewById(R.id.summary);
            summaryView.setText(getLocalizedTime(context, times, position));
            summaryView.setEnabled(enabled);
            RelativeLayout relLayout = (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
            relLayout.setOnClickListener(chooseTimeClickListener);
            relLayout.setTag(R.id.mensa_notification_day_tag, position);
            CheckBox enabledCB = (CheckBox) convertView.findViewById(R.id.checkbox);
            enabledCB.setChecked(enabled);
            enabledCB.setOnCheckedChangeListener(checkboxChangeListener);
            return convertView;
        }

        private CompoundButton.OnCheckedChangeListener checkboxChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View listEntry = Util.findParentWithId(buttonView, R.id.relativeLayout);
                int day = (Integer) listEntry.getTag(R.id.mensa_notification_day_tag);
                times.setEnabled(day, isChecked);
                DayListAdapter.this.notifyDataSetChanged();
            }
        };

        private View.OnClickListener chooseTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int day = (Integer) v.getTag(R.id.mensa_notification_day_tag);
                TimePickerDialog.OnTimeSetListener timePickedListener =
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                times.setHour(day, hourOfDay);
                                times.setMinute(day, minute);
                                DayListAdapter.this.notifyDataSetChanged();
                            }
                        };

                TimePickerDialog pickerDialog = new TimePickerDialog(context,
                        timePickedListener, times.getHour(day), times.getMinute(day),
                        DateFormat.is24HourFormat(context));
                pickerDialog.show();
            }
        };
    }
}