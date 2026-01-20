import {SetStateAction, useCallback, useEffect, useRef, useState} from "react";
import {toast} from "../utils/toast";
import {User} from "../types/user";
import {UserService} from "../services/UserService";

let usersCache: SetStateAction<User[]> | null = null;
let lastFetchTime = 0;
const CACHE_DURATION = 5 * 60 * 1000; // 5 minut

export default function useUsers() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const isMounted = useRef(true);

    const fetchUsers = useCallback(async (forceRefresh = false) => {
        const now = Date.now();

        if (!forceRefresh && usersCache && (now - lastFetchTime < CACHE_DURATION)) {
            setUsers(usersCache);
            setLoading(false);
            return;
        }

        setLoading(true);
        try {
            const userData = await UserService.getAllUsers();
            if (isMounted.current) {
                setUsers(userData);
                usersCache = userData;
                lastFetchTime = now;
            }
        } catch (error) {
            console.error('Error fetching users:', error);
            if (isMounted.current) {
                toast.error('Błąd podczas pobierania użytkowników');
            }
        } finally {
            if (isMounted.current) {
                setLoading(false);
            }
        }
    }, []);

    useEffect(() => {
        isMounted.current = true;
        fetchUsers().catch(console.error);

        return () => {
            isMounted.current = false;
        };
    }, [fetchUsers]);

    return {
        users, loading, fetchUsers, getUserById: useCallback(async (userId: string): Promise<User | null> => {
            // Najpierw szukaj w lokalnym stanie
            const cachedUser = users.find(user => user.id === userId);
            if (cachedUser) return cachedUser;

            try {
                return await UserService.getUserById(userId);
            } catch (error) {
                console.error('Error fetching user:', error);
                toast.error('Błąd podczas pobierania użytkownika');
                return null;
            }
        }, [users])
    };
}