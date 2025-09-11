import {CardData, FormErrors, QuestionData, ScenarioFormData} from "../../types/scandallShuffle/scenario-creation";
import {ScenarioApiService} from "./ScenarioApiService";

/**
 * Service for validating scenario creation/update form data.
 * Adapted from React Native validation logic.
 */
export class ScenarioValidationService {
    /**
     * Public method to check if a scenario name already exists.
     * Can be used for real-time validation.
     * @param name The scenario name to check.
     * @param excludeScenarioId Optional. The ID of the current scenario to exclude from the check (used for updates).
     * @returns A promise that resolves to true if the name exists, otherwise false.
     */
    static async checkScenarioNameExists(name: string, excludeScenarioId?: string): Promise<boolean> {
        try {
            const scenarios = await ScenarioApiService.getAll();
            return scenarios.some(scenario =>
                scenario.name.toLowerCase() === name.toLowerCase() &&
                scenario.id !== excludeScenarioId
            );
        } catch (error) {
            console.error('Error checking scenario name availability:', error);
            // In case of an API error, we assume the name is not taken to avoid blocking the user.
            // The final validation on submit will catch this again.
            return false;
        }
    }

    static async validate(
        formData: ScenarioFormData,
        cards: CardData[],
        questions: QuestionData[],
        excludeScenarioId?: string
    ): Promise<FormErrors> {
        const newErrors: FormErrors = {};
        const generalErrors: string[] = [];

        // --- Basic field validation ---
        if (!formData.name?.trim()) {
            newErrors.name = 'Scenario name is required';
        } else if (formData.name.trim().length < 3) {
            newErrors.name = 'Name must be at least 3 characters';
        } else if (formData.name.trim().length > 100) {
            newErrors.name = 'Name must be less than 100 characters';
        } else {
            try {
                // This validation runs on final submission
                const nameExists = await this.checkScenarioNameExists(formData.name, excludeScenarioId);
                if (nameExists) {
                    newErrors.name = 'A scenario with this name already exists';
                }
            } catch (error) {
                console.error('Error checking scenario name:', error);
            }
        }
        if (!formData.description?.trim()) {
            newErrors.description = 'Description is required';
        } else if (formData.description.trim().length < 10) {
            newErrors.description = 'Description must be at least 10 characters';
        } else if (formData.description.trim().length > 500) {
            newErrors.description = 'Description must be less than 500 characters';
        }

        if (!formData.firstClue?.trim()) {
            newErrors.firstClue = 'First clue is required';
        } else if (formData.firstClue.trim().length < 10) {
            newErrors.firstClue = 'First clue must be at least 10 characters';
        }

        if (!formData.solution?.trim()) {
            newErrors.solution = 'Solution is required';
        } else if (formData.solution.trim().length < 20) {
            newErrors.solution = 'Solution must be at least 20 characters';
        } else if (formData.solution.trim().length > 1000) {
            newErrors.solution = 'Solution must be less than 1000 characters';
        }

        if (!formData.duration) {
            newErrors.duration = 'Duration is required';
        } else if (formData.duration < 15 || formData.duration > 300) {
            newErrors.duration = 'Duration must be between 15 and 300 minutes';
        }

        if (cards.length < 15) {
            generalErrors.push(`At least 15 cards are required (currently ${cards.length})`);
        }
        if (cards.length > 100) {
            generalErrors.push('Too many cards (maximum 100)');
        }
        if (questions.length === 0) {
            generalErrors.push('At least one question is required');
        } else if (questions.length > 20) {
            generalErrors.push('Too many questions (maximum 20)');
        }

        if (cards.some(card => !card.content.trim())) {
            generalErrors.push('All cards must have content. Empty cards are not allowed.');
        } else {
            const cardContents = cards.map(card => card.content.trim().toLowerCase());
            const uniqueCardContents = new Set(cardContents);
            if (uniqueCardContents.size < cardContents.length) {
                generalErrors.push('Each card must have unique content. Duplicate content found.');
            }
        }

        if (questions.some(q =>
            !q.question.trim() ||
            q.options.some(opt => !opt.trim())
        )) {
            generalErrors.push('All questions must have text, and all their answer options must be filled in.');
        }

        if (generalErrors.length > 0) {
            newErrors.general = generalErrors.join('\n');
        }

        return newErrors;
    }
}
