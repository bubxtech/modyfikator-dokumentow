package pl.pentacomp.ModyfikatorDokumentow.service;

import javafx.scene.Parent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.pentacomp.ModyfikatorDokumentow.enums.ErrorType;
import pl.pentacomp.ModyfikatorDokumentow.view.MainFrameFxmlController;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileModificationService {

    private final ModificationRulesService modificationRulesService;
    private final FileReadWriteDeleteService fileReadWriteDeleteService;

    private Map<String, String> fileNameModificationRulesMap;
    private Map<String, String> fileContentModificationRulesMap;

    public void loadModificationFile(String configurationFilePath) {
        fileNameModificationRulesMap = modificationRulesService.getFileNameModificationRules(configurationFilePath);
        fileContentModificationRulesMap = modificationRulesService.getFileContentModificationRules(configurationFilePath);
    }

    public boolean modifiedFiles(List<File> sourceFileList, String destinyFolderPath, boolean withSubfolders,
                                 boolean removeSourceFile, MainFrameFxmlController fxmlController) {
        return withSubfolders
                ? modifiedWithSubfolders(sourceFileList, destinyFolderPath, removeSourceFile, fxmlController)
                : modifiedWithoutSubfolders(sourceFileList, destinyFolderPath, removeSourceFile, fxmlController);
    }

    private boolean modifiedWithoutSubfolders(List<File> sourceFileList, String destinyFolderPath, boolean removeSourceFile, MainFrameFxmlController fxmlController) {
        double oneFileProgressPart = 0.9d / sourceFileList.size();
        for (File sourceFile : sourceFileList) {
            if (sourceFile.isFile()) {
                readModifieAndCopyFile(sourceFile, destinyFolderPath, removeSourceFile, fxmlController);
            }
            fxmlController.fillProgressBar(oneFileProgressPart);
        }
        return true; //TODO - ogarnięcie jak coś źle to false
    }

    private boolean modifiedWithSubfolders(List<File> sourceFileList, String destinyFolderPath, boolean removeSourceFile, MainFrameFxmlController fxmlController) {
        //TODO
        fxmlController.clearProgressBar();
        fxmlController.setErrorBox(ErrorType.OPERATION_TEMPORARY_AVAILABLE);
        return false;
    }

    private void readModifieAndCopyFile(File sourceFile, String destinyFolderPath,
                                        boolean removeSourceFile, MainFrameFxmlController fxmlController) {

        if (fileNameModificationRulesMap.entrySet().stream().noneMatch(entry -> sourceFile.getName().contains(entry.getKey()))) {
            return;
        }

        String sourceFileName = sourceFile.getName();

        fxmlController.addTextToProcessTextArea("Zaczynam wczytywanie pliku " + sourceFileName + "\n");
        final List<String> sourceFileContentLines = fileReadWriteDeleteService.readFileLines(sourceFile);

        if (CollectionUtils.isEmpty(sourceFileContentLines)) {
            fxmlController.addTextToProcessTextArea("Błąd wczytywania pliku " + sourceFileName + " lub plik jest pusty!\n");
            return;
        }

        // Generowanie nazwy nowego pliku
        final String newFileName = generateNewFileName(sourceFileName);

        // Generowanie treści nowego pliku
        final String newFileContent = generateNewFileContent(sourceFileContentLines);

        fxmlController.addTextToProcessTextArea("Zaczynam zapis nowego pliku " + newFileName + " do lokalizacji " + destinyFolderPath + "\n");
        if (!fileReadWriteDeleteService.writeFile(newFileContent, newFileName, destinyFolderPath)) {
            fxmlController.addTextToProcessTextArea("Błąd zapisu nowego pliku " + newFileName + " do ścieżki " + destinyFolderPath + "\n");
        }

        if (removeSourceFile) {
            if (fileReadWriteDeleteService.deleteFile(sourceFile)) {
                fxmlController.addTextToProcessTextArea("Usunięto stary plik o nazwie " + sourceFileName);
            } else {
                fxmlController.addTextToProcessTextArea("Błąd podczas usuwania pliku o nazwie " + sourceFileName);
            }
        }
    }

    private String generateNewFileContent(List<String> sourceFileContentLines) {
        List<String> newFileContentLines = new ArrayList<>(sourceFileContentLines);
        for (Map.Entry<String, String> entry : fileContentModificationRulesMap.entrySet()) {
            final String modificationKey = entry.getKey();
            final String modificationValue = entry.getValue();

            boolean isMultilineRule = entry.getKey().contains("\n");
            if (isMultilineRule) {
                modificateWithMultiLineRule(newFileContentLines, modificationKey, modificationValue);
            } else {
                modificateWithSingleLineRule(newFileContentLines, modificationKey, modificationValue);
            }
        }
        return String.join("\n", newFileContentLines);
    }

    private void modificateWithSingleLineRule(List<String> newFileContentLines, String modificationKey, String modificationValue) {
        for (int lineIndex = 0; lineIndex < newFileContentLines.size(); lineIndex++) {
            if (newFileContentLines.get(lineIndex).contains(modificationKey)) {
                newFileContentLines.set(lineIndex, newFileContentLines.get(lineIndex).replace(modificationKey, modificationValue));
            }
        }
    }

    private void modificateWithMultiLineRule(List<String> newFileContentLines, String modificationKey, String modificationValue) {
        List<String> changePattern = Arrays.stream(modificationKey.split("\n"))
                                            .map(String::trim)
                                            .collect(Collectors.toList());

        List<String> modificationValuesLineList = Arrays.asList(modificationValue.split("\n"));

        List<String> trimFileContentLines = newFileContentLines.stream()
                                                .map(String::trim)
                                                .collect(Collectors.toList());

        for(int lineIndex = 0; lineIndex < trimFileContentLines.size(); lineIndex++) {
            if (checkLinesSimilarity(changePattern, trimFileContentLines, lineIndex)) {
                replaceLines(newFileContentLines, lineIndex, modificationValuesLineList);
                lineIndex += modificationValuesLineList.size() - 1;
            }
        }
    }

    private void replaceLines(List<String> newFileContentLines, int lineIndex, List<String> modifiedLines) {
        for (int index = lineIndex; index < modifiedLines.size() + lineIndex; index++) {
            final String whitespaceLinePrefix = getWhitespaceLinePrefix(newFileContentLines.get(index));
            newFileContentLines.set(index, StringUtils.join(whitespaceLinePrefix, modifiedLines.get(index - lineIndex)));
        }
    }

    private String getWhitespaceLinePrefix(String text) {
        final StringBuilder sb = new StringBuilder();
        int whitespacePrefixCounter = 0;
        while (text.toCharArray()[whitespacePrefixCounter++] == ' ') {
            sb.append(StringUtils.SPACE);
        }
        return sb.toString();
    }

    private boolean checkLinesSimilarity(List<String> changePattern, List<String> trimFileContentLines, int lineIndex) {
        for (int i = 0; i < changePattern.size(); i++) {
            if (!changePattern.get(i).equals(trimFileContentLines.get(lineIndex + i))) {
                return false;
            }
        }
        return true;
    }

    private String generateNewFileName(String sourceFileName) {
        for (Map.Entry<String,String> entry : fileNameModificationRulesMap.entrySet()) {
            sourceFileName = sourceFileName.replace(entry.getKey(), entry.getValue());
        }
        return sourceFileName;
    }
}
