package pl.pentacomp.ModyfikatorDokumentow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    BLANK_SOURCE_FOLDER_PATH("Scieżka folderu z plikami do modyfikacji nie może być pusta!"),
    BLANK_DESTINY_FOLDER_PATH("Scieżka folderu docelowego dla nowych plików nie może być pusta!"),
    BLANK_CONFIGURATION_FILE_PATH("Scieżka do folderu konfiguracyjnego nie może być pusta!"),
    NOT_DIRECTORY("Wskazana ścieżka nie odnosi się do folderu!"),
    NOT_FILE("Wskazana ścieżka nie odnosi się do pliku!"),
    BAD_FILE_TYPE("Typ pliku niezgodny z oczekiwanym!"),
    OPERATION_TEMPORARY_AVAILABLE("Operacja chwilowo niedostępna!");

    private final String message;
}
