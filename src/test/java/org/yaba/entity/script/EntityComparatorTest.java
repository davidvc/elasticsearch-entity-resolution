package org.yaba.entity.script;

import no.priv.garshol.duke.Cleaner;
import no.priv.garshol.duke.Comparator;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.cleaners.LowerCaseNormalizeCleaner;
import no.priv.garshol.duke.cleaners.TrimCleaner;
import no.priv.garshol.duke.comparators.ExactComparator;
import no.priv.garshol.duke.comparators.JaroWinkler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaba.entity.cleaners.DateCleaner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.yaba.entity.script.EntityResolutionConstants.CLEANERS;
import static org.yaba.entity.script.EntityResolutionConstants.COMPARATOR;
import static org.yaba.entity.script.EntityResolutionConstants.HIGH;
import static org.yaba.entity.script.EntityResolutionConstants.LOW;

@RunWith(MockitoJUnitRunner.class)
public class EntityComparatorTest {
    private EntityComparator underTest;

    @Mock private Record recordOne;
    @Mock private Record recordTwo;

    private static final String TEXT_FIELD = "textField";
    private static final String DATE_FIELD = "dateField";

    private static final Double HIGH_MARK = 0.95;
    private static final Double LOW_MARK = 0.05;

    private Collection<String> properties = Arrays.asList(TEXT_FIELD, DATE_FIELD);
    private Map<String, HashMap<String, Object>> params = new HashMap<>();

    @Before
    public void setup() {
        underTest = new EntityComparator();

        when(recordOne.getProperties()).thenReturn(properties);
        when(recordTwo.getProperties()).thenReturn(properties);
        setCleaners(TEXT_FIELD, new LowerCaseNormalizeCleaner(), new TrimCleaner());
        setCleaners(DATE_FIELD, new DateCleaner());
        setComparator(TEXT_FIELD, new JaroWinkler());
        setComparator(DATE_FIELD, new ExactComparator());

        addParam(TEXT_FIELD, HIGH, HIGH_MARK);
        addParam(TEXT_FIELD, LOW, LOW_MARK);
        addParam(DATE_FIELD, HIGH, HIGH_MARK);
        addParam(DATE_FIELD, LOW, LOW_MARK);
    }

    @Test
    public void recordWithExactMismatchShouldScoreWayLowerThanRecordWithExactMatch() {
        double exactProbability = compare("John", "2014-02-12", "John", "2014-02-12");
        double mismatchProbability = compare("John", "2014-02-12", "John", "2017-08-09");

        assertTrue(exactProbability - mismatchProbability > 0.4);
    }

    @Test
    public void textWithMisspellShouldScoreLowerThanNoMisspell() {
        double exactProbability = compare("John", "2014-02-12", "John", "2014-02-12");
        double mismatchProbability = compare("John", "2014-02-12", "Jhon", "2014-02-12");

        assertTrue(exactProbability > mismatchProbability);

    }

    private double compare(String textOne, String dateOne, String textTwo, String dateTwo) {
        setupRecord(recordOne, textOne, dateOne);
        setupRecord(recordTwo, textTwo, dateTwo);

        return underTest.compareEntities(recordOne, recordTwo, params);
    }

    private void setupRecord(Record record, String textField, String dateField) {
        setValue(record, TEXT_FIELD, textField);
        setValue(record, DATE_FIELD, dateField);
    }


    private void setValue(Record record, String fieldName, String value) {
        when(record.getValues(fieldName)).thenReturn(Collections.singletonList(value));

    }

    private void setComparator(String fieldName, Comparator comparator) {
        addParam(fieldName, COMPARATOR, comparator);
    }

    private void setCleaners(String fieldName, Cleaner ... cleaners) {
        addParam(fieldName, CLEANERS, Arrays.asList(cleaners));
    }

    private void addParam(String fieldName, String paramName, Object value) {
        HashMap<String, Object> fieldParams = params.computeIfAbsent(fieldName, k -> new HashMap<>());

        fieldParams.put(paramName, value);
    }


}
