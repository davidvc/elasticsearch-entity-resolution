package org.yaba.entity.script;

import no.priv.garshol.duke.Cleaner;
import no.priv.garshol.duke.Comparator;
import no.priv.garshol.duke.Record;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.priv.garshol.duke.utils.Utils.computeBayes;
import static org.yaba.entity.script.EntityResolutionConstants.AVERAGE_SCORE;
import static org.yaba.entity.script.EntityResolutionConstants.CLEANERS;
import static org.yaba.entity.script.EntityResolutionConstants.COMPARATOR;
import static org.yaba.entity.script.EntityResolutionConstants.HIGH;
import static org.yaba.entity.script.EntityResolutionConstants.LOW;

public class EntityComparator {
    /**
     * Compares two entities and returns the probability that they represent the same real-world entity.
     *
     * @return Bayesian probability
     */
    double compareEntities(final Record entityOne, final Record entityTwo, final Map<String, HashMap<String, Object>> params) {
       double probability = AVERAGE_SCORE;

       for (String propname : entityOne.getProperties()) {
          Collection<String> valuesOne = entityOne.getValues(propname);
          Collection<String> valuesTwo = entityTwo.getValues(propname);

          if (stringListIsEmpty(valuesOne) || stringListIsEmpty(valuesTwo)) {
             continue; // no values to compare, so skip
          }

          Comparator comp = (Comparator) params.get(propname).get(COMPARATOR);
          ArrayList<Cleaner> cleanersList = (ArrayList<Cleaner>) params.get(propname).get(CLEANERS);

          Double max = (Double) params.get(propname).get(HIGH);
          Double min = (Double) params.get(propname).get(LOW);

          double high = computeProbability(valuesOne, valuesTwo, comp, cleanersList, max, min);
          probability = computeBayes(probability, high);
       }
       return probability;
    }

   private double computeProbability(Collection<String> valuesOne, Collection<String> valuesTwo,
                                                Comparator comparator, List<Cleaner> cleanersList, Double max, Double min) {
     double high = 0.0;
     for (String valueOne : valuesOne) {
        if (StringUtils.isEmpty(valueOne)) {
           continue;
        }

        valueTwoLoop: for (String valueTwo : valuesTwo) {
           if (valueTwo.equals("")) {
              continue;
           }

           for (Cleaner cleaner : cleanersList) {
              valueTwo = cleaner.clean(valueTwo);
              if ((valueTwo == null) || valueTwo.equals("")) {
                 continue valueTwoLoop;
              }
           }
           double p = compare(valueOne, valueTwo, max, min, comparator);
           high = Math.max(high, p);
        }
     }
     return high;
  }

    /**
     * Returns the probability that the records stringOne and stringTwo represent the same entity
     *
     * @return the computed probability
     */
    private double compare(final String stringOne, final String stringTwo, final double high, final double low,
                      final Comparator comparator) {

       if (comparator == null) {
          return AVERAGE_SCORE; // we ignore properties with no comparator
       }

       double sim = comparator.compare(stringOne, stringTwo);
       if (sim < AVERAGE_SCORE) {
          return low;
       } else {
          return ((high - AVERAGE_SCORE) * (sim * sim)) + AVERAGE_SCORE;
       }
    }


    private boolean stringListIsEmpty(Collection<String> stringList) {
       if (stringList.isEmpty()) { return true; }
        Boolean valueOneIsEmpty = true;
        for (String value : stringList) {
           if (!StringUtils.isEmpty(value)) {
              valueOneIsEmpty = false;
              break;
           }
        }
        return valueOneIsEmpty;
    }
}
