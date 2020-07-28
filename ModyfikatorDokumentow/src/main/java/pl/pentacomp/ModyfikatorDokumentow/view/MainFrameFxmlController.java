package pl.pentacomp.ModyfikatorDokumentow.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import pl.pentacomp.ModyfikatorDokumentow.enums.ErrorType;
import pl.pentacomp.ModyfikatorDokumentow.service.FileLoaderService;
import pl.pentacomp.ModyfikatorDokumentow.service.FileModificationService;
import pl.pentacomp.ModyfikatorDokumentow.utils.TestUtils;

import java.io.File;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainFrameFxmlController {

    private static final File APPLICATION_FOLDER_FILE = new File(System.getProperty("user.dir"));

    private final FileLoaderService fileLoaderService;
    private final FileModificationService fileModificationService;

    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;

    @FXML
    void initialize() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(APPLICATION_FOLDER_FILE);

        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(APPLICATION_FOLDER_FILE);
    }

    /**********
     * LABELS *
     **********/

    @FXML
    private Label filesSourcePathLabel;

    @FXML
    private Label filesDestinyPathLabel;

    @FXML
    private Label configurationFilePathLabel;

    // TEXT AREAS

    @FXML
    private TextArea processingTextArea;

    /***********
     * BUTTONS *
     ***********/

    @FXML
    private Button chooseSourcePathButton;

    @FXML
    private Button chooseDestinyPathButton;

    @FXML
    private Button chooseConfigurationFileButton;

    @FXML
    private Button startButton;

    @FXML
    private Button exitButton;

    /***************
     * CHECK BOXES *
     ***************/

    @FXML
    private CheckBox subfoldersCheckBox;

    @FXML
    public CheckBox removeSourceFilesCheckbox;

    /****************
     * PROGRESS BAR *
     ****************/

    @FXML
    private ProgressBar processingProgressBar;

    /********************
     * ACTION LISTENERS *
     ********************/

    @FXML
    private void startButtonActionListener(ActionEvent event) {

        // Wstępna walidacja wartości danych wejściowych i ich typów

        final String sourceFolderPath = filesSourcePathLabel.getText();
        if (StringUtils.isBlank(sourceFolderPath)) {
            setErrorBox(ErrorType.BLANK_SOURCE_FOLDER_PATH);
            return;
        } else if (!fileLoaderService.checkIsDirectory(sourceFolderPath)) {
            setErrorBox(ErrorType.NOT_DIRECTORY);
            return;
        }

        final String destinyFolderPath = filesDestinyPathLabel.getText();
        if (StringUtils.isBlank(destinyFolderPath)) {
            setErrorBox(ErrorType.BLANK_DESTINY_FOLDER_PATH);
        } else if (!fileLoaderService.checkIsDirectory(destinyFolderPath)) {
            setErrorBox(ErrorType.NOT_DIRECTORY);
            return;
        }

        final String configurationFilePath = configurationFilePathLabel.getText();
        if (StringUtils.isBlank(configurationFilePath)) {
            setErrorBox(ErrorType.BLANK_CONFIGURATION_FILE_PATH);
        } else if (!fileLoaderService.checkIsFile(configurationFilePath)) {
            setErrorBox(ErrorType.NOT_FILE);
            return;
        } else if (!FilenameUtils.getExtension(configurationFilePath).equals("json")) {
            setErrorBox(ErrorType.BAD_FILE_TYPE);
            return;
        }

        clearProgressBar();

        // Pobieranie danych z plików

        addTextToProcessTextArea("Wczytuje plik konfiguracyjny.\n");
        fileModificationService.loadModificationFile(configurationFilePath);
        fillProgressBar(0.05d);

        addTextToProcessTextArea("Wczytuję pliki z folderu źródłowego.\n");
        final List<File> sourceFileList = fileLoaderService.getFilesFromFolder(sourceFolderPath);
        fillProgressBar(0.05d);

        addTextToProcessTextArea("Zaczynam modyfikować i kopiować pliki.\n");
        fileModificationService.modifiedFiles(
                sourceFileList,
                destinyFolderPath,
                subfoldersCheckBox.isSelected(),
                removeSourceFilesCheckbox.isSelected(),
                this);
    }

    @FXML
    private void exitButtonActionListener(ActionEvent event) {
        //TODO - safe exit from Spring and JavaFX
        System.exit(0);
    }

    @FXML
    private void setChooseConfigurationFileButtonActionListener(ActionEvent event) {
        configurationFilePathLabel.setText(getFilePath());
    }

    @FXML
    private void setChooseDestinyPathButtonActionListener(ActionEvent event) {
        filesDestinyPathLabel.setText(getDirectoryPath());
    }

    @FXML
    private void setChooseSourcePathButtonActionListener(ActionEvent event) {
        filesSourcePathLabel.setText(getDirectoryPath());
    }

    /******************
     * PUBLIC METHODS *
     ******************/

    public void clearProgressBar() {
        processingProgressBar.setProgress(0.0d);
    }

    public void fillProgressBar(double fillPercentage) {
        processingProgressBar.setProgress(processingProgressBar.getProgress() + fillPercentage);
    }

    public void addTextToProcessTextArea(String text) {
        processingTextArea.setText(StringUtils.join(processingTextArea.getText(), text));
    }

    public void setErrorBox(ErrorType errorType) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wystąpił błąd!");
        alert.setHeaderText(null);
        alert.setContentText(errorType.getMessage());
        alert.showAndWait();
    }

    /*******************
     * PRIVATE METHODS *
     *******************/

    private String getDirectoryPath() {
        return directoryChooser.showDialog(filesSourcePathLabel.getScene().getWindow()).getAbsolutePath();
    }

    private String getFilePath() {
        return fileChooser.showOpenDialog(filesSourcePathLabel.getScene().getWindow()).getAbsolutePath();
    }
}
