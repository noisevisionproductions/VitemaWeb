-- 1. Add author_id safely (only if it doesn't exist)
ALTER TABLE products ADD COLUMN IF NOT EXISTS author_id VARCHAR(255);

-- 2. Add type safely (only if it doesn't exist)
ALTER TABLE products ADD COLUMN IF NOT EXISTS type VARCHAR(50);

-- 3. CRITICAL: Update existing rows so they aren't NULL
UPDATE products SET type = 'GLOBAL' WHERE type IS NULL;

-- 4. Now that data is safe, enforce the NOT NULL constraint
ALTER TABLE products ALTER COLUMN type SET NOT NULL;