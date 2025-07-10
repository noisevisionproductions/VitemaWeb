-- Tabela główna szablonów diet
CREATE TABLE diet_templates (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 1,

    -- Parametry diety
    duration INTEGER NOT NULL,
    meals_per_day INTEGER NOT NULL,
    meal_times TEXT, -- JSON
    meal_types TEXT, -- JSON

    -- Wartości odżywcze - używaj DOUBLE PRECISION bez scale
    target_calories DOUBLE PRECISION,
    target_protein DOUBLE PRECISION,
    target_fat DOUBLE PRECISION,
    target_carbs DOUBLE PRECISION,
    calculation_method VARCHAR(50),

    -- Metadane
    usage_count INTEGER NOT NULL DEFAULT 0,
    last_used TIMESTAMP,
    notes TEXT,

    CONSTRAINT chk_duration_positive CHECK (duration > 0),
    CONSTRAINT chk_meals_per_day_positive CHECK (meals_per_day > 0),
    CONSTRAINT chk_usage_count_non_negative CHECK (usage_count >= 0)
);

-- Indeksy dla tabeli głównej
CREATE INDEX idx_diet_templates_external_id ON diet_templates(external_id);
CREATE INDEX idx_diet_templates_created_by ON diet_templates(created_by);
CREATE INDEX idx_diet_templates_category ON diet_templates(category);
CREATE INDEX idx_diet_templates_usage_count ON diet_templates(usage_count DESC);
CREATE INDEX idx_diet_templates_created_at ON diet_templates(created_at DESC);
CREATE INDEX idx_diet_templates_name ON diet_templates(name);

-- Tabela dni szablonu
CREATE TABLE diet_template_days (
    id BIGSERIAL PRIMARY KEY,
    diet_template_id BIGINT NOT NULL,
    day_number INTEGER NOT NULL,
    day_name VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_diet_template_days_template
        FOREIGN KEY (diet_template_id) REFERENCES diet_templates(id) ON DELETE CASCADE,
    CONSTRAINT chk_day_number_positive CHECK (day_number > 0),
    CONSTRAINT uk_diet_template_day UNIQUE (diet_template_id, day_number)
);

-- Indeksy dla dni
CREATE INDEX idx_diet_template_days_template_id ON diet_template_days(diet_template_id);
CREATE INDEX idx_diet_template_days_day_number ON diet_template_days(day_number);

-- Tabela posiłków szablonu
CREATE TABLE diet_template_meals (
    id BIGSERIAL PRIMARY KEY,
    template_day_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    time VARCHAR(10) NOT NULL,
    instructions TEXT,
    meal_order INTEGER NOT NULL,

    -- Wartości odżywcze - DECIMAL jest OK dla BigDecimal
    calories DECIMAL(8,2),
    protein DECIMAL(8,2),
    fat DECIMAL(8,2),
    carbs DECIMAL(8,2),

    meal_template_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_diet_template_meals_day
        FOREIGN KEY (template_day_id) REFERENCES diet_template_days(id) ON DELETE CASCADE,
    CONSTRAINT chk_meal_order_non_negative CHECK (meal_order >= 0),
    CONSTRAINT uk_template_day_meal_order UNIQUE (template_day_id, meal_order)
);

-- Indeksy dla posiłków
CREATE INDEX idx_diet_template_meals_day_id ON diet_template_meals(template_day_id);
CREATE INDEX idx_diet_template_meals_order ON diet_template_meals(meal_order);
CREATE INDEX idx_diet_template_meals_type ON diet_template_meals(meal_type);
CREATE INDEX idx_diet_template_meals_name ON diet_template_meals(name);

-- Tabela składników szablonu
CREATE TABLE diet_template_ingredients (
    id BIGSERIAL PRIMARY KEY,
    template_meal_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    original_text TEXT,
    category_id VARCHAR(255),
    has_custom_unit BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_diet_template_ingredients_meal
        FOREIGN KEY (template_meal_id) REFERENCES diet_template_meals(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_display_order_non_negative CHECK (display_order >= 0)
);

-- Indeksy dla składników
CREATE INDEX idx_diet_template_ingredients_meal_id ON diet_template_ingredients(template_meal_id);
CREATE INDEX idx_diet_template_ingredients_name ON diet_template_ingredients(name);
CREATE INDEX idx_diet_template_ingredients_category ON diet_template_ingredients(category_id);
CREATE INDEX idx_diet_template_ingredients_order ON diet_template_ingredients(display_order);

-- Tabela zdjęć posiłków szablonu
CREATE TABLE diet_template_meal_photos (
    id BIGSERIAL PRIMARY KEY,
    template_meal_id BIGINT NOT NULL,
    photo_url VARCHAR(1000) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_diet_template_meal_photos_meal
        FOREIGN KEY (template_meal_id) REFERENCES diet_template_meals(id) ON DELETE CASCADE,
    CONSTRAINT chk_photo_display_order_non_negative CHECK (display_order >= 0)
);

-- Indeksy dla zdjęć
CREATE INDEX idx_diet_template_meal_photos_meal_id ON diet_template_meal_photos(template_meal_id);
CREATE INDEX idx_diet_template_meal_photos_order ON diet_template_meal_photos(display_order);

-- Dodanie komentarzy do tabel
COMMENT ON TABLE diet_templates IS 'Szablony diet do wielokrotnego użytku';
COMMENT ON TABLE diet_template_days IS 'Dni w szablonie diety';
COMMENT ON TABLE diet_template_meals IS 'Posiłki w dniach szablonu diety';
COMMENT ON TABLE diet_template_ingredients IS 'Składniki posiłków w szablonach diet';
COMMENT ON TABLE diet_template_meal_photos IS 'Zdjęcia posiłków w szablonach diet';

-- Sprawdzenie ograniczeń kategorii
ALTER TABLE diet_templates ADD CONSTRAINT chk_category_valid
    CHECK (category IN ('WEIGHT_LOSS', 'WEIGHT_GAIN', 'MAINTENANCE', 'SPORT', 'MEDICAL', 'VEGETARIAN', 'VEGAN', 'CUSTOM'));

-- Sprawdzenie ograniczeń typów posiłków
ALTER TABLE diet_template_meals ADD CONSTRAINT chk_meal_type_valid
    CHECK (meal_type IN ('BREAKFAST', 'SECOND_BREAKFAST', 'LUNCH', 'SNACK', 'DINNER'));