CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    instructions TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    calories DOUBLE PRECISION,
    protein DOUBLE PRECISION,
    fat DOUBLE PRECISION,
    carbs DOUBLE PRECISION,
    parent_recipe_id VARCHAR(255)
);

CREATE TABLE recipe_photos (
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    photo_url VARCHAR(1000) NOT NULL
);

CREATE INDEX idx_recipe_external_id ON recipes(external_id);
CREATE INDEX idx_recipe_parent_id ON recipes(parent_recipe_id);