package org.yaba.entity.plugin;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.ScriptEngineService;
import org.yaba.entity.script.EntityResolutionScriptEngine;

public class EntityResolutionPlugin extends Plugin implements ScriptPlugin {
    @Override
    public ScriptEngineService getScriptEngineService(Settings settings) {
        return new EntityResolutionScriptEngine();
    }
}
