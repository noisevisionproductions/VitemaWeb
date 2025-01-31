import {useEffect, useState} from "react";
import {collection, getDocs, query, Timestamp, where} from "firebase/firestore";
import {db} from "../config/firebase";
import {Diet} from "../types/diet";

interface DietInfo {
    hasDiet: boolean;
    startDate: string | null;
    endDate: string | null;
}

export interface UserDietInfo {
    [userId: string]: DietInfo;
}

const compareDates = (dateA: Timestamp, dateB: Timestamp): number => {
    return dateA.seconds - dateB.seconds;
};

export const useDietInfo = (userIds: string[]) => {
    const [dietInfo, setDietInfo] = useState<UserDietInfo>({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDietInfo = async () => {
            if (!userIds.length) {
                setDietInfo({});
                setLoading(false);
                return;
            }

            try {
                const dietsRef = collection(db, 'diets');
                const dietInfoMap: UserDietInfo = {};

                for (const userId of userIds) {
                    const q = query(dietsRef, where('userId', '==', userId));
                    const querySnapshot = await getDocs(q);

                    if (!querySnapshot.empty) {
                        let earliestDate: Timestamp | null = null;
                        let latestDate: Timestamp | null = null;

                        querySnapshot.docs.forEach((doc) => {
                            const diet = doc.data() as Diet;
                            if (diet.days && diet.days.length > 0) {
                                const dates = diet.days.map(day => day.date);

                                const minDate = dates.reduce((a, b) =>
                                    compareDates(a, b) <= 0 ? a : b
                                );

                                const maxDate = dates.reduce((a, b) =>
                                    compareDates(a, b) >= 0 ? a : b
                                );

                                if (!earliestDate || compareDates(minDate, earliestDate) < 0) {
                                    earliestDate = minDate;
                                }
                                if (!latestDate || compareDates(maxDate, latestDate) > 0) {
                                    latestDate = maxDate;
                                }
                            }
                        });

                        dietInfoMap[userId] = {
                            hasDiet: true,
                            startDate: earliestDate,
                            endDate: latestDate
                        };
                    } else {
                        dietInfoMap[userId] = {
                            hasDiet: false,
                            startDate: null,
                            endDate: null
                        };
                    }
                }

                setDietInfo(dietInfoMap);
            } catch (error) {
                console.error('Error fetching diet info:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDietInfo().catch();
    }, [userIds]);

    return {dietInfo, loading};
};