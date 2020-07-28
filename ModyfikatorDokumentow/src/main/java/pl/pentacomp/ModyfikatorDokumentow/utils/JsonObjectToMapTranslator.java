package pl.pentacomp.ModyfikatorDokumentow.utils;

import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonObjectToMapTranslator {

    public static Map<String, String> translate(JSONObject jsonObject) {
        final Map<String, String> resultMap = new ConcurrentHashMap<>();
        for (Object e : jsonObject.entrySet()) {
            resultMap.put(
                    String.valueOf(((Map.Entry)(e)).getKey()),
                    String.valueOf(((Map.Entry)(e)).getValue()));
        }
        return resultMap;
    }
}
