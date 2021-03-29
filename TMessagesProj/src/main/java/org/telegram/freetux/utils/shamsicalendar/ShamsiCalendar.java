package org.telegram.freetux.utils.shamsicalendar;

import android.annotation.SuppressLint;
import android.support.v4.view.PointerIconCompat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@SuppressLint({"UseValueOf"})
public class ShamsiCalendar {
    public static final int CURRENT_CENTURY = 13;
    public static final String[] shamsiMonths;
    private static final String[] shamsiMonthsEn;
    private static final String[] shamsiMonthsKur;
    public static final String[] shamsiWeekDays;
    public static final String[] shamsiWeekDaysKu;
    private static final Map<Integer, String> shamsiWeekDaysKuMap;
    private static final Map<Integer, String> shamsiWeekDaysMap;

    static {
        shamsiWeekDays = new String[]{"\u0634\u0646\u0628\u0647", "\u06cc\u06a9\u0634\u0646\u0628\u0647", "\u062f\u0648\u0634\u0646\u0628\u0647", "\u0633\u0647 \u0634\u0646\u0628\u0647", "\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647", "\u067e\u0646\u062c \u0634\u0646\u0628\u0647", "\u062c\u0645\u0639\u0647"};
        shamsiWeekDaysKu = new String[]{"\u0634\u0647 \u0645\u06cc", "\u06cc\u0647 \u06a9 \u0634\u0647 \u0645", "\u062f\u0648 \u0634\u0647 \u0645", "\u0633\u06cc \u0634\u0647 \u0645", "\u0686\u0627\u0631\u0634\u0647 \u0645", "\u067e\u0646\u062c \u0634\u0647 \u0645", "\u0626\u06cc\u0646"};
        shamsiMonths = new String[]{"\u0641\u0631\u0648\u0631\u062f\u06cc\u0646", "\u0627\u0631\u062f\u06cc\u0628\u0647\u0634\u062a", "\u062e\u0631\u062f\u0627\u062f", "\u062a\u06cc\u0631", "\u0645\u0631\u062f\u0627\u062f", "\u0634\u0647\u0631\u06cc\u0648\u0631", "\u0645\u0647\u0631", "\u0622\u0628\u0627\u0646", "\u0622\u0630\u0631", "\u062f\u06cc", "\u0628\u0647\u0645\u0646", "\u0627\u0633\u0641\u0646\u062f"};
        shamsiMonthsEn = new String[]{"Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"};
        shamsiMonthsKur = new String[]{"\u062e\u0627\u06a9\u0647\u200c\u0644\u06ce\u0648\u0647", "\u06af\u0648\u06b5\u0627\u0646", "\u062c\u06c6\u0632\u0647\u200c\u0631\u062f\u0627\u0646", "\u067e\u0648\u0634\u067e\u0647\u200c\u0631", "\u06af\u0647\u200c\u0644\u0627\u0648\u06ce\u0698", "\u062e\u0647\u200c\u0631\u0645\u0627\u0646\u0627\u0646", "\u0631\u0647\u200c\u0632\u0628\u0647\u200c\u0631", "\u06af\u0647\u200c\u06b5\u0627\u0631\u06ce\u0632\u0627\u0646", "\u0633\u0647\u200c\u0631\u0645\u0627\u0648\u0647\u200c\u0632", "\u0628\u0647\u200c\u0641\u0631\u0627\u0646\u0628\u0627\u0631", "\u0631\u06ce\u0628\u0647\u200c\u0646\u062f\u0627\u0646", "\u0631\u0647\u200c\u0634\u0647\u200c\u0645\u0647"};
        shamsiWeekDaysMap = new HashMap();
        shamsiWeekDaysKuMap = new HashMap();
        shamsiWeekDaysMap.put(new Integer(7), shamsiWeekDays[0]);
        shamsiWeekDaysMap.put(new Integer(1), shamsiWeekDays[1]);
        shamsiWeekDaysMap.put(new Integer(2), shamsiWeekDays[2]);
        shamsiWeekDaysMap.put(new Integer(3), shamsiWeekDays[3]);
        shamsiWeekDaysMap.put(new Integer(4), shamsiWeekDays[4]);
        shamsiWeekDaysMap.put(new Integer(5), shamsiWeekDays[5]);
        shamsiWeekDaysMap.put(new Integer(6), shamsiWeekDays[6]);
        shamsiWeekDaysKuMap.put(new Integer(7), shamsiWeekDaysKu[0]);
        shamsiWeekDaysKuMap.put(new Integer(1), shamsiWeekDaysKu[1]);
        shamsiWeekDaysKuMap.put(new Integer(2), shamsiWeekDaysKu[2]);
        shamsiWeekDaysKuMap.put(new Integer(3), shamsiWeekDaysKu[3]);
        shamsiWeekDaysKuMap.put(new Integer(4), shamsiWeekDaysKu[4]);
        shamsiWeekDaysKuMap.put(new Integer(5), shamsiWeekDaysKu[5]);
        shamsiWeekDaysKuMap.put(new Integer(6), shamsiWeekDaysKu[6]);
    }

