# Product Database Refactor (V12)

**Scope:** PostgreSQL product table for ingredients + optional link from recipe ingredients.

- **Migration (V12):** `products` table (id, name unique, category nullable, kcal/protein/fat/carbs per 100g, is_verified); `recipe_ingredients.product_id` added (nullable FK to `products.id`, ON DELETE SET NULL).
- **Backend:** `ProductEntity`, `ProductRequest`, `ProductResponse`, `ProductJpaRepository`, `ProductDatabaseService` (create, findById, searchByName, delete). `RecipeIngredientEntity` has `ManyToOne` to `ProductEntity`; `RecipeJpaConverter` maps product link and snapshots product name into ingredient name. `RecipeIngredient` model has `productId`.
- **Delete behavior:** Deleting a product sets `recipe_ingredients.product_id` to null (DB FK); recipes are not deleted.
- **Usage:** When an ingredient is linked to a product, use the product’s macros for calculation; `name` on the ingredient is a snapshot for display.
