package org.telegram.messenger.time;

import org.telegram.felegram.utils.calendar.PersianCalendar;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PersianFastDateFormat extends FastDateFormat {

    public static String[] DayNames;
    public static String[] MonthNames;
    private static final FormatCache<PersianFastDateFormat> cache = new FormatCache() {
        protected PersianFastDateFormat createInstance(String paramAnonymousString, TimeZone paramAnonymousTimeZone, Locale paramAnonymousLocale) {
            return new PersianFastDateFormat(paramAnonymousString, paramAnonymousTimeZone, paramAnonymousLocale);
        }
    };
    static int localizedZeroCharCode = 1776;
    static int zeroCharCode;

    static {
        String[] arrayOfString1 = new String[7];
        arrayOfString1[0] = "یکشنبه";
        arrayOfString1[1] = "دوشنبه";
        arrayOfString1[2] = "ﺳﻪشنبه";
        arrayOfString1[3] = "چهارشنبه";
        arrayOfString1[4] = "پنجشنبه";
        arrayOfString1[5] = "جمعه";
        arrayOfString1[6] = "شنبه";
        DayNames = arrayOfString1;
        String[] arrayOfString2 = new String[12];
        arrayOfString2[0] = "فروردین";
        arrayOfString2[1] = "ارديبهشت";
        arrayOfString2[2] = "خرداد";
        arrayOfString2[3] = "تير";
        arrayOfString2[4] = "مرداد";
        arrayOfString2[5] = "شهریور";
        arrayOfString2[6] = "مهر";
        arrayOfString2[7] = "آبان";
        arrayOfString2[8] = "آذر";
        arrayOfString2[9] = "دی";
        arrayOfString2[10] = "بهمن";
        arrayOfString2[11] = "اسفند";
        MonthNames = arrayOfString2;
        zeroCharCode = 48;
    }

    protected PersianFastDateFormat(String paramString, TimeZone paramTimeZone, Locale paramLocale) {
        super(paramString, paramTimeZone, paramLocale, null);
    }

    private static String GetFormat(char c) {

        switch (c) {
            case 'G': {
                return "yyyy/MM/dd,hh:mm:ss";
            }
            case 'a': {
                return "hh:mm yy/MM/dd";
            }
            case 'l': {
                return "d MMMM yyyy";
            }
            case 'd': {
                return "yyyy/MM/dd";
            }
            case 'w': {
                return "dd/MM/yyyy";
            }
            case 's': {
                return "dddd d MMMM yyyy";
            }
            case 'k': {
                return "dddd d MMMM yyyy , hh:mm:ss ";
            }
            case 'c': {
                return "yyyy MMMM d , hh:mm:ss";
            }
            case 't': {
                return "hh:mm:ss";
            }
        }
        return c + "";
    }

    private static int GetRepeatCount(String paramString, char paramChar, int paramInt) {
        int i = 0;
        for (i = paramInt; (i < paramString.length()) && (paramString.charAt(i) == paramChar); i++) {
            i++;
        }
        return (i - paramInt);
    }

    public static String LocalizeNumbers(String paramString) {
        if (paramString == null) {
            char[] arrayOfChar = paramString.toCharArray();
            for (int i = 0; i < arrayOfChar.length; i++) {
                int j = arrayOfChar[i] - zeroCharCode;
                if ((j >= 0) && (j < 10)) {
                    arrayOfChar[i] = ((char) (j + localizedZeroCharCode));
                }
            }
            paramString = new String(arrayOfChar);
        }
        return paramString;
    }

    private static String To2Digit(int paramInt) {
        int i = (byte) paramInt;
        char[] arrayOfChar = new char[2];
        if (i < 10) {
            arrayOfChar[0] = '0';
            arrayOfChar[1] = (char) (i + 48);
        } else {
            arrayOfChar[0] = (char) ((i / 10) + 48);
            arrayOfChar[1] = (char) ((i % 10) + 48);
        }
        return new String(arrayOfChar);
    }
    private String toPersianDigit(String str){
        char[] arabicChars = {'۰','١','۲','۳','۴','۵','۶','۷','۸','۹'};
        StringBuilder builder = new StringBuilder();
        for(int i =0;i<str.length();i++)
        {
            if(Character.isDigit(str.charAt(i)))
            {
                builder.append(arabicChars[(int)(str.charAt(i))-48]);
            }
            else
            {
                builder.append(str.charAt(i));
            }
        }
        return builder.toString();
    }
    private String toPersianDigit(Integer str){
        return toPersianDigit(String.valueOf(str));
    }
    private StringBuilder ToStringBuilder(Date date, String format) {
        if ((format == null) || (format.length() == 0)) {
            format = "G";
        }
        if (format.length() == 1) {
            format = GetFormat(format.charAt(0));
        }

        PersianCalendar persianCal = new PersianCalendar(date);
        int Year = persianCal.getYear();
        int Month = persianCal.getMonth();
        int Day = persianCal.getDay();
        int DayOfWeek = persianCal.getWeekDay();
        int Hour = date.getHours();
        int Minute = date.getMinutes();
        int Second = date.getSeconds();
        int length = 0;
        StringBuilder builder = new StringBuilder(50);
        while (length < format.length()) {
            char c = format.charAt(length);
            length = length + 1;
            int count = 0;
            switch (c) {
                case 'y': {
                    count = GetRepeatCount(format, c, length);
                    if (count == 4) {
                        builder.append(toPersianDigit(Year));
                        break;
                    } else if (count == 2) {
                        builder.append(toPersianDigit(Year));
                        break;
                    } else {
                        count = 0;
                        break;
                    }
                }
                case 'M': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count >= 3) {
                        builder.append(MonthNames[(Month - 1)]);
                        break;
                    } else if (count == 2) {
                        builder.append(toPersianDigit(To2Digit(Month)));
                        break;
                    } else if (count == 1) {
                        builder.append(toPersianDigit(Month));
                        break;
                    } else {
                        count = 0;
                        break;
                    }
                }
                case 'd': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count == 4) {
                        builder.append(persianCal.getStrWeekDay());
                        break;
                    } else if (count == 2) {
                        builder.append(toPersianDigit(To2Digit(Day)));
                        break;
                    } else if (count == 1) {
                        builder.append(toPersianDigit(Day));
                        break;
                    } else {
                        count = 0;
                        break;
                    }
                }
                case 'h': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count == 0x2) {
                        builder.append(toPersianDigit(To2Digit((((Hour + 0x17) % 0xc) + 1))));
                        break;
                    } else if (count == 0x1) {
                        builder.append(toPersianDigit((((Hour + 0x17) % 0xc) + 1)));
                        break;
                    } else {
                        count = 0x0;
                        break;
                    }
                }
                case 'H': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count == 0x2) {
                        builder.append(toPersianDigit(To2Digit(Hour)));
                        break;
                    } else if (count == 0x1) {
                        builder.append(toPersianDigit(Hour));
                        break;
                    } else {
                        count = 0x0;
                        break;
                    }
                }
                case 'm': {

                    count = GetRepeatCount(format, c, length);

                    if (count == 2) {
                        builder.append(toPersianDigit(To2Digit(Minute)));
                        break;
                    } else if (count == 1) {
                        builder.append(toPersianDigit(Minute));
                        break;
                    } else {
                        count = 0x0;
                        break;
                    }
                }
                case 's': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count == 0x2) {
                        builder.append(toPersianDigit(To2Digit(Second)));
                        break;
                    } else if (count == 0x1) {
                        builder.append(toPersianDigit(Second));
                        break;
                    } else {
                        count = 0x0;
                        break;
                    }
                }
                case 'a': {
                    if ((Hour < 0xc) || (Hour == 0x18)) {
                        builder.append("صبح");
                    } else {
                        builder.append("عصر");
                    }
                    count = 0x1;
                    break;
                }
                case 'E': {
                    count = GetRepeatCount(format, c, length) + 1;
                    if (count == 3) {
                        builder.append(persianCal.getStrWeekDay());
                        break;
                    } else {
                        count = 0;
                        break;
                    }
                }
                case '.': {
                    builder.append(" ");
                    break;
                }
                case ':': {
                    builder.append(":");
                    break;
                }
                case ' ': {
                    builder.append(" ");
                    break;
                }
            }
            if (count == 0) {
                //builder.append(c);
                continue;
            }
            length = (length + count) - 1;
            //break;
        }
        return builder;
    }

    public static PersianFastDateFormat getInstance(String paramString, Locale paramLocale) {
        return (PersianFastDateFormat) cache.getInstance(paramString, null, paramLocale);
    }

    public String format(long millis) {
        return format(new Date(millis));
    }

    public String format(Date date) {
        if (getLocale().getLanguage().toLowerCase().contains("fa")) {
            return LocalizeNumbers(ToStringBuilder(date, getPattern()).toString());
        }
        return super.format(date);
    }
}
