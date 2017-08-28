package org.yaba.entity.cleaners;

import no.priv.garshol.duke.Cleaner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Normalize various date formats to a single normalized date
 */
public class DateCleaner implements Cleaner {
    private static final SimpleDateFormat[]  formats = new SimpleDateFormat[] {
            new SimpleDateFormat("dd MMM yyyy"),
            new SimpleDateFormat("yyyy-MM-dd")
    };

    @Override
    public String clean(String str) {
        // It could already be long representation of the date
        if (isLong(str)) {
            return str;
        }

        for (SimpleDateFormat format : formats) {
            try {
                Date date = format.parse(str);
                return Long.toString(date.getTime());
            } catch (ParseException e) {
                // Keep trying
            }
        }

        return "";
        //throw new RuntimeException("Date field '" + str + "' not in a recognized format");
    }

    private boolean isLong(String str) {
        try {
            Long.parseLong(str);

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }
}
