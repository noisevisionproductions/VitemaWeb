-- Product database for ingredients (per 100g macros)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    kcal DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    fat DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category);

-- Link recipe ingredients to products (hybrid: keep name/quantity/unit, add optional product_id)
ALTER TABLE recipe_ingredients
    ADD COLUMN product_id BIGINT NULL,
    ADD CONSTRAINT fk_recipe_ingredients_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;

CREATE INDEX idx_recipe_ingredients_product_id ON recipe_ingredients(product_id);
