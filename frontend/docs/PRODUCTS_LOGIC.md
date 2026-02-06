## 2. Frontend Logic

The frontend interacts with the new SQL-backed endpoints, treating IDs as strings (compatible with the numeric IDs
returned by SQL).

### Services (`ProductService.ts`)

* **Search:**
    * The `searchProducts` method now accepts an optional `trainerId`.
    * **Crucial:** This `trainerId` MUST be passed in every search call (e.g., in the Diet Creator). If omitted, the
      user will only see Global products.
    *
* **Creation:**
    * `createCustomProduct` sends the `trainerId` as a query parameter (`POST /api/products?trainerId=...`). This
      ensures the backend flags it as `CUSTOM`.
    *

### UI Components (`InlineIngredientSearch` & `DietCreator`)

* **Search Flow:**
    1. User types in the search bar.
    2. Component debounces input and calls API with `query` + `currentUser.uid`.
    3. Results (mixed Global and Custom) are displayed in a single list.
    4.
* **ID Handling:**
    * The frontend expects IDs to be strings. The backend now returns numbers (e.g., `155`), which are converted to
      strings in the DTO mapping layer (`String.valueOf(id)`) before reaching the UI.
    *