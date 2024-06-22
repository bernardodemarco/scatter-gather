package com.github.bernardodemarco.textretrieval.worker.dto;

public class KeywordOccurrencesDTO {
    private final String fileName;
    private final long occurrences;

    public KeywordOccurrencesDTO(String fileName, long occurrences) {
        this.fileName = fileName;
        this.occurrences = occurrences;
    }

    public String getFileName() {
        return fileName;
    }

    public long getOccurrences() {
        return occurrences;
    }
}
