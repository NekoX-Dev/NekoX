package org.telegram.freetux.utils.calendar;

import java.util.Calendar;
import java.util.Date;

public class PersianCalendar {

    private int WeekDay;
    int[] buf1;
    int[] buf2;
    Calendar calendar;
    private int day;
    private int gregorianDate;
    private int gregorianMonth;
    private int gregorianYear;
    int ld;
    private int month;
    String[] monthNames;
    String strMonth;
    String strWeekDay;
    String[] weekDayNames;
    private int year;

    public PersianCalendar()
    {
        String[] arrayOfString1 = new String[7];
        arrayOfString1[0] = "شنبه";
        arrayOfString1[1] = "یکشنبه";
        arrayOfString1[2] = "دوشنبه";
        arrayOfString1[3] = "سه شنبه";
        arrayOfString1[4] = "چهارشنبه";
        arrayOfString1[5] = "پنج شنبه";
        arrayOfString1[6] = "جمعه";
        this.weekDayNames = arrayOfString1;
        String[] arrayOfString2 = new String[12];
        arrayOfString2[0] = "فروردین";
        arrayOfString2[1] = "اردیبهشت";
        arrayOfString2[2] = "خرداد";
        arrayOfString2[3] = "تیر";
        arrayOfString2[4] = "مرداد";
        arrayOfString2[5] = "شهریور";
        arrayOfString2[6] = "مهر";
        arrayOfString2[7] = "آبان";
        arrayOfString2[8] = "آذر";
        arrayOfString2[9] = "دی";
        arrayOfString2[10] = "بهمن";
        arrayOfString2[11] = "اسفند";
        this.monthNames = arrayOfString2;
        this.strWeekDay = "";
        this.strMonth = "";
        this.calendar = Calendar.getInstance();
        this.gregorianYear = this.calendar.get(Calendar.YEAR);
        this.gregorianMonth = (1 + this.calendar.get(Calendar.MONTH));
        this.gregorianDate = this.calendar.get(Calendar.DAY_OF_MONTH);
        this.WeekDay = this.calendar.get(Calendar.DAY_OF_WEEK);
        int[] arrayOfInt1 = new int[12];
        arrayOfInt1[0] = 0;
        arrayOfInt1[1] = 31;
        arrayOfInt1[2] = 59;
        arrayOfInt1[3] = 90;
        arrayOfInt1[4] = 120;
        arrayOfInt1[5] = 151;
        arrayOfInt1[6] = 181;
        arrayOfInt1[7] = 212;
        arrayOfInt1[8] = 243;
        arrayOfInt1[9] = 273;
        arrayOfInt1[10] = 304;
        arrayOfInt1[11] = 334;
        this.buf1 = arrayOfInt1;
        int[] arrayOfInt2 = new int[12];
        arrayOfInt2[0] = 0;
        arrayOfInt2[1] = 31;
        arrayOfInt2[2] = 60;
        arrayOfInt2[3] = 91;
        arrayOfInt2[4] = 121;
        arrayOfInt2[5] = 152;
        arrayOfInt2[6] = 182;
        arrayOfInt2[7] = 213;
        arrayOfInt2[8] = 244;
        arrayOfInt2[9] = 274;
        arrayOfInt2[10] = 305;
        arrayOfInt2[11] = 335;
        this.buf2 = arrayOfInt2;
        Date localDate = new Date();
        this.calendar.setTime(localDate);
        toPersian(localDate);
    }

