package org.yaba.entity.script;

import java.util.HashMap;
import java.util.Map;

import static no.priv.garshol.duke.utils.ObjectUtils.instantiate;
import static no.priv.garshol.duke.utils.ObjectUtils.setBeanProperty;

public class ObjectFactory {
    /**
     * Sets params for cleaners or comparators
     *
     * @param anObject
     *           the object to parametrize
     * @param params
     *           params list
     */
    void setObjectParameters(Object anObject, Object params) {
       if (params != null) {
          Map<String, String> paramsMap = (Map<String, String>) params;
          for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
             setBeanProperty(anObject, entry.getKey(), entry.getValue(), null);
          }
       }
    }

    static void setObjects(Object anObject, Object objects) {
       if (objects != null) {
          HashMap<String, Object> list = new HashMap<>();
          Map<String, HashMap> paramsMap = (Map<String, HashMap>) objects;

          for (Map.Entry<String, HashMap> entry : paramsMap.entrySet()) {
             HashMap<String, String> objectParam = entry.getValue();
             String klass = objectParam.get("class");
             String name = objectParam.get("name");
             list.put(klass, instantiate(klass));
             setBeanProperty(anObject, name, klass, list);
          }
       }
    }
}
