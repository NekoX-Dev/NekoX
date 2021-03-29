package org.telegram.freetux.utils.shamsicalendar;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.io.*;

public class ShamsiDate implements Comparable<ShamsiDate> {
    public static final Pattern SHAMSI_DATE_PATTERN;
    public static final Pattern TIME_PATTERN;
    private int day;
    private int hour;
    private int minute;
    private int month;
    private int second;
    private int smallYear;
    private int year;

    static {
        SHAMSI_DATE_PATTERN = Pattern.compile("0*1[34]\\d\\d[/\\\\-]0*([1-9]|1[012])[/\\\\-]0*([1-9]|[12]\\d|3[01])");
        TIME_PATTERN = Pattern.compile("0*(|[12])\\d:0*(|[1-5])\\d(:(0*(|[1-5])\\d)|)");
    }

    private ShamsiDate() {
    }

    public ShamsiDate(int i, int i2, int i3) {
        this(i, i2, i3, 0, 0, 0);
    }

    public ShamsiDate(int i, int i2, int i3, int i4, int i5) {
        this(i, i2, i3, i4, i5, 0);
    }

    public ShamsiDate(int i, int i2, int i3, int i4, int i5, int i6) {
        setYear(i);
        setMonth(i2);
        setDay(i3);
        setHour(i4);
        setMinute(i5);
        setSecond(i6);
    }

    private static void isDateValid(ShamsiDate shamsiDate) throws Exception {
        try {
        ShamsiCalendar.dateToShamsi(ShamsiCalendar.shamsiToDate(shamsiDate)).toDateString().equals(shamsiDate.toDateString());
        } catch (Throwable e) {
            throw new ShamsiDateIllegalFormatException("Invalid shamsi date : " + shamsiDate);
        }
    }

    public static ShamsiDate parseDate(String str) throws Exception {
        if (str != null && TtmlNode.ANONYMOUS_REGION_ID.equals(str)) throws Exception {
            throw new ShamsiDateIllegalFormatException("Bad time format: <null>");
        } else if (SHAMSI_DATE_PATTERN.matcher(str).matches()) {
            ShamsiDate shamsiDate = new ShamsiDate();
            try {
                StringTokenizer stringTokenizer = new StringTokenizer(str, " -/\\");
                shamsiDate.setYear(Integer.parseInt(stringTokenizer.nextToken()));
                shamsiDate.setMonth(Integer.parseInt(stringTokenizer.nextToken()));
                shamsiDate.setDay(Integer.parseInt(stringTokenizer.nextToken()));
                isDateValid(shamsiDate);
                return shamsiDate;
            } catch (Throwable e) {
                throw new ShamsiDateIllegalFormatException("Bad shamsi date format: " + str, e);
            }
        } else {
            throw new ShamsiDateIllegalFormatException("Bad shamsi date format: " + str);
        }
    }

    public static ShamsiDate parseTime(String str) throws Exception {
        if (str != null && TtmlNode.ANONYMOUS_REGION_ID.equals(str)) {
            throw new ShamsiDateIllegalFormatException("Bad time format: <null>");
        } else if (TIME_PATTERN.matcher(str).matches()) {
            try {
                ShamsiDate shamsiDate = new ShamsiDate();
                StringTokenizer stringTokenizer = new StringTokenizer(str, " :-/");
                shamsiDate.setHour(Integer.parseInt(stringTokenizer.nextToken()));
                shamsiDate.setMinute(Integer.parseInt(stringTokenizer.nextToken()));
                if (stringTokenizer.hasMoreTokens()) {
                    shamsiDate.setSecond(Integer.parseInt(stringTokenizer.nextToken()));
                }
                return shamsiDate;
            } catch (Throwable e) {
                throw new ShamsiDateIllegalFormatException("Bad time format: " + str, e);
            }
        } else {
            throw new ShamsiDateIllegalFormatException("Bad time format: " + str);
        }
    }

    private void setDay(int i) throws Exception {
        if (i < 1 || i > 31) {
            throw new IllegalArgumentException("Day must be between 1 and 31");
        }
        this.day = i;
    }

    private void setHour(int i) {
        if (i < 0 || i >= 24) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        this.hour = i;
    }

    private void setMinute(int i) {
        if (i < 0 || i >= 60) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
        this.minute = i;
    }

