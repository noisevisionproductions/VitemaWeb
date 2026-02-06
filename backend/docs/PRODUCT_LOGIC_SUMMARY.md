# Product Logic Summary (PostgreSQL Migration)

## 1. Backend Logic

The backend has been migrated from Firestore to PostgreSQL to support relational queries and strict ownership.

### Core Components

* **Database:** Products are stored in the `products` table in PostgreSQL.
* **Entity:** `ProductEntity` maps to the table. Key fields:
    * `id`: Long (Auto-incremented primary key).
    * `type`: Enum (`GLOBAL` or `CUSTOM`).
    * `author_id`: String (Stores the Trainer's UID for custom products; NULL for global).
    *

### Search Logic (`ProductService` & `ProductDatabaseService`)

* **Unified Access:** The system no longer queries Firestore for products. All searches go through
  `ProductDatabaseService`.
* **Security & Filtering:**
    * When searching, the system requires a `trainerId`.
    * **Query Logic:**
      `SELECT * FROM products WHERE name LIKE %query% AND (type = 'GLOBAL' OR (type = 'CUSTOM' AND author_id = :trainerId))`
    * This ensures trainers see System products + their *own* private products, but never another trainer's private
      products.
    *

### Creation & Deletion

* **Creation:**
    * If `trainerId` is provided in the request -> Product is saved as `CUSTOM` with `authorId = trainerId`.
    * If no `trainerId` -> Product is saved as `GLOBAL` (System Admin only).
    *
* **Deletion:**
    * Strict check: A user can only delete a product if `type == CUSTOM` AND `authorId == currentUserId`.
    * Global products are protected from deletion by trainers.
    *
