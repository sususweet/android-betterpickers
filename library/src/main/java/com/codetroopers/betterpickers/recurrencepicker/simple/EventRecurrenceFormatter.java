/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codetroopers.betterpickers.recurrencepicker.simple;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.TimeFormatException;

import com.codetroopers.betterpickers.R;
import com.codetroopers.betterpickers.recurrencepicker.simple.EventRecurrence;
import com.codetroopers.betterpickers.recurrencepicker.simple.SimpleRecurrencePickerDialogFragment;

import java.util.Calendar;

public class EventRecurrenceFormatter {

    private static int[] mMonthRepeatByDayOfWeekIds;
    private static String[][] mMonthRepeatByDayOfWeekStrs;

    public static String getRepeatString(Context context, Resources r, EventRecurrence recurrence, boolean includeEndString) {
        String endString = "";
        if (includeEndString) {
            StringBuilder sb = new StringBuilder();
            if (recurrence.until != null) {
                try {
//                    Time time = new Time();
//                    // until is in second
//                    time.set(Long.valueOf(recurrence.until) * 1000);
                    final String dateStr = DateUtils.formatDateTime(context, Long.valueOf(recurrence.until) * 1000, DateUtils.FORMAT_NUMERIC_DATE);
                    sb.append(r.getString(R.string.endByDate, dateStr));
                } catch (TimeFormatException e) {
                }
            }

            if (recurrence.count > 0) {
                sb.append(r.getQuantityString(R.plurals.endByCount, recurrence.count,
                        recurrence.count));
            }
            endString = sb.toString();
        }

        // TODO Implement "Until" portion of string, as well as custom settings
        int interval = recurrence.interval <= 1 ? 1 : recurrence.interval;
        switch (recurrence.freq) {
            case EventRecurrence.HOURLY:
                return r.getQuantityString(R.plurals.hourly, interval, interval) + endString;
            case EventRecurrence.DAILY:
                return r.getQuantityString(R.plurals.daily, interval, interval) + endString;
            case EventRecurrence.WEEKLY: {
                if (recurrence.repeatsOnEveryWeekDay()) {
                    return r.getString(R.string.every_weekday) + endString;
                } else {
                    return r.getQuantityString(R.plurals.recurrence_interval_weekly, interval, interval) + endString;
                }
            }
            case EventRecurrence.MONTHLY: {
                String details = "";
                if (recurrence.byday != null) {
                    int weekday = EventRecurrence.day2CalendarDay(recurrence.byday[0]) - 1;
                    // Cache this stuff so we won't have to redo work again later.
                    cacheMonthRepeatStrings(r, weekday);

                    int dayNumber = recurrence.bydayNum[0];
                    if(dayNumber == SimpleRecurrencePickerDialogFragment.LAST_NTH_DAY_OF_WEEK){
                        dayNumber = 5;
                    }
                    details = mMonthRepeatByDayOfWeekStrs[weekday][dayNumber - 1];
                }
                return r.getQuantityString(R.plurals.monthly, interval, interval, details) + endString;
            }
            case EventRecurrence.YEARLY:
                return r.getQuantityString(R.plurals.yearly_plain, interval, interval, "") + endString;
        }

        return null;
    }

    private static void cacheMonthRepeatStrings(Resources r, int weekday) {
        if (mMonthRepeatByDayOfWeekIds == null) {
            mMonthRepeatByDayOfWeekIds = new int[7];
            mMonthRepeatByDayOfWeekIds[0] = R.array.repeat_by_nth_sun;
            mMonthRepeatByDayOfWeekIds[1] = R.array.repeat_by_nth_mon;
            mMonthRepeatByDayOfWeekIds[2] = R.array.repeat_by_nth_tues;
            mMonthRepeatByDayOfWeekIds[3] = R.array.repeat_by_nth_wed;
            mMonthRepeatByDayOfWeekIds[4] = R.array.repeat_by_nth_thurs;
            mMonthRepeatByDayOfWeekIds[5] = R.array.repeat_by_nth_fri;
            mMonthRepeatByDayOfWeekIds[6] = R.array.repeat_by_nth_sat;
        }
        if (mMonthRepeatByDayOfWeekStrs == null) {
            mMonthRepeatByDayOfWeekStrs = new String[7][];
        }
        if (mMonthRepeatByDayOfWeekStrs[weekday] == null) {
            mMonthRepeatByDayOfWeekStrs[weekday] = r.getStringArray(mMonthRepeatByDayOfWeekIds[weekday]);
        }
    }

    /**
     * Converts day of week to a String.
     *
     * @param day a EventRecurrence constant
     * @return day of week as a string
     */
    private static String dayToString(int day, int dayOfWeekLength) {
        return DateUtils.getDayOfWeekString(dayToUtilDay(day), dayOfWeekLength);
    }

    /**
     * Converts EventRecurrence's day of week to DateUtil's day of week.
     *
     * @param day of week as an EventRecurrence value
     * @return day of week as a DateUtil value.
     */
    private static int dayToUtilDay(int day) {
        switch (day) {
            case EventRecurrence.SU:
                return Calendar.SUNDAY;
            case EventRecurrence.MO:
                return Calendar.MONDAY;
            case EventRecurrence.TU:
                return Calendar.TUESDAY;
            case EventRecurrence.WE:
                return Calendar.WEDNESDAY;
            case EventRecurrence.TH:
                return Calendar.THURSDAY;
            case EventRecurrence.FR:
                return Calendar.FRIDAY;
            case EventRecurrence.SA:
                return Calendar.SATURDAY;
            default:
                throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
}
