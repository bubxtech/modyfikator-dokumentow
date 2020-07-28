package pl.pentacomp.ModyfikatorDokumentow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import pl.pentacomp.ModyfikatorDokumentow.utils.JsonObjectToMapTranslator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationRulesService {

    private final FileLoaderService fileLoaderService;

    public Map<String, String> getFileNameModificationRules(String configFilePath) {
        final Optional<JSONObject> jsonObject = fileLoaderService.getFileNameModificationRulesJsonFile(configFilePath);
        return jsonObject
                .map(JsonObjectToMapTranslator::translate)
                .orElseGet(this::logErrorAndReturnEmptyMap);
    }

    public Map<String, String> getFileContentModificationRules(String configFilePath) {
        final Optional<JSONObject> jsonObject = fileLoaderService.getFileContentModificationRulesJsonFile(configFilePath);
        return jsonObject
                .map(JsonObjectToMapTranslator::translate)
                .orElseGet(this::logErrorAndReturnEmptyMap);
    }

    private Map<String, String> logErrorAndReturnEmptyMap() {
        log.error("There was an error during taking configuration file from JSON file!");
        return Collections.emptyMap();
    }
}
