import {useEffect, useState} from "react";
import {DietService} from "../services/DietService";
import {toast} from "sonner";

interface DietInfo {
    hasDiet: boolean;
    startDate: string | null;
    endDate: string | null;
}

export interface UserDietInfo {
    [userId: string]: DietInfo;
}

export interface LoadingStates {
    [userId: string]: boolean;
}

export const useDietInfo = (userIds: string[]) => {
    const [dietInfo, setDietInfo] = useState<UserDietInfo>({});
    const [loading, setLoading] = useState(true);
    const [loadingStates, setLoadingStates] = useState<LoadingStates>({});

    useEffect(() => {
        const fetchDietInfo = async () => {
            if (!userIds.length) {
                setDietInfo({});
                setLoadingStates({});
                setLoading(false);
                return;
            }

            const initialLoadingStates = userIds.reduce((acc, userId) => {
                acc[userId] = true;
                return acc;
            }, {} as LoadingStates);
            setLoadingStates(initialLoadingStates);

            try {
                const dietInfoMap = await DietService.getDietsInfoForUsers(userIds);
                setDietInfo(dietInfoMap);
            } catch (error) {
                console.error('Error fetching diet info:', error);
                toast.error('Błąd podczas pobierania informacji o dietach');
            } finally {
                const completedLoadingStates = userIds.reduce((acc, userId) => {
                    acc[userId] = false;
                    return acc;
                }, {} as LoadingStates);
                setLoadingStates(completedLoadingStates);
                setLoading(false);
            }
        };

        setLoading(true);
        fetchDietInfo().catch(() => {
            setLoading(false);
            setLoadingStates({});
        });
    }, [userIds]);

    return {dietInfo, loading, loadingStates};
};