    public static ShamsiDate dateToShamsi(Date date) {
        int i;
        int i2;
        int i3;
        long dSTSavings = (((long) (TimeZone.getDefault().inDaylightTime(date) ? TimeZone.getDefault().getDSTSavings() : 0)) + date.getTime()) - new Date("01/01/1900").getTime();
        long j = dSTSavings / 1000;
        int i4 = (int) (j % 60);
        j /= 60;
        int i5 = (int) (j % 60);
        int i6 = (int) ((j / 60) % 24);
        long j2 = dSTSavings / 86400000;
        if (j2 <= 78) {
            i = (short) ((int) (((10 + j2) / 30) + 10));
            i2 = (short) ((int) (((j2 + 10) % 30) + 1));
            i3 = 1278;
        } else {
            j = j2 - 78;
            int i7 = 1279;
            while (true) {
                j2 = ((long) (i7 + 11)) % 33;
                i3 = (j2 == 32 || j2 % 4 != 0) ? 0 : 1;
                if (j <= ((long) (i3 + 365))) {
                    break;
                }
                j -= (long) (i3 + 365);
                i7 = (short) (i7 + 1);
            }
            short s;
            if (j <= 186) {
                i2 = (short) ((int) (((j - 1) % 31) + 1));
                s = (short) ((int) (((j - 1) / 31) + 1));
                i3 = i7;
            } else {
                i2 = (short) ((int) ((((j - 1) - 186) % 30) + 1));
                s = (short) ((int) ((((j - 1) - 186) / 30) + 7));
                i3 = i7;
            }
        }
        return new ShamsiDate(i3, i, i2, i6, i5, i4);
    }

    public static int getDaysInMonth(ShamsiDate shamsiDate) {
        if (shamsiDate.getMonth() < 7) {
            return 31;
        }
        if (shamsiDate.getMonth() < 12) {
            return 30;
        }
        Date shamsiToDate = shamsiToDate(new ShamsiDate(shamsiDate.getYear(), shamsiDate.getMonth(), 29));
        Calendar instance = Calendar.getInstance();
        instance.setTime(shamsiToDate);
        instance.add(5, 1);
        return dateToShamsi(instance.getTime()).getMonth() != 12 ? 29 : 30;
    }

    public static String getShamsiMonth(int i) {
        return (i >= 1 || i <= 12) ? shamsiMonths[i - 1] : null;
    }

    public static String getShamsiMonthEn(int i) {
        return (i >= 1 || i <= 12) ? shamsiMonthsEn[i - 1] : null;
    }

    public static String getShamsiMonthKur(int i) {
        return (i >= 1 || i <= 12) ? shamsiMonthsKur[i - 1] : null;
    }

    public static String[] getShamsiMonths() {
        return shamsiMonths;
    }

    public static String getShamsiWeekDay(int i) {
        return (i < 0 || i > 6) ? null : shamsiWeekDays[i];
    }

    public static String[] getShamsiWeekDays() {
        return shamsiWeekDays;
    }

    public static Map<Integer, String> getShamsiWeekDaysKyMap() {
        return shamsiWeekDaysKuMap;
    }

    public static Map<Integer, String> getShamsiWeekDaysMap() {
        return shamsiWeekDaysMap;
    }

    public static Date shamsiToDate(ShamsiDate shamsiDate) {
        int month = shamsiDate.getMonth() >= 7 ? (((shamsiDate.getMonth() - 7) * 30) + 186) + shamsiDate.getDay() : ((shamsiDate.getMonth() - 1) * 31) + shamsiDate.getDay();
        long j = shamsiDate.getYear() == 1278 ? (long) (month - 287) : 79;
        for (int i = 1279; i < shamsiDate.getYear(); i++) {
            long j2 = ((long) (i + 11)) % 33;
            Object obj = (j2 == 32 || j2 % 4 != 0) ? null : 1;
            j = obj;
            if (j == null) {
                j += 365;
            } else{
                j += 366;
            }
        }
        Date date = new Date(new Date("01/01/1900").getTime() + ((((((long) month) + j) - 1) * 86400000) + ((long) ((((shamsiDate.getHour() * 3600) * PointerIconCompat.TYPE_DEFAULT) + ((shamsiDate.getMinute() * 60) * PointerIconCompat.TYPE_DEFAULT)) + (shamsiDate.getSecond() * PointerIconCompat.TYPE_DEFAULT)))));
        return TimeZone.getDefault().inDaylightTime(date) ? new Date(date.getTime() - ((long) TimeZone.getDefault().getDSTSavings())) : date;
    }
}
