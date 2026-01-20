import {MealSuggestion, TemplateChange, TemplateUpdateSummary} from "../../../types/mealSuggestions";

export class TemplateChangeTracker {

    private originalTemplate: MealSuggestion | null = null;
    private changes: TemplateChange[] = [];

    public startTracking(template: MealSuggestion): void {
        this.originalTemplate = JSON.parse(JSON.stringify(template));
        this.changes = [];
    }

    public getUpdateSummary(): TemplateUpdateSummary | null {
        if (!this.originalTemplate || this.changes.length === 0) {
            return null;
        }

        const significantFields = ['name', 'instructions', 'ingredients', 'nutritionalValues', 'photos'];
        const hasSignificantChanges = this.changes.some(change =>
            significantFields.includes(change.field)
        );

        return {
            templateId: this.originalTemplate.id,
            templateName: this.originalTemplate.name,
            source: this.originalTemplate.source,
            changes: this.changes,
            hasSignificantChanges
        };
    }

    public detectChanges(currentMeal: {
        name: string;
        instructions?: string;
        ingredients?: any[];
        nutritionalValues?: any;
        photos?: string[];
    }): void {
        if (!this.originalTemplate) return;

        this.changes = [];

        // Porównaj nazwę
        if (this.originalTemplate.name !== currentMeal.name) {
            this.changes.push({
                field: 'name',
                oldValue: this.originalTemplate.name,
                newValue: currentMeal.name,
                timestamp: new Date()
            });
        }

        // Porównaj instrukcje
        if (this.originalTemplate.instructions !== currentMeal.instructions) {
            this.changes.push({
                field: 'instructions',
                oldValue: this.originalTemplate.instructions,
                newValue: currentMeal.instructions,
                timestamp: new Date()
            });
        }

        // Porównaj składniki (uproszczona wersja)
        const originalIngredientsStr = JSON.stringify(this.originalTemplate.ingredients || []);
        const currentIngredientsStr = JSON.stringify(currentMeal.ingredients || []);
        if (originalIngredientsStr !== currentIngredientsStr) {
            this.changes.push({
                field: 'ingredients',
                oldValue: this.originalTemplate.ingredients,
                newValue: currentMeal.ingredients,
                timestamp: new Date()
            });
        }

        // Porównaj wartości odżywcze
        const originalNutritionStr = JSON.stringify(this.originalTemplate.nutritionalValues || {});
        const currentNutritionStr = JSON.stringify(currentMeal.nutritionalValues || {});
        if (originalNutritionStr !== currentNutritionStr) {
            this.changes.push({
                field: 'nutritionalValues',
                oldValue: this.originalTemplate.nutritionalValues,
                newValue: currentMeal.nutritionalValues,
                timestamp: new Date()
            });
        }

        // Porównaj zdjęcia
        const originalPhotosStr = JSON.stringify(this.originalTemplate.photos || []);
        const currentPhotosStr = JSON.stringify(currentMeal.photos || []);
        if (originalPhotosStr !== currentPhotosStr) {
            this.changes.push({
                field: 'photos',
                oldValue: this.originalTemplate.photos,
                newValue: currentMeal.photos,
                timestamp: new Date()
            });
        }
    }

    public reset(): void {
        this.originalTemplate = null;
        this.changes = [];
    }
}