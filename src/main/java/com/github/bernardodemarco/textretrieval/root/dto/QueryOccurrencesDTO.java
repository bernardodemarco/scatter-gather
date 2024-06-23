package com.github.bernardodemarco.textretrieval.root.dto;

import java.util.List;

public class QueryOccurrencesDTO {
    private final String fileName;
    private final List<String> fileContent;
    private final long occurrences;

    public QueryOccurrencesDTO(String fileName, List<String> fileContent, long occurrences) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.occurrences = occurrences;
    }
}
