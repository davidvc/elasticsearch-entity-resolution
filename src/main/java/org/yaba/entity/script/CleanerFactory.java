package org.yaba.entity.script;

import no.priv.garshol.duke.Cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.priv.garshol.duke.utils.ObjectUtils.instantiate;
import static org.yaba.entity.script.EntityResolutionConstants.NAME;
import static org.yaba.entity.script.EntityResolutionConstants.PARAMS;

class CleanerFactory {
    private final ObjectFactory objectFactory;

    CleanerFactory() {
        this(new ObjectFactory());
    }

    CleanerFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
    /**
      * . Reads & instantiates cleaners from cleaner class names
      *
      * @param cleanersList
      *           array of cleaners from JSON
      * @return the list of instantiated cleaners
      */
     List<Cleaner> getCleaners(final List<Map<String, String>> cleanersList) {
        List<Cleaner> cleanList = new ArrayList<>();

        for (Map aCleaner : cleanersList) {
           Cleaner cleaner = (Cleaner) instantiate((String) aCleaner.get(NAME));
           objectFactory.setObjectParameters(cleaner, aCleaner.get(PARAMS));
           cleanList.add(cleaner);
        }
        return cleanList;
     }



}
