package pl.pentacomp.ModyfikatorDokumentow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModificationRulesFilePartType {
    FILE_NAMES(0),
    FILE_CONTENT(1);

    private final int filePartOrderNumber;
}
