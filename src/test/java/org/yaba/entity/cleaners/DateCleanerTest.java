package org.yaba.entity.cleaners;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

public class DateCleanerTest {
    private DateCleaner underTest = new DateCleaner();

    @Test
    public void returnsLongAsItself() {
        String originalValue = "1390377600000";
        assertEquals(originalValue, underTest.clean(originalValue));
    }

    @Test
    public void invalidValueReturnsEmptyString() {
        assertEquals("", underTest.clean("blahblah"));
    }

    @Test
    public void returnsLongValueOfValidDateFormat() throws Exception {
        SimpleDateFormat[] formats = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("dd MMM yyyy")
        };

        String[] validDates = new String[] {
                "2014-12-02",
                "02 Apr 1964"
        };

        for (int i = 0 ; i < formats.length ; i++) {
            SimpleDateFormat format = formats[i];
            String validDate = validDates[i];

            String expected = Long.toString(format.parse(validDate).getTime());
            assertEquals(expected, underTest.clean(validDate));
        }

    }
}
