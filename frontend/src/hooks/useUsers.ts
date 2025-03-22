import { useCallback, useEffect, useState } from "react";
import { toast} from "../utils/toast";
import { User } from "../types/user";
import { UserService } from "../services/UserService";

export default function useUsers() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchUsers = async () => {
        try {
            const userData = await UserService.getAllUsers();
            setUsers(userData);
        } catch (error) {
            console.error('Error fetching users:', error);
            toast.error('Błąd podczas pobierania użytkowników');
        } finally {
            setLoading(false);
        }
    };

    const getUserById = useCallback(async (userId: string): Promise<User | null> => {
        try {
            return await UserService.getUserById(userId);
        } catch (error) {
            console.error('Error fetching user:', error);
            toast.error('Błąd podczas pobierania użytkownika');
            return null;
        }
    }, []);

    useEffect(() => {
        fetchUsers().catch(console.error);
    }, []);

    return { users, loading, fetchUsers, getUserById };
}