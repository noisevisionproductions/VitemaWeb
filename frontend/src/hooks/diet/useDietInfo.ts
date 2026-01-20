import {useCallback, useEffect, useRef, useState} from "react";
import {DietService} from "../../services/diet/DietService";
import {toast} from "../../utils/toast";

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

interface CacheEntry {
    data: UserDietInfo;
    timestamp: number;
}

const dietInfoCache: Map<string, CacheEntry> = new Map();
const CACHE_DURATION = 5 * 60 * 1000; // 5 minut

export const useDietInfo = (userIds: string[]) => {
    const [dietInfo, setDietInfo] = useState<UserDietInfo>({});
    const [loading, setLoading] = useState(true);
    const [loadingStates, setLoadingStates] = useState<LoadingStates>({});
    const isMounted = useRef(true);

    const cacheKey = [...userIds].sort().join(',');

    const userIdsRef = useRef<string[]>(userIds);
    userIdsRef.current = userIds;

    const fetchDietInfo = useCallback(async (forceRefresh = false) => {
        if (!userIds.length) {
            if (isMounted.current) {
                setDietInfo({});
                setLoadingStates({});
                setLoading(false);
            }
            return;
        }

        const now = Date.now();
        const cached = dietInfoCache.get(cacheKey);

        if (!forceRefresh && cached && (now - cached.timestamp < CACHE_DURATION)) {
            if (isMounted.current) {
                setDietInfo(cached.data);
                setLoadingStates(
                    userIds.reduce((acc, userId) => {
                        acc[userId] = false;
                        return acc;
                    }, {} as LoadingStates)
                );
                setLoading(false);
            }
            return;
        }

        if (isMounted.current) {
            const initialLoadingStates = userIds.reduce((acc, userId) => {
                acc[userId] = true;
                return acc;
            }, {} as LoadingStates);
            setLoadingStates(initialLoadingStates);
            setLoading(true);
        }

        try {
            const dietInfoMap = await DietService.getDietsInfoForUsers(userIds);

            dietInfoCache.set(cacheKey, {
                data: dietInfoMap,
                timestamp: now
            });

            if (isMounted.current) {
                setDietInfo(dietInfoMap);
            }
        } catch (error) {
            console.error('Error fetching diet info:', error);
            if (isMounted.current) {
                toast.error('Błąd podczas pobierania informacji o dietach');
            }
        } finally {
            if (isMounted.current) {
                const completedLoadingStates = userIds.reduce((acc, userId) => {
                    acc[userId] = false;
                    return acc;
                }, {} as LoadingStates);
                setLoadingStates(completedLoadingStates);
                setLoading(false);
            }
        }
    }, [userIds, cacheKey]);

    useEffect(() => {
        const cleanupInterval = setInterval(() => {
            const now = Date.now();
            dietInfoCache.forEach((entry, key) => {
                if (now - entry.timestamp > CACHE_DURATION) {
                    dietInfoCache.delete(key);
                }
            });
        }, CACHE_DURATION);

        return () => clearInterval(cleanupInterval);
    }, []);

    useEffect(() => {
        isMounted.current = true;
        fetchDietInfo().catch(console.error);

        return () => {
            isMounted.current = false;
        };
    }, [fetchDietInfo]);

    return {
        dietInfo,
        loading,
        loadingStates,
        userIds: userIdsRef.current
    };
};