    private void setMonth(int i) {
        if (i < 1 || i > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        this.month = i;
    }

    private void setSecond(int i) {
        if (i < 0 || i >= 60) {
            throw new IllegalArgumentException("Second must be between 0 and 59");
        }
        this.second = i;
    }

    private void setSmallYear(int i) {
        if (i >= 0) {
            this.smallYear = i;
            return;
        }
        throw new IllegalArgumentException("Small Year must be positive");
    }

    private void setYear(int i) {
        if (i >= 0) {
            this.year = i;
            setSmallYear(i % 100);
            return;
        }
        throw new IllegalArgumentException("Year must be positive");
    }

    public void add(int i, int i2) {
        int i3 = 30;
        if (i == 2) {
            this.month += i2;
            if (this.month <= 0) {
                this.month += 12;
                this.year--;
            } else if (this.month > 12) {
                this.month -= 12;
                this.year++;
            }
            if (this.month < 7 && this.day > 31) {
                this.day = 31;
            } else if (this.month < 12 && this.day > 30) {
                this.day = 30;
            } else if (this.month == 12 && this.day > 29) {
                Date shamsiToDate = ShamsiCalendar.shamsiToDate(new ShamsiDate(this.year, this.month, 29));
                Calendar instance = Calendar.getInstance();
                instance.setTime(shamsiToDate);
                instance.add(5, 1);
                if (ShamsiCalendar.dateToShamsi(instance.getTime()).getMonth() != 12) {
                    i3 = 29;
                }
                if (this.day > i3) {
                    this.day = 29;
                }
            }
        } else if (i == 1) {
            this.year += i2;
            this.year = this.year < 0 ? 0 : this.year;
        } else {
            Calendar instance2 = Calendar.getInstance();
            instance2.setTime(ShamsiCalendar.shamsiToDate(this));
            instance2.add(i, i2);
            ShamsiDate dateToShamsi = ShamsiCalendar.dateToShamsi(instance2.getTime());
            this.year = dateToShamsi.getYear();
            this.month = dateToShamsi.getMonth();
            this.day = dateToShamsi.getDay();
        }
    }

    public int compareTo(ShamsiDate shamsiDate) {
        if (shamsiDate != null) {
            return this.year != shamsiDate.year ? this.year - shamsiDate.year : this.month != shamsiDate.month ? this.month - shamsiDate.month : this.day != shamsiDate.day ? this.day - shamsiDate.day : this.hour != shamsiDate.hour ? this.hour - shamsiDate.hour : this.minute != shamsiDate.minute ? this.minute - shamsiDate.minute : this.second != shamsiDate.second ? this.second - shamsiDate.second : 0;
        } else {
            throw new IllegalArgumentException("Object must not be null and must be of type ShamsiDate: " + shamsiDate);
        }
    }

    public int getDay() {
        return this.day;
    }

    public int getDayInYear() {
        return this.month < 7 ? ((this.month - 1) * 31) + this.day : this.month <= 12 ? (((this.month - 7) * 30) + this.day) + 186 : 0;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getMonth() {
        return this.month;
    }

    public int getSecond() {
        return this.second;
    }

    public int getSmallYear() {
        return this.smallYear;
    }

    public int getWeekOfYear() {
        int i = 0;
        Date shamsiToDate = ShamsiCalendar.shamsiToDate(new ShamsiDate(this.year, 1, 1));
        int dayInYear = getDayInYear();
        Calendar instance = Calendar.getInstance();
        instance.setTime(shamsiToDate);
        int i2 = instance.get(7);
        int i3 = 0;
        while (i2 != 7) {
            i2 = i3 + 1;
            instance.add(6, 1);
            int i4 = i2;
            i2 = instance.get(7);
            i3 = i4;
        }
        i2 = i3 > 0 ? 1 : 0;
        if (dayInYear <= i3) {
            return i2;
        }
        i3 = dayInYear - i3;
        dayInYear = i3 / 7;
        if (i3 % 7 != 0) {
            i = 1;
        }
        return (i + dayInYear) + i2;
    }

    public int getYear() {
        return this.year;
    }

    public String toDateString() {
        return toDateString('/');
    }

    public String toDateString(char c) {
        return String.valueOf(getYear()) + c + getMonth() + c + getDay();
    }

    public String toReverseDateString() {
        return toReverseDateString('/');
    }

    public String toReverseDateString(char c) {
        return String.valueOf(getDay()) + c + getMonth() + c + getYear();
    }

    public String toString() {
        return getYear() + "/" + getMonth() + "/" + getDay() + " " + getHour() + ":" + getMinute() + ":" + getSecond();
    }

    public String toTimeString() {
        return toTimeString(':');
    }

    public String toTimeString(char c) {
        return toTimeString(c, false);
    }

    public String toTimeString(char c, boolean z) {
        String str = (String.valueOf(getHour()) + c) + (getMinute() > 9 ? TtmlNode.ANONYMOUS_REGION_ID : "0") + getMinute();
        if (!z) {
            return str;
        }
        return str + c + (getSecond() > 9 ? TtmlNode.ANONYMOUS_REGION_ID : "0") + getSecond();
    }

    public String toTimeString(boolean z) {
        return toTimeString(':', z);
    }
}
