package com.github.bernardodemarco.textretrieval.root.dto;

public class QueryOccurrencesDTO {
    // later on, add query and file content
    private final String fileName;
    private final long occurrences;

    public QueryOccurrencesDTO(String fileName, long occurrences) {
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
