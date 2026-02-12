# Dokumentacja Logiki Listy Zakupów (Backend)

## Przegląd
Moduł listy zakupów odpowiada za automatyczne generowanie, przechowywanie i aktualizowanie list zakupów na podstawie planów dietetycznych użytkowników. Dane są przechowywane w bazie NoSQL (Firestore) w kolekcji `shopping_lists`, ale korzystają z referencji do relacyjnej bazy produktów (PostgreSQL).

## Kluczowe Serwisy

### 1. ShoppingListGeneratorService
Główny serwis odpowiedzialny za konwersję diety (`Diet`) na strukturę listy zakupów.

**Algorytm generowania:**
1.  **Ekstrakcja składników:** Serwis iteruje przez wszystkie dni i posiłki w diecie, wyciągając składniki (`RecipeIngredient`).
2.  **Wzbogacanie danych (PostgreSQL):**
    * Dla każdego składnika sprawdza, czy posiada `productId`.
    * Jeśli tak -> pobiera oficjalną nazwę, kategorię i jednostkę z bazy SQL (`ProductEntity`).
    * Jeśli nie -> używa nazwy surowej z przepisu i próbuje przypisać kategorię (lub wpada do "inne").
3.  **Agregacja:**
    * Składniki są grupowane według klucza: `Nazwa + Jednostka` (np. "Pomidor_kg" i "Pomidor_szt" to osobne pozycje).
    * Ilości są sumowane.
4.  **Śledzenie użycia (Context):**
    * Każdy agregowany produkt przechowuje listę `ShoppingListRecipeReference` – informację, do jakiego przepisu/posiłku jest potrzebny (np. "Jajecznica - Dzień 1").

### 2. ShoppingListService
Zarządza operacjami CRUD na kolekcji `shopping_lists` oraz cache'owaniem.

* **Cache:** Używa adnotacji `@Cacheable` (`shoppingListCache`) kluczowanej po `dietId`, aby zminimalizować odczyty z Firestore.
* **Struktura:** Przechowuje mapę, gdzie kluczem jest ID kategorii (np. `warzywa_i_owoce`), a wartością lista obiektów `CategorizedShoppingListItem`.

## Model Danych (Firestore)

Dokument w kolekcji `shopping_lists`:

```json
{
  "id": "auto-gen-id",
  "dietId": "diet-id-ref",
  "userId": "user-uid",
  "startDate": Timestamp,
  "endDate": Timestamp,
  "version": 4,
  "items": {
    "warzywa_i_owoce": [
      {
        "name": "Pomidor",
        "quantity": 2.5,
        "unit": "kg",
        "original": "Pomidor malinowy",
        "recipes": [
          {
            "recipeName": "Sałatka grecka",
            "dayIndex": 0,
            "mealType": "DINNER"
          }
        ]
      }
    ],
    "nabial": [ ... ]
  }
}