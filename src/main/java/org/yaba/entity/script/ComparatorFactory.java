package org.yaba.entity.script;

import no.priv.garshol.duke.Comparator;
import no.priv.garshol.duke.comparators.Levenshtein;
import no.priv.garshol.duke.comparators.WeightedLevenshtein;
import no.priv.garshol.duke.comparators.WeightedLevenshtein.DefaultWeightEstimator;

import java.util.Map;

import static no.priv.garshol.duke.utils.ObjectUtils.instantiate;
import static no.priv.garshol.duke.utils.ObjectUtils.setBeanProperty;
import static org.yaba.entity.script.EntityResolutionConstants.COMPARATOR;
import static org.yaba.entity.script.EntityResolutionConstants.NAME;
import static org.yaba.entity.script.EntityResolutionConstants.OBJECTS;
import static org.yaba.entity.script.EntityResolutionConstants.PARAMS;

class ComparatorFactory {
    private static final String WEIGHTED_LEVENSHTEIN = "no.priv.garshol.duke.comparators.WeightedLevenshtein";
    private final ObjectFactory objectFactory;

    ComparatorFactory() {
        this(new ObjectFactory());
    }

    ComparatorFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    Comparator getComparator(final Map<String, Object> value) {
       Map<String, Object> compEntity = (Map<String, Object>) value.get(COMPARATOR);


       if (compEntity.get(NAME).equals(WEIGHTED_LEVENSHTEIN)) {
          return createWeightedLevensteinComparator(compEntity);
       }

       return createComparatorByName(compEntity, getComparatorName(compEntity));
    }

    private String getComparatorName(Map<String, Object> compEntity) {
        return compEntity.get(NAME) == null ? Levenshtein.class.getName() : (String)compEntity.get(NAME);
    }

    private Comparator createComparatorByName(Map<String, Object> compEntity, String comparatorName) {
         Comparator comparator;
         comparator = (Comparator) instantiate(comparatorName);
         setObjectParameters(comparator, compEntity.get(PARAMS));

         objectFactory.setObjects(comparator, compEntity.get(OBJECTS));
         return comparator;
     }

     private Comparator createWeightedLevensteinComparator(Map<String, Object> compEntity) {
         Comparator comparator;WeightedLevenshtein weightedLevenshtein = new WeightedLevenshtein();
         DefaultWeightEstimator weightEstimator = new DefaultWeightEstimator();
         setObjectParameters(weightEstimator, compEntity.get(PARAMS));
         weightedLevenshtein.setEstimator(weightEstimator);
         comparator = weightedLevenshtein;
         return comparator;
     }

    private void setObjectParameters(Object anObject, Object params) {
       if (params != null) {
          Map<String, String> paramsMap = (Map<String, String>) params;
          for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
             setBeanProperty(anObject, entry.getKey(), entry.getValue(), null);
          }
       }
    }


}
