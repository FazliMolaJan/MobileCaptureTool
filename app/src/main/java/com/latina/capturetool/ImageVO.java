package com.latina.capturetool;
import java.util.Calendar;

/**
 * MobileCaptureTool
 * Class: ImageVO
 * Created by Yoon on 2018-11-06.
 * <p>
 * Description:
 */
public class ImageVO {
    String path;
    int year;
    int month;
    int day;

    public ImageVO(String path, long modified) {
        this.path = path;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(modified);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
    }
}
