package com.noisevisionsoftware.nutrilog.utils.excelParser.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.noisevisionsoftware.nutrilog.utils.excelParser.config.ExcelReadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExcelReaderService {

    public List<List<String>> readExcelFile(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        EasyExcel.read(file.getInputStream())
                .sheet()
                .headRowNumber(0) // Pierwszy wiersz to nagłówki
                .registerReadListener(new AnalysisEventListener<Map<Integer, String>>() {
                    @Override
                    public void invoke(Map<Integer, String> rowMap, AnalysisContext context) {
                        List<String> cleanRow = new ArrayList<>();
                        for (int i = 0; i < rowMap.size(); i++) {
                            String cell = rowMap.get(i);
                            cleanRow.add(cell != null ? cell.trim() : "");
                        }
                        rows.add(cleanRow);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        // Implementacja pozostaje pusta
                    }

                    @Override
                    public void onException(Exception exception, AnalysisContext context) throws Exception {
                        log.error("Błąd podczas czytania pliku Excel", exception);
                        throw exception;
                    }
                })
                .doRead();

        return rows;
    }

    /**
     * Wersja metody z dodatkowymi parametrami konfiguracyjnymi
     */
    public List<List<String>> readExcelFile(MultipartFile file, ExcelReadConfig config) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        EasyExcel.read(file.getInputStream())
                .sheet(config.getSheetNumber())
                .headRowNumber(config.getHeaderRowNumber())
                .registerReadListener(new AnalysisEventListener<List<String>>() {
                    @Override
                    public void invoke(List<String> row, AnalysisContext context) {
                        if (config.shouldSkipEmptyRows() && isRowEmpty(row)) {
                            return;
                        }

                        List<String> cleanRow = new ArrayList<>();
                        for (String cell : row) {
                            String cleanCell = cell != null ? cell.trim() : "";
                            if (config.shouldTrimCells()) {
                                cleanCell = cleanCell.trim();
                            }
                            cleanRow.add(cleanCell);
                        }
                        rows.add(cleanRow);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        // Implementacja pozostaje pusta
                    }

                    @Override
                    public void onException(Exception exception, AnalysisContext context) throws Exception {
                        log.error("Błąd podczas czytania pliku Excel", exception);
                        throw exception;
                    }
                })
                .doRead();

        return rows;
    }

    private boolean isRowEmpty(List<String> row) {
        return row == null || row.stream().allMatch(cell -> cell == null || cell.trim().isEmpty());
    }
}