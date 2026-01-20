import React, { useEffect, useState } from "react";
import { useChangeLog } from "../../../../../../hooks/useChangeLog";
import LoadingSpinner from "../../../../../shared/common/LoadingSpinner";
import { PlusCircle } from "lucide-react";
import ChangelogForm from "../../../../changelog/ChangelogForm";
import ChangelogList from "../../../../changelog/ChangelogList";
import SectionHeader from "../../../../../shared/common/SectionHeader";

const Changelog: React.FC = () => {
    const { entries, loading, addEntry, hasUnread, markAsRead } = useChangeLog();
    const [isAddingEntry, setIsAddingEntry] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (hasUnread) {
            markAsRead().catch(console.error);
        }
    }, [hasUnread]);

    const handleSubmit = async (data: any) => {
        setIsSubmitting(true);
        try {
            await addEntry(data);
            setIsAddingEntry(false);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <SectionHeader
                title="Historia zmian"
                description="Informacje o najnowszych zmianach"
                rightContent={
                    <button
                        onClick={() => setIsAddingEntry(!isAddingEntry)}
                        className="flex items-center gap-2 px-4 py-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    >
                        <PlusCircle className="w-5 h-5" />
                        {isAddingEntry ? 'Anuluj' : 'Dodaj wpis'}
                    </button>
                }
            />

            {isAddingEntry && (
                <div className="bg-white p-6 rounded-lg shadow-sm">
                    <ChangelogForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
                </div>
            )}

            <ChangelogList entries={entries} />
        </div>
    );
};

export default Changelog;