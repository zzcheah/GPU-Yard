package fyp.gy.main_server.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static fyp.gy.common.constant.GyConstant.DATE_TIME_FORMAT;

public class DateTimeUtil {

    public static String getCurrentTime() {
        Date currentTime = new Date(System.currentTimeMillis());
        return new SimpleDateFormat(DATE_TIME_FORMAT, Locale.ENGLISH).format(currentTime);
    }
}
