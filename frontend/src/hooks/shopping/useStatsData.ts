import { useState, useEffect } from 'react';
import { Diet } from '../../types';
import { DietService } from '../../services/diet/DietService';
import { formatMonthYear } from '../../utils/dateFormatters';
import useUsers from '../../hooks/useUsers';

export interface MonthlyStats {
    month: string;
    count: number;
    cumulativeCount?: number;
}

export interface UserStats {
    name: string;
    value: number;
}

export interface MealTypeStats {
    name: string;
    value: number;
}

export interface DietLengthStats {
    range: string;
    count: number;
}

export interface StatsData {
    // Podstawowe metryki
    totalUsers: number;
    activeUsers: number;
    averageDietDays: number;
    totalDiets: number;

    // Trendy
    userGrowthRate: number;  // Wzrost procentowy użytkowników w ciągu ostatniego miesiąca
    dietGrowthRate: number;  // Wzrost procentowy diet w ciągu ostatniego miesiąca

    // Dane do wykresów
    usersData: UserStats[];
    monthlyData: MonthlyStats[];
    mealsTypeData: MealTypeStats[];
    dietLengthDistribution: DietLengthStats[];

    // Wskaźniki wydajności
    dietPerUserRate: number; // Liczba diet na użytkownika
    completionRate: number;  // Procent zakończonych diet
}

export const useStatsData = () => {
    const { users, loading: usersLoading } = useUsers();
    const [diets, setDiets] = useState<Diet[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);
    const [statsData, setStatsData] = useState<StatsData | null>(null);

    // Pobieranie danych
    useEffect(() => {
        const fetchStats = async () => {
            try {
                setLoading(true);
                const dietsData = await DietService.getDiets();
                setDiets(dietsData);
                setError(null);
            } catch (error) {
                console.error('Error fetching diets:', error);
                setError(error as Error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats().catch(console.error);
    }, []);

    // Przetwarzanie danych statystycznych
    useEffect(() => {
        if (loading || usersLoading || !diets.length) return;

        // Aktywni użytkownicy (z dietą)
        const activeUsersSet = new Set(diets.map(diet => diet.userId));
        const activeUsersCount = activeUsersSet.size;

        // Średnia liczba dni w dietach
        const averageDietDays = Math.round(
            diets.reduce((acc, diet) => acc + (diet.days?.length || 0), 0) / diets.length
        );

        // Dane do wykresu użytkowników
        const usersData: UserStats[] = [
            { name: 'Wszyscy użytkownicy', value: users.length },
            { name: 'Aktywni użytkownicy', value: activeUsersCount },
            { name: 'Nieaktywni użytkownicy', value: users.length - activeUsersCount }
        ];

        // Dane do wykresu miesięcznego przyrostu diet
        const monthsMap: { [key: string]: { count: number, cumulativeCount: number } } = {};

        // Sortowanie diet według daty utworzenia
        const sortedDiets = [...diets].sort((a, b) => {
            if (!a.createdAt || !b.createdAt) return 0;
            return a.createdAt.seconds - b.createdAt.seconds;
        });

        // Obliczanie przyrostu miesięcznego
        let cumulative = 0;
        sortedDiets.forEach(diet => {
            if (!diet.createdAt) return;

            const date = new Date(diet.createdAt.seconds * 1000);
            const monthKey = formatMonthYear(date);

            if (!monthsMap[monthKey]) {
                monthsMap[monthKey] = { count: 0, cumulativeCount: 0 };
            }

            monthsMap[monthKey].count++;
            cumulative++;
            monthsMap[monthKey].cumulativeCount = cumulative;
        });

        // Konwersja do formatu do wyświetlenia
        const monthlyData: MonthlyStats[] = Object.entries(monthsMap)
            .map(([month, stats]) => ({
                month,
                count: stats.count,
                cumulativeCount: stats.cumulativeCount
            }))
            .sort((a, b) => {
                const [monthA, yearA] = a.month.split('/').map(Number);
                const [monthB, yearB] = b.month.split('/').map(Number);
                return yearA !== yearB ? yearA - yearB : monthA - monthB;
            });

        // Dane do wykresu typów posiłków
        const mealTypesCount: Record<string, number> = {};
        diets.forEach(diet => {
            diet.days?.forEach(day => {
                day.meals?.forEach(meal => {
                    const mealType = meal.mealType;
                    mealTypesCount[mealType] = (mealTypesCount[mealType] || 0) + 1;
                });
            });
        });

        const mealsTypeData: MealTypeStats[] = Object.entries(mealTypesCount)
            .map(([name, value]) => ({
                name: formatMealType(name),
                value
            }))
            .sort((a, b) => b.value - a.value);

        // Dane do wykresu rozkładu długości diet
        const dietLengths: number[] = diets.map(diet => diet.days?.length || 0);
        const dietLengthMap: Record<string, number> = {
            "1-7 dni": 0,
            "8-14 dni": 0,
            "15-30 dni": 0,
            "31-60 dni": 0,
            "60+ dni": 0
        };

        dietLengths.forEach(length => {
            if (length <= 7) dietLengthMap["1-7 dni"]++;
            else if (length <= 14) dietLengthMap["8-14 dni"]++;
            else if (length <= 30) dietLengthMap["15-30 dni"]++;
            else if (length <= 60) dietLengthMap["31-60 dni"]++;
            else dietLengthMap["60+ dni"]++;
        });

        const dietLengthDistribution: DietLengthStats[] = Object.entries(dietLengthMap)
            .map(([range, count]) => ({ range, count }));

        // Obliczanie trendów wzrostu
        const calculateGrowthRate = (data: MonthlyStats[]): number => {
            if (data.length < 2) return 0;

            const lastMonth = data[data.length - 1];
            const previousMonth = data[data.length - 2];

            if (previousMonth.count === 0) return 100; // Jeśli poprzedni miesiąc miał 0, to wzrost jest 100%

            return Math.round(((lastMonth.count - previousMonth.count) / previousMonth.count) * 100);
        };

        const dietGrowthRate = calculateGrowthRate(monthlyData);

        // Fake user growth rate (w rzeczywistości potrzebowalibyśmy danych o rejestracji użytkowników)
        const userGrowthRate = 5; // 5% miesięcznie (przykładowa wartość)

        // Wskaźnik diet na użytkownika
        const dietPerUserRate = activeUsersCount > 0
            ? parseFloat((diets.length / activeUsersCount).toFixed(2))
            : 0;

        // Wskaźnik ukończenia diet (przykładowy-w rzeczywistości potrzebujemy więcej danych)
        const completionRate = 75; // Zakładamy 75% ukończenia (przykładowa wartość)

        // Kompletne dane statystyczne
        setStatsData({
            totalUsers: users.length,
            activeUsers: activeUsersCount,
            averageDietDays,
            totalDiets: diets.length,
            userGrowthRate,
            dietGrowthRate,
            usersData,
            monthlyData,
            mealsTypeData,
            dietLengthDistribution,
            dietPerUserRate,
            completionRate
        });

    }, [diets, loading, users, usersLoading]);

    return {
        loading: loading || usersLoading,
        error,
        stats: statsData
    };
};

// Funkcje pomocnicze
function formatMealType(mealType: string): string {
    const typeMap: Record<string, string> = {
        "BREAKFAST": "Śniadanie",
        "SECOND_BREAKFAST": "Drugie śniadanie",
        "LUNCH": "Obiad",
        "SNACK": "Przekąska",
        "DINNER": "Kolacja",
        "SUPPER": "Kolacja II"
    };

    return typeMap[mealType] || mealType;
}