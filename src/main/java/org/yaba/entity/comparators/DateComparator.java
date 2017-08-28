package org.yaba.entity.comparators;

import no.priv.garshol.duke.Comparator;

public class DateComparator implements Comparator {
    @Override
    public boolean isTokenized() {
        return false;
    }

    @Override
    public double compare(String value1, String value2) {
        return value1.equals(value2)?1.0D:0.0D;
    }
}
