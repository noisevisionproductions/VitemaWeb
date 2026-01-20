package com.noisevisionsoftware.vitema.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void removeUnits_WithNull_ShouldReturnEmptyString() {
        // when
        String result = StringUtils.removeUnits(null);

        // then
        assertEquals("", result);
    }

    @Test
    void removeUnits_WithEmptyString_ShouldReturnEmptyString() {
        // given
        String input = "";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("", result);
    }

    @Test
    void removeUnits_WithoutUnits_ShouldReturnOriginalString() {
        // given
        String input = "Mleko bez jednostki";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Mleko bez jednostki", result);
    }

    @Test
    void removeUnits_WithKgUnit_ShouldRemoveUnit() {
        // given
        String input = "Mąka pszenna 1kg";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Mąka pszenna", result);
    }

    @Test
    void removeUnits_WithDecimalUnit_ShouldRemoveUnit() {
        // given
        String input = "Woda mineralna 1.5l";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Woda mineralna", result);
    }

    @Test
    void removeUnits_WithPolishDecimalSeparator_ShouldRemoveUnit() {
        // given
        String input = "Mleko UHT 2,5% 1,5l";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Mleko UHT 2,5%", result);
    }

    @Test
    void removeUnits_WithSpacesAroundUnit_ShouldRemoveUnit() {
        // given
        String input = "Masło extra   200 g  ";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Masło extra", result);
    }

    @Test
    void removeUnits_WithFullUnitNames_ShouldRemoveUnit() {
        // given
        String input = "Cukier 1 kilogram";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Cukier", result);
    }

    @Test
    void removeUnits_WithPolishInflectionGram_ShouldRemoveUnit() {
        // given
        String input = "Ryż 500 gramów";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Ryż", result);
    }

    @Test
    void removeUnits_WithPolishInflectionLiter_ShouldRemoveUnit() {
        // given
        String input = "Sok pomarańczowy 2 litry";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Sok pomarańczowy", result);
    }

    @Test
    void removeUnits_WithMultipleUnits_ShouldRemoveAllUnits() {
        // given
        String input = "Zestaw 2 sztuki po 500 ml";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Zestaw po", result);
    }

    @Test
    void removeUnits_WithExtraSpaces_ShouldCollapseSpaces() {
        // given
        String input = "Kawa   mielona    250  g";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Kawa mielona", result);
    }

    @Test
    void removeUnits_WithDekaPrefix_ShouldRemoveUnit() {
        // given
        String input = "Wędlina 15 dekagramów";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Wędlina", result);
    }

    @Test
    void removeUnits_WithKiloPrefix_ShouldRemoveUnit() {
        // given
        String input = "Ziemniaki 5 kilogramów";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Ziemniaki", result);
    }

    @Test
    void removeUnits_WithSztukPrefix_ShouldRemoveUnit() {
        // given
        String input = "Jajka 10 sztuk";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Jajka", result);
    }

    @Test
    void removeUnits_WithOpakPrefix_ShouldRemoveUnit() {
        // given
        String input = "Herbata 3 opakowania";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Herbata", result);
    }

    @Test
    void removeUnits_WithLitPrefix_ShouldRemoveUnit() {
        // given
        String input = "Olej roślinny 2 litry";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Olej roślinny", result);
    }

    @Test
    void removeUnits_WithMlUnit_ShouldRemoveUnit() {
        // given
        String input = "Syrop 350 ml";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Syrop", result);
    }

    @Test
    void removeUnits_WithCmUnit_ShouldRemoveUnit() {
        // given
        String input = "Deska do krojenia 30 cm";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Deska do krojenia", result);
    }

    @Test
    void removeUnits_WithMmUnit_ShouldRemoveUnit() {
        // given
        String input = "Folia aluminiowa grubość 0,5 mm";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Folia aluminiowa grubość", result);
    }

    @Test
    void removeUnits_WithNoSpaceBetweenNumberAndUnit_ShouldRemoveUnit() {
        // given
        String input = "Pieprz czarny 50g";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Pieprz czarny", result);
    }

    @Test
    void removeUnits_WithNoDigitBeforeUnit_ShouldNotRemoveText() {
        // given
        String input = "Gramatura to ważna cecha";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Gramatura to ważna cecha", result);
    }

    @Test
    void removeUnits_WithMultipleSimilarUnits_ShouldRemoveAllUnits() {
        // given
        String input = "Napój 2 litry z dodatkiem 30 gram cukru";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Napój z dodatkiem cukru", result);
    }

    @Test
    void removeUnits_WithUnitAtBeginning_ShouldRemoveUnit() {
        // given
        String input = "500g Mąka pszenna";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Mąka pszenna", result);
    }

    @Test
    void removeUnits_WithComplexProductName_ShouldRemoveOnlyUnits() {
        // given
        String input = "Jogurt naturalny 2% tłuszczu 400g x 4 sztuki";

        // when
        String result = StringUtils.removeUnits(input);

        // then
        assertEquals("Jogurt naturalny 2% tłuszczu x", result);
    }
}