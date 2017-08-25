package org.yaba.entity.script;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.script.CompiledScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.LeafSearchScript;
import org.elasticsearch.script.ScriptEngineService;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.util.Map;

/**
 * This is ElasticSearch's new way of registering plugins.  What a nightmare.  And very little documentation
 * or examples.  Best I could find was https://www.elastic.co/guide/en/elasticsearch/reference/5.5/modules-scripting-engine.html.
 * Pretty obtuse...
 */
public class EntityResolutionScriptEngine implements ScriptEngineService {

    @Override
    public String getType() {
        return "castlight";
    }

    @Override
    public String getExtension() {
        return "castlight";
    }

    @Override
    public Object compile(String scriptName, String scriptSource, Map<String, String> compileParams) {
        if (!("entity-resolver".equals(scriptName)))  {
            throw new IllegalArgumentException("Unrecognized script name " + scriptName);
        }

        return null;
    }

    @Override
    public ExecutableScript executable(CompiledScript compiledScript, Map<String, Object> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchScript search(CompiledScript compiledScript, SearchLookup searchLookup, Map<String, Object> params) {
        return new SearchScript() {
            @Override
            public LeafSearchScript getLeafSearchScript(LeafReaderContext leafReaderContext) throws IOException {
                return new EntityResolutionScript(params, searchLookup.getLeafSearchLookup(leafReaderContext));
            }

            @Override
            public boolean needsScores() {
                return false;
            }
        };
    }

    @Override
    public boolean isInlineScriptEnabled() {
        return true;
    }

    @Override
    public void close() throws IOException { }
}