    public PersianCalendar(Date paramDate) {
        String[] arrayOfString1 = new String[7];
        arrayOfString1[0] = "شنبه";
        arrayOfString1[1] = "یکشنبه";
        arrayOfString1[2] = "دوشنبه";
        arrayOfString1[3] = "سه شنبه";
        arrayOfString1[4] = "چهارشنبه";
        arrayOfString1[5] = "پنج شنبه";
        arrayOfString1[6] = "جمعه";
        this.weekDayNames = arrayOfString1;
        String[] arrayOfString2 = new String[12];
        arrayOfString2[0] = "فروردین";
        arrayOfString2[1] = "اردیبهشت";
        arrayOfString2[2] = "خرداد";
        arrayOfString2[3] = "تیر";
        arrayOfString2[4] = "مرداد";
        arrayOfString2[5] = "شهریور";
        arrayOfString2[6] = "مهر";
        arrayOfString2[7] = "آبان";
        arrayOfString2[8] = "آذر";
        arrayOfString2[9] = "دی";
        arrayOfString2[10] = "بهمن";
        arrayOfString2[11] = "اسفند";
        this.monthNames = arrayOfString2;
        this.strWeekDay = "";
        this.strMonth = "";
        this.calendar = Calendar.getInstance();
        /*
        this.gregorianYear = this.calendar.get(Calendar.YEAR);
        this.gregorianMonth = (1 + this.calendar.get(Calendar.MONTH));
        this.gregorianDate = this.calendar.get(Calendar.DAY_OF_MONTH);
        this.WeekDay = this.calendar.get(Calendar.DAY_OF_WEEK);
        */
        int[] arrayOfInt1 = new int[12];
        arrayOfInt1[0] = 0;
        arrayOfInt1[1] = 31;
        arrayOfInt1[2] = 59;
        arrayOfInt1[3] = 90;
        arrayOfInt1[4] = 120;
        arrayOfInt1[5] = 151;
        arrayOfInt1[6] = 181;
        arrayOfInt1[7] = 212;
        arrayOfInt1[8] = 243;
        arrayOfInt1[9] = 273;
        arrayOfInt1[10] = 304;
        arrayOfInt1[11] = 334;
        this.buf1 = arrayOfInt1;
        int[] arrayOfInt2 = new int[12];
        arrayOfInt2[0] = 0;
        arrayOfInt2[1] = 31;
        arrayOfInt2[2] = 60;
        arrayOfInt2[3] = 91;
        arrayOfInt2[4] = 121;
        arrayOfInt2[5] = 152;
        arrayOfInt2[6] = 182;
        arrayOfInt2[7] = 213;
        arrayOfInt2[8] = 244;
        arrayOfInt2[9] = 274;
        arrayOfInt2[10] = 305;
        arrayOfInt2[11] = 335;
        this.buf2 = arrayOfInt2;
        this.calendar.setTime(paramDate);
        this.gregorianYear = this.calendar.get(Calendar.YEAR);
        this.gregorianMonth = (1 + this.calendar.get(Calendar.MONTH));
        this.gregorianDate = this.calendar.get(Calendar.DAY_OF_MONTH);
        this.WeekDay = this.calendar.get(Calendar.DAY_OF_WEEK);
        toPersian(paramDate);
    }

    private void func1() {
        day = buf1[gregorianMonth - 1] + gregorianDate;
        if (day > 79){
            day = day - 79;
            if (day <= 186) {
                int day2 = day;
                month = (day2 / 31) + 1;
                day = (day2 % 31);
                if(day2 % 31 == 0){
                    month--;
                    day = 31;
                }
                year = gregorianYear - 621;
            }
            else {
                int day2 = day - 186;
                month = (day2 / 30) + 7;
                day = (day2 % 30);
                if(day2 % 30 == 0){
                    month = (day2 / 30) + 6;
                    day = 30;
                }
                year = gregorianYear - 621;
            }
        }
        else{
            ld = gregorianYear > 1996 && gregorianYear % 4 == 1 ? 11 : 10 ;
            int day2 = day + ld;
            month = (day2 / 30) + 10;
            day = (day2 % 30);
            if(day2 % 30 == 0) {
                month--;
                day = 30;
            }
            year = gregorianYear - 622;
        }
    }

    private void func2() {
        day = buf2[gregorianMonth - 1] + gregorianDate;
        ld = gregorianYear >= 1996 ? 79 : 80 ;
        if (day > ld) {
            day = day - ld;
            if (day <= 186) {
                int day2 = day;
                month = (day2 / 31) + 1;
                day = (day2 % 31);
                if(day2 % 31 == 0){
                    month--;
                    day = 31;
                }
                year = gregorianYear - 621;
            } else {
                int day2 = day - 186;
                month = (day2 / 30) + 7;
                day = (day2 % 30);
                if(day2 % 30 == 0 ){
                    month--;
                    day = 30;
                }
                year = gregorianYear - 621;
            }
        }
        else {
            int day2 = day + 10;
            month = (day2 / 30) + 10;
            day = (day2 % 30);
            if(day2 % 30==0){
                month--;
                day = 30;
            }
            year = gregorianYear - 622;
        }

    }

    private void toPersian(Date gregorianDate)
    {
        if ((gregorianYear % 4) != 0)
            func1();
        else
            func2();
        strMonth = monthNames[month-1];
        if(WeekDay>6) {
            WeekDay = WeekDay - 1;
        }
        strWeekDay = weekDayNames[WeekDay];
    }

    public int getDay()
    {
        return this.day;
    }

    public int getMonth()
    {
        return this.month;
    }

    public String getStrMonth()
    {
        return this.strMonth;
    }

    public String getStrWeekDay()
    {
        return this.strWeekDay;
    }

    public int getWeekDay()
    {
        return this.WeekDay;
    }

    public int getYear()
    {
        return this.year;
    }
}
