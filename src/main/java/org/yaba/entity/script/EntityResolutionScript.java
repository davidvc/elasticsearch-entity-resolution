/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yaba.entity.script;

import no.priv.garshol.duke.Cleaner;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.RecordImpl;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractExecutableScript;
import org.elasticsearch.script.LeafSearchScript;
import org.elasticsearch.search.lookup.LeafDocLookup;
import org.elasticsearch.search.lookup.LeafSearchLookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.yaba.entity.script.EntityResolutionConstants.CLEANERS;
import static org.yaba.entity.script.EntityResolutionConstants.COMPARATOR;
import static org.yaba.entity.script.EntityResolutionConstants.FIELDS;
import static org.yaba.entity.script.EntityResolutionConstants.HIGH;
import static org.yaba.entity.script.EntityResolutionConstants.LOW;


/**
 * Entity Resolution Script for Elasticsearch
 *
 * @author Yann Barraud, David Van Couvering
 *
 */
public final class EntityResolutionScript extends AbstractExecutableScript implements LeafSearchScript {
    private final ComparatorFactory comparatorFactory;
    private final CleanerFactory cleanerFactory;
    private final EntityComparator entityComparator;
    private final Record entityToCompare;
    private final Map<String, HashMap<String, Object>> scriptParameters;
    private final LeafSearchLookup leafSearchLookup;

   public EntityResolutionScript(final Map<String, Object> params, LeafSearchLookup leafSearchLookup) {
        this(params, leafSearchLookup, new ComparatorFactory(), new CleanerFactory(), new EntityComparator());
    }

    // TODO - add unit testing with this constructor
    EntityResolutionScript(final Map<String, Object> params,
                           LeafSearchLookup leafSearchLookup,
                           ComparatorFactory comparatorFactory,
                           CleanerFactory cleanerFactory, EntityComparator entityResolver) {
        if (params.get(FIELDS) == null) {
            throw new IllegalArgumentException("Missing the 'fields' parameters");
        }

        this.leafSearchLookup = leafSearchLookup;
        this.comparatorFactory = comparatorFactory;
        this.entityComparator = entityResolver;
        this.cleanerFactory = cleanerFactory;

        List<Map<String, Object>> fields = (List<Map<String, Object>>) params.get(FIELDS);
        scriptParameters = createScriptParameters(fields);
        entityToCompare = createEntityToCompare(fields);
    }

    /**
     * . Computes probability that objects are the same
     *
     * @return float the computed score
     */
    @Override
    public double runAsDouble() {
        Record currentEntity = createCurrentEntity();

        return entityComparator.compareEntities(entityToCompare, currentEntity, scriptParameters);
    }

    @Override
    public void setDocument(int docId) {
        leafSearchLookup.setDocument(docId);

    }

    private Record createCurrentEntity() {
        HashMap<String, Collection<String>> props = new HashMap<>();
        LeafDocLookup doc = doc();
        Collection<String> docKeys = entityToCompare.getProperties();

        for (String key : docKeys) {
            addKeyValueToRecordProperties(props, doc, key);
        }

        return new RecordImpl(props);
    }

    private LeafDocLookup doc() {
        return leafSearchLookup.doc();
    }

    private void addKeyValueToRecordProperties(HashMap<String, Collection<String>> props, LeafDocLookup doc, String key) {
        if (doc.containsKey(key)) {
            String value = (doc.get(key) == null ? "" : getCurrentFieldAsString(doc.get(key)));
            props.put(key, value == null ? Collections.singleton("") : Collections.singleton(value));
        }
    }

    private Record createEntityToCompare(final List<Map<String, Object>> fieldsParams) {
        return new RecordImpl(readAndCleanFields(fieldsParams));
    }

    private Map<String, Collection<String>> readAndCleanFields(List<Map<String, Object>> fields) {
        Map<String, Collection<String>> props = new HashMap<>();

        for (Map<String, Object> value : fields) {
            String field = (String) value.get("field");
            String fieldValue = (String) value.get("value");
            for (Cleaner cl : (ArrayList<Cleaner>) scriptParameters.get(field).get(CLEANERS)) {
                fieldValue = cl.clean(fieldValue);
            }
            props.put(field, Collections.singleton(fieldValue));
        }

        return props;
    }

    private static String getCurrentFieldAsString(final Object field) {
        String result = "";

        if (field instanceof ScriptDocValues.Strings) {
            if (!((ScriptDocValues.Strings) field).isEmpty()) {
                result = ((ScriptDocValues.Strings) field).getValue();
            }
        }

        if (field instanceof ScriptDocValues.Doubles) {
            if (!((ScriptDocValues.Doubles) field).isEmpty()) {
                result = Double.toString(((ScriptDocValues.Doubles) field).getValue());
            }
        }
        if (field instanceof ScriptDocValues.Longs) {
            if (!((ScriptDocValues.Longs) field).isEmpty()) {
                result = Long.toString(((ScriptDocValues.Longs) field).getValue());
            }
        }
        if (field instanceof ScriptDocValues.GeoPoints) {
            if (!((ScriptDocValues.GeoPoints) field).isEmpty()) {
                ScriptDocValues.GeoPoints point = (ScriptDocValues.GeoPoints) field;
                result = String.format(Locale.getDefault(), "%s,%s", point.getLat(), point.getLon());
            }
        }

        return result;
    }

    private Map<String, HashMap<String, Object>> createScriptParameters(final List<Map<String, Object>> fieldParameters) {
        Map<String, HashMap<String, Object>> scriptParameters =  new HashMap<>();
        for (Map<String, Object> fieldParam : fieldParameters) {
            HashMap<String, Object> scriptParameter = new HashMap<>();

            scriptParameter.put(CLEANERS, cleanerFactory.getCleaners((ArrayList<Map<String, String>>) fieldParam.get(CLEANERS)));
            scriptParameter.put(COMPARATOR, comparatorFactory.getComparator(fieldParam));
            scriptParameter.put(HIGH, getDoubleParamValue(fieldParam, HIGH));
            scriptParameter.put(LOW, getDoubleParamValue(fieldParam, LOW));

            String fieldName = (String) fieldParam.get("field");
            scriptParameters.put(fieldName, scriptParameter);
        }

        return scriptParameters;
    }

    private Double getDoubleParamValue(Map<String, Object> params, String paramName) {
        Double maxValue = 0.0;
        if (params.get(paramName) != null) {
            maxValue = (Double) params.get(paramName);
        }
        return maxValue;
    }
}
