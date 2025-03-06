package com.noisevisionsoftware.nutrilog.config;

import com.noisevisionsoftware.nutrilog.model.shopping.category.Category;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CategoryConfig {

    @Bean
    public List<Category> defaultCategories() {
        return List.of(
                Category.builder()
                        .id("dairy")
                        .name("Nabiał")
                        .color("#AED6F1")
                        .icon("Milk")
                        .order(1)
                        .build(),
                Category.builder()
                        .id("meat-fish")
                        .name("Ryby i Mięso")
                        .color("#F5B7B1")
                        .icon("Fish")
                        .order(2)
                        .build(),
                Category.builder()
                        .id("vegetables")
                        .name("Warzywa")
                        .color("#A9DFBF")
                        .icon("Carrot")
                        .order(3)
                        .build(),
                Category.builder()
                        .id("fruits")
                        .name("Owoce")
                        .color("#F9E79F")
                        .icon("Apple")
                        .order(4)
                        .build(),
                Category.builder()
                        .id("dry-goods")
                        .name("Produkty suche")
                        .color("#F5CBA7")
                        .icon("Wheat")
                        .order(5)
                        .build(),
                Category.builder()
                        .id("spices")
                        .name("Przyprawy")
                        .color("#E8DAEF")
                        .icon("Soup")
                        .order(6)
                        .build(),
                Category.builder()
                        .id("oils")
                        .name("Oleje i tłuszcze")
                        .color("#FAD7A0")
                        .icon("Droplet")
                        .order(7)
                        .build(),
                Category.builder()
                        .id("nuts")
                        .name("Orzechy i nasiona")
                        .color("#D5D8DC")
                        .icon("Nut")
                        .order(8)
                        .build(),
                Category.builder()
                        .id("beverages")
                        .name("Napoje")
                        .color("#A3E4D7")
                        .icon("Beer")
                        .order(9)
                        .build(),
                Category.builder()
                        .id("canned")
                        .name("Produkty konserwowe")
                        .color("#D7BDE2")
                        .icon("Box")
                        .order(10)
                        .build(),
                Category.builder()
                        .id("frozen")
                        .name("Mrożonki")
                        .color("#85C1E9")
                        .icon("Snowflake")
                        .order(11)
                        .build(),
                Category.builder()
                        .id("snacks")
                        .name("Przekąski")
                        .color("#F8C471")
                        .icon("Cookie")
                        .order(12)
                        .build(),
                Category.builder()
                        .id("other")
                        .name("Inne")
                        .color("#CCD1D1")
                        .icon("Package")
                        .order(13)
                        .build()
        );
    }
}