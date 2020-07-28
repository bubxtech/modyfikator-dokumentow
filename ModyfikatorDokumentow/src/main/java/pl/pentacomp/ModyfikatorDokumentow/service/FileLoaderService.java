package pl.pentacomp.ModyfikatorDokumentow.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.pentacomp.ModyfikatorDokumentow.enums.ModificationRulesFilePartType;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileLoaderService {

    public boolean checkIsFile(String filePath) {
        return new File(filePath).isFile();
    }

    public boolean checkIsDirectory(String folderPath) {
        return new File(folderPath).isDirectory();
    }

    public Optional<JSONObject> getFileNameModificationRulesJsonFile(String filePath) {
        return getModificationRules(filePath, ModificationRulesFilePartType.FILE_NAMES);
    }

    public Optional<JSONObject> getFileContentModificationRulesJsonFile(String filePath) {
        return getModificationRules(filePath, ModificationRulesFilePartType.FILE_CONTENT);
    }

    private Optional<JSONObject> getModificationRules(String filePath, ModificationRulesFilePartType filePartType) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            final JSONArray employeeList = (JSONArray) jsonParser.parse(reader);
            return CollectionUtils.isEmpty(employeeList)
                    ? Optional.empty()
                    : Optional.ofNullable((JSONObject) employeeList.get(filePartType.getFilePartOrderNumber()));
        }catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            return Optional.empty();
        }
    }

    public List<File> getFilesFromFolder(String sourceFolderPath) {
        return Arrays.stream(Objects.requireNonNull(new File(sourceFolderPath).listFiles()))
                .filter(File::isFile)
                .collect(Collectors.toList());
    }
}
