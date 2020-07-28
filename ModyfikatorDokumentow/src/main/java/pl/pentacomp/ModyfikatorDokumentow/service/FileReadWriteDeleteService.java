package pl.pentacomp.ModyfikatorDokumentow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FileReadWriteDeleteService {

    public boolean writeFile(String fileContent, String fileName, String filePath) {
        try {
            Files.write(Paths.get(filePath + "\\" + fileName), fileContent.getBytes());
            return true;
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            return false;
        }
    }

    public List<String> readFileLines(File sourceFile) {
        try {
            return Files.readAllLines(sourceFile.toPath());
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            return Collections.emptyList();
        }

    }

    public boolean deleteFile(File file) {
        return file.delete();
    }
}
