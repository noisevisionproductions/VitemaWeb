package com.noisevisionsoftware.vitema.utils.excelParser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "excel-parser")
@Data
public class ExcelParserConfig {

    /*
    * Liczba kolumn do pominięcia podczas parsowania pliku excel.
    * Domyślnie pomijana jest 1 kolumna (indeks 0).
    * */
    private int skipColumnsCount = 1;

    /*
    * Maksymalna liczba kolumn, która może zostać pominięta
    * */
    private int maxSkipColumnsCount = 3;
}
