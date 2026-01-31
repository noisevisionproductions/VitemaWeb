ALTER TABLE recipes ADD COLUMN author_id VARCHAR(255);
ALTER TABLE recipes ADD COLUMN is_public BOOLEAN DEFAULT TRUE;

CREATE INDEX idx_recipes_author ON recipes(author_id);
CREATE INDEX idx_recipes_public ON recipes(is_public);