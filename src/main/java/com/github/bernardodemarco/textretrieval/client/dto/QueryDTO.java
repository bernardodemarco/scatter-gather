package com.github.bernardodemarco.textretrieval.client.dto;

public class QueryDTO {
    private final String query;

    public QueryDTO(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
