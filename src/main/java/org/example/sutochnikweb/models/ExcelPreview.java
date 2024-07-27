package org.example.sutochnikweb.models;

import java.util.List;

public class ExcelPreview {
    private List<String> headers;
    private List<List<String>> rows;
    private List<List<String>> analyticRows;
    private List<String> analyticHeaders;// New field

    public ExcelPreview(List<String> headers, List<List<String>> rows, List<List<String>> analyticRows, List<String> analyticHeaders) {
        this.headers = headers;
        this.rows = rows;
        this.analyticRows = analyticRows;
        this.analyticHeaders = analyticHeaders;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public List<List<String>> getAnalyticRows() {
        return analyticRows;
    }

    public List<String> getAnalyticHeaders() {
        return analyticHeaders;
    }
}