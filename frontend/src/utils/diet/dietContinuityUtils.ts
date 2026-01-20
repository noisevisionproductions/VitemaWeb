import {Timestamp} from "firebase/firestore";
import {Diet} from "../../types";
import {getFirstDayOfDiet, getLastDayOfDiet, getTimestampSeconds} from "./dietWarningUtils";

export interface DietContinuityStatus {
    hasFutureDiet: boolean;
    gapDays: number;
    nextDietId?: string;
    nextDietStartTimestamp?: Timestamp | null;
}

/*
* Sprawdza, czy użytkownik ma przypisaną przyszłą dietę po zakończeniu bieżącej
* */
export function checkFutureDiets(currentDiet: Diet, allDiets: Diet[]): DietContinuityStatus {
    const lastDayTimestamp = getLastDayOfDiet(currentDiet);

    if (!lastDayTimestamp) {
        return {hasFutureDiet: false, gapDays: -1};
    }

    const otherUserDiets = allDiets.filter(diet =>
        diet.userId === currentDiet.userId && diet.id !== currentDiet.id
    );

    let closestFutureDiet: Diet | null = null;
    let minGapDays = Number.MAX_SAFE_INTEGER;

    for (const diet of otherUserDiets) {
        const firstDayTimestamp = getFirstDayOfDiet(diet);

        if (!firstDayTimestamp) continue;

        const currentDietEndDate = new Date(getTimestampSeconds(lastDayTimestamp) * 1000);
        const dietStartDate = new Date(getTimestampSeconds(firstDayTimestamp) * 1000);

        currentDietEndDate.setHours(0, 0, 0, 0);
        dietStartDate.setHours(0, 0, 0, 0);

        const timeDiff = dietStartDate.getTime() - currentDietEndDate.getTime();
        const dayDiff = Math.round(timeDiff / (1000 * 3600 * 24));

        if (dayDiff >= -1) {
            if (dayDiff < minGapDays) {
                minGapDays = dayDiff;
                closestFutureDiet = diet;
            }
        }
    }

    if (closestFutureDiet) {
        const firstDayTimestamp = getFirstDayOfDiet(closestFutureDiet);

        return {
            hasFutureDiet: true,
            gapDays: minGapDays,
            nextDietId: closestFutureDiet.id,
            nextDietStartTimestamp: firstDayTimestamp
        };
    }

    return {hasFutureDiet: false, gapDays: -1};
}

/**
 * Sprawdza, czy istnieje przerwa między dietami użytkownika
 */
export function hasDietGap(currentDiet: Diet, allDiets: Diet[]): boolean {
    const continuity = checkFutureDiets(currentDiet, allDiets);
    return !continuity.hasFutureDiet || continuity.gapDays > 1;
}