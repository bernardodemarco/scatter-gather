package com.github.bernardodemarco.textretrieval.root.dto;

import java.util.List;

public class QueryOccurrencesDTO {
    // later on, add query and file content
    private final String fileName;
    private final List<String> fileContent;
    private final long occurrences;

    public QueryOccurrencesDTO(String fileName, List<String> fileContent, long occurrences) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.occurrences = occurrences;
    }
}
