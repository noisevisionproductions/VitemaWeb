package com.noisevisionsoftware.vitema.utils;

import org.springframework.stereotype.Component;

/**
 * Utility do obliczania podobieństwa między tekstami
 */
@Component
public class SimilarityCalculator {

    /**
     * Oblicza podobieństwo między dwoma tekstami
     * @param query tekst zapytania
     * @param target tekst docelowy
     * @return wartość podobieństwa między 0.0 a 1.0
     */
    public double calculateSimilarity(String query, String target) {
        if (query == null || target == null) return 0.0;

        String normalizedQuery = normalizeText(query);
        String normalizedTarget = normalizeText(target);

        // Dokładne dopasowanie
        if (normalizedQuery.equals(normalizedTarget)) {
            return 1.0;
        }

        // Sprawdź czy jeden tekst zawiera drugi
        if (normalizedTarget.contains(normalizedQuery) || normalizedQuery.contains(normalizedTarget)) {
            return 0.9;
        }

        // Oblicz podobieństwo za pomocą odległości Levenshtein
        return calculateLevenshteinSimilarity(normalizedQuery, normalizedTarget);
    }

    /**
     * Oblicza podobieństwo na podstawie odległości Levenshtein
     */
    private double calculateLevenshteinSimilarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return 1.0;

        int distance = calculateLevenshteinDistance(a, b);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Oblicza odległość Levenshtein między dwoma tekstami
     */
    private int calculateLevenshteinDistance(String a, String b) {
        if (a.isEmpty()) return b.length();
        if (b.isEmpty()) return a.length();

        int[][] matrix = new int[a.length() + 1][b.length() + 1];

        // Inicjalizacja pierwszego wiersza i kolumny
        for (int i = 0; i <= a.length(); i++) {
            matrix[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            matrix[0][j] = j;
        }

        // Wypełnienie macierzy
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                matrix[i][j] = Math.min(
                        Math.min(
                                matrix[i - 1][j] + 1,      // usunięcie
                                matrix[i][j - 1] + 1       // wstawienie
                        ),
                        matrix[i - 1][j - 1] + cost    // zamiana
                );
            }
        }

        return matrix[a.length()][b.length()];
    }

    /**
     * Normalizuje tekst do porównania (małe litery, bez białych znaków na końcach)
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase().trim();
    }

    /**
     * Sprawdza, czy dwa teksty są identyczne (ignorując wielkość liter)
     */
    public boolean isExactMatch(String query, String target) {
        if (query == null || target == null) return false;
        return normalizeText(query).equals(normalizeText(target));
    }

    /**
     * Sprawdza, czy podobieństwo jest wysokie (> threshold)
     */
    public boolean isHighSimilarity(String query, String target, double threshold) {
        return calculateSimilarity(query, target) > threshold;
    }
}