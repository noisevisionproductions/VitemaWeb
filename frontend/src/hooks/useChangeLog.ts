import { useEffect, useState } from "react";
import { ChangelogEntry } from "../types/changeLog";
import { useAuth } from "../contexts/AuthContext";
import { toast } from "sonner";
import { ChangelogService } from "../services/ChangelogService";

export const useChangeLog = () => {
    const [entries, setEntries] = useState<ChangelogEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [hasUnread, setHasUnread] = useState(false);
    const { currentUser } = useAuth();

    const fetchChangelog = async () => {
        try {
            const [entries, hasUnread] = await Promise.all([
                ChangelogService.getAllEntries(),
                currentUser ? ChangelogService.hasUnreadEntries() : Promise.resolve(false)
            ]);

            setEntries(entries);
            setHasUnread(hasUnread);
        } catch (error) {
            console.error('Error fetching changelog:', error);
            toast.error('Błąd podczas pobierania historii zmian');
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async () => {
        if (!currentUser) return;

        try {
            await ChangelogService.markAsRead();
            setHasUnread(false);
        } catch (error) {
            console.error('Error marking changelog as read:', error);
        }
    };

    const addEntry = async (data: Omit<ChangelogEntry, 'id' | 'createdAt' | 'author'>) => {
        try {
            if (!currentUser?.email) {
                toast.error('Musisz być zalogowany, aby dodać wpis');
                return;
            }

            await ChangelogService.createEntry(data);
            toast.success('Dodano nowy wpis do historii zmian');
            await fetchChangelog();
        } catch (error) {
            console.error('Error adding changelog entry:', error);
            toast.error('Błąd podczas dodawania wpisu');
        }
    };

    useEffect(() => {
        fetchChangelog().catch(console.error);
    }, []);

    return {
        entries,
        loading,
        addEntry,
        refresh: fetchChangelog,
        hasUnread,
        markAsRead
    };
};