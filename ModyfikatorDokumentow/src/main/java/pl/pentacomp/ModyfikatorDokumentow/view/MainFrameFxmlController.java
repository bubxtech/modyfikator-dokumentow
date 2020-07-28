package pl.pentacomp.ModyfikatorDokumentow.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import pl.pentacomp.ModyfikatorDokumentow.service.FileLoaderService;
import pl.pentacomp.ModyfikatorDokumentow.service.ModificationRulesService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainFrameFxmlController {

    private static final String ERROR_TITLE = "Wystąpił błąd!";
    private static final String ERROR_BLANK_SOURCE_FOLDER_PATH = "Scieżka folderu z plikami do modyfikacji nie może być pusta!";
    private static final String ERROR_BLANK_DESTINY_FOLDER_PATH = "Scieżka folderu docelowego dla nowych plików nie może być pusta!";
    private static final String ERROR_BLANK_CONFIGURATION_FILE_PATH = "Scieżka do folderu konfiguracyjnego nie może być pusta!";
    private static final String ERROR_NOT_DIRECTORY = "Wskazana ścieżka nie odnosi się do folderu!";
    private static final String ERROR_NOT_FILE = "Wskazana ścieżka nie odnosi się do pliku!";
    private static final String ERROR_BAD_FILE_TYPE = "Typ pliku niezgodny z oczekiwanym!";

    private static final String JSON_EXTENSION = "json";

    private final FileLoaderService fileLoaderService;
    private final ModificationRulesService modificationRulesService;

    // LABELS

    @FXML
    private Label filesSourcePathLabel;

    @FXML
    private Label filesDestinyPathLabel;

    @FXML
    private Label configurationFilePathLabel;

    // TEXT AREAS

    @FXML
    private TextArea processingTextArea;

    // BUTTONS

    @FXML
    private Button chooseSourcePathButton;

    @FXML
    private void setChooseSourcePathButtonActionListener(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File sourceDirectory = directoryChooser.showDialog(chooseSourcePathButton.getScene().getWindow());

        filesSourcePathLabel.setText(sourceDirectory.getAbsolutePath());
    }

    @FXML
    private Button chooseDestinyPathButton;

    @FXML
    private void setChooseDestinyPathButtonActionListener(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File destinyDirectory = directoryChooser.showDialog(chooseDestinyPathButton.getScene().getWindow());

        filesDestinyPathLabel.setText(destinyDirectory.getAbsolutePath());
    }

    @FXML
    private Button chooseConfigurationFileButton;

    @FXML
    private void setChooseConfigurationFileButtonActionListener(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File configFile = fileChooser.showOpenDialog(chooseConfigurationFileButton.getScene().getWindow());

        configurationFilePathLabel.setText(configFile.getAbsolutePath());
    }

    @FXML
    private Button startButton;

    @FXML
    private void startButtonActionListener(ActionEvent event) {

        // Wstępna walidacja wartości danych wejściowych i ich typów

        final String sourceFolderPath = filesSourcePathLabel.getText();
        if (StringUtils.isBlank(sourceFolderPath)) {
            setErrorBox(ERROR_BLANK_SOURCE_FOLDER_PATH);
            return;
        } else if (!fileLoaderService.checkIsDirectory(sourceFolderPath)) {
            setErrorBox(ERROR_NOT_DIRECTORY);
            return;
        }

        final String destinyFolderPath = filesDestinyPathLabel.getText();
        if (StringUtils.isBlank(destinyFolderPath)) {
            setErrorBox(ERROR_BLANK_DESTINY_FOLDER_PATH);
        } else if (!fileLoaderService.checkIsDirectory(destinyFolderPath)) {
            setErrorBox(ERROR_NOT_DIRECTORY);
            return;
        }

        final String configurationFilePath = configurationFilePathLabel.getText();
        if (StringUtils.isBlank(configurationFilePath)) {
            setErrorBox(ERROR_BLANK_CONFIGURATION_FILE_PATH);
        } else if (!fileLoaderService.checkIsFile(configurationFilePath)) {
            setErrorBox(ERROR_NOT_FILE);
            return;
        } else if (!FilenameUtils.getExtension(configurationFilePath).equals(JSON_EXTENSION)) {
            setErrorBox(ERROR_BAD_FILE_TYPE);
            return;
        }

        clearProgressBar();

        // Pobieranie danych z plików

        fillProgressBar(0.05d);

        addTextToProcessTextArea("Wczytuje plik konfiguracyjny.\n");
        final Map<String, String> fileNameModificationRulesMap
                = modificationRulesService.getFileNameModificationRules(configurationFilePath);

        final Map<String, String> fileContentModificationRulesMap
                = modificationRulesService.getFileContentModificationRules(configurationFilePath);

        fillProgressBar(0.05d);

        addTextToProcessTextArea("Wczytuję pliki z folderu źródłowego.\n");
        final List<File> sourceFileList = fileLoaderService.getFilesFromFolder(sourceFolderPath);

        addTextToProcessTextArea("Zaczynam modyfikować i kopiować pliki.\n");
        if (subfoldersCheckBox.isSelected()) {
            checkFolderAndAllSubfoldersThenModifieAndCopyFiles(sourceFileList,
                    destinyFolderPath,
                    fileNameModificationRulesMap,
                    fileContentModificationRulesMap,
                    removeOldFilesCheckbox.isSelected());
        } else {
            checkFolderThenModifieAndCopyFiles(sourceFileList,
                    destinyFolderPath,
                    fileNameModificationRulesMap,
                    fileContentModificationRulesMap,
                    removeOldFilesCheckbox.isSelected());
        }
    }

    private void checkFolderAndAllSubfoldersThenModifieAndCopyFiles(List<File> sourceFileList, String destinyFolderPath, Map<String, String> fileNameModificationRulesMap, Map<String, String> fileContentModificationRulesMap, boolean selected) {
        clearProgressBar();
        setErrorBox("Opcja modyfikacji w podfolderach chwilowo niedostępna!");
    }

    private void checkFolderThenModifieAndCopyFiles(List<File> sourceFileList, String destinyFolderPath, Map<String, String> fileNameModificationRulesMap, Map<String, String> fileContentModificationRulesMap, boolean removeOldFile) {
        double oneFileProgressPart = 0.9d / sourceFileList.size();
        for (File sourceFile : sourceFileList) {
            if (sourceFile.isFile()) {
                readModifieAndCopyFile(sourceFile, destinyFolderPath, fileNameModificationRulesMap, fileContentModificationRulesMap, removeOldFile);
            }
            fillProgressBar(oneFileProgressPart);
        }
    }

    private void readModifieAndCopyFile(File sourceFile, String destinyFolderPath, Map<String, String> fileNameModificationRulesMap, Map<String, String> fileContentModificationRulesMap, boolean removeOldFile) {
        if (fileNameModificationRulesMap.entrySet().stream().anyMatch(entry -> sourceFile.getName().contains(entry.getKey()))) {

            String sourceFileName = sourceFile.getName();
            addTextToProcessTextArea("Zaczynam wczytywanie pliku " + sourceFileName + "\n");
            String newFileName = sourceFileName;

            String newFileContent = null;
            try {

                newFileContent = new String(Files.readAllBytes(sourceFile.toPath()));

                // Generowanie nazwy nowego pliku
                for (Map.Entry<String,String> entry : fileNameModificationRulesMap.entrySet()) {
                    newFileName = newFileName.replace(entry.getKey(), entry.getValue());
                }

                // Generowanie treści nowego pliku
                for (Map.Entry<String, String> entry : fileContentModificationRulesMap.entrySet()) {
                    newFileContent = newFileContent.replace(entry.getKey(), entry.getValue());
                }
            } catch (IOException ex) {
                addTextToProcessTextArea("Błąd wczytywania pliku " + sourceFileName + "!\n");
                return;
            }

            try {
                //Zapis nowego pliku
                addTextToProcessTextArea("Zaczynam zapis nowego pliku " + newFileName + " do lokalizacji " + destinyFolderPath + "\n");
                Files.write(Paths.get(destinyFolderPath + "\\" + newFileName), newFileContent.getBytes());
            } catch (IOException ex) {
                addTextToProcessTextArea("Błąd zapisu nowego pliku " + newFileName + " do ścieżki " + destinyFolderPath + "\n");
                return;
            }

            if (removeOldFile) {
                if (sourceFile.delete()) {
                    addTextToProcessTextArea("Usunięto stary plik o nazwie " + sourceFileName);
                } else {
                    addTextToProcessTextArea("Błąd podczas usuwania pliku o nazwie " + sourceFileName);
                }
            }
        }
    }


    private void clearProgressBar() {
        processingProgressBar.setProgress(0.0d);
    }

    private void fillProgressBar(double fillPercentage) {
        processingProgressBar.setProgress(processingProgressBar.getProgress() + fillPercentage);
    }

    private void addTextToProcessTextArea(String text) {
        processingTextArea.setText(StringUtils.join(processingTextArea.getText(), text));
    }

    @FXML
    private Button exitButton;

    @FXML
    private void exitButtonActionListener(ActionEvent event) {
        //TODO - safe exit from Spring and JavaFX
        System.exit(0);
    }

    // CHECK BOXES

    @FXML
    private CheckBox subfoldersCheckBox;

    @FXML
    public CheckBox removeOldFilesCheckbox;

    // PROGRESS BAR

    @FXML
    private ProgressBar processingProgressBar;

    private void setErrorBox(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
