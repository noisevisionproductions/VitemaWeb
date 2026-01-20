import React, {useEffect, useState} from "react";
import {ExternalRecipient} from "../../../../../types/sendGrid";
import {toast} from "../../../../../utils/toast";
import {PlusCircle} from "lucide-react";
import LoadingSpinner from "../../../../shared/common/LoadingSpinner";
import {DialogContent, Dialog, DialogHeader, DialogTitle, DialogDescription} from "../../../../shared/ui/Dialog";
import RecipientsList from "./RecipientsList";
import AddRecipientForm from "./AddRecipientForm";
import {ExternalRecipientService} from "../../../../../services/newsletter/ExternalRecipientService";

const ExternalRecipientsManager: React.FC = () => {
    const [recipients, setRecipients] = useState<ExternalRecipient[]>([]);
    const [loading, setLoading] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [categories, setCategories] = useState<string[]>([]);
    const [filterCategory, setFilterCategory] = useState<string>('all');
    const [filterStatus, setFilterStatus] = useState<string>('all');
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchRecipients().catch(console.error);
        fetchCategories().catch(console.error);
    }, []);

    const fetchRecipients = async () => {
        try {
            setLoading(true);
            const data = await ExternalRecipientService.getAllRecipients();
            setRecipients(data);
        } catch (error) {
            console.error('Error fetching external recipients:', error);
            toast.error('Nie udało się pobrać listy odbiorców');
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const data = await ExternalRecipientService.getCategories();
            setCategories(data.categories);
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    };

    const handleAddRecipient = async (formData: any) => {
        try {
            await ExternalRecipientService.addRecipient(formData);
            toast.success('Dodano nowego odbiorcę');
            setIsAddModalOpen(false);
            fetchRecipients().catch(console.error);
        } catch (error) {
            console.error('Error adding recipient:', error);
            toast.error('Nie udało się dodać odbiorcy');
        }
    };

    const handleUpdateStatus = async (id: string, status: ExternalRecipient['status']) => {
        try {
            await ExternalRecipientService.updateStatus(id, status);
            toast.success('Zaktualizowano status odbiorcy');
            fetchRecipients().catch(console.error);
        } catch (error) {
            console.error('Error updating recipient status:', error);
            toast.error('Nie udało się zaktualizować statusu');
        }
    };

    const handleDeleteRecipient = async (id: string) => {
        try {
            await ExternalRecipientService.deleteRecipient(id);
            toast.success('Usunięto odbiorcę');
            fetchRecipients().catch(console.error)
        } catch (error) {
            console.error('Error deleting recipient:', error);
            toast.error('Nie udało się usunąć odbiorcy');
        }
    };

    const filteredRecipients = recipients.filter(recipient => {
        if (filterCategory !== 'all' && recipient.category !== filterCategory) return false;
        if (filterStatus !== 'all' && recipient.status !== filterStatus) return false;
        if (searchTerm) {
            const searchLower = searchTerm.toLowerCase();
            return (
                recipient.email.toLowerCase().includes(searchLower) ||
                (recipient.name && recipient.name.toLowerCase().includes(searchLower))
            );
        }
        return true;
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap justify-between items-center mb-6">
                <h2 className="text-xl font-semibold">Zewnętrzni odbiorcy</h2>
                <div className="flex space-x-2">
                    <button
                        onClick={() => setIsAddModalOpen(true)}
                        className="inline-flex items-center px-3 py-2 bg-primary text-white rounded-md hover:bg-primary-dark"
                    >
                        <PlusCircle size={18} className="mr-1"/>
                        Dodaj odbiorcę
                    </button>
                </div>
            </div>

            <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                <div className="flex flex-wrap gap-3 mb-4">
                    <select
                        value={filterCategory}
                        onChange={(e) => setFilterCategory(e.target.value)}
                        className="border border-gray-300 rounded-md px-3 py-2 pr-8 text-sm min-w-[180px]"
                    >
                        <option value="all">Wszystkie kategorie</option>
                        {categories.map(category => (
                            <option key={category} value={category}>{category}</option>
                        ))}
                    </select>

                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="border border-gray-300 rounded-md px-3 py-2 pr-8 text-sm min-w-[180px]"
                    >
                        <option value="all">Wszystkie statusy</option>
                        <option value="new">Nowy</option>
                        <option value="contacted">Skontaktowano</option>
                        <option value="responded">Odpowiedział</option>
                        <option value="subscribed">Zapisany</option>
                        <option value="rejected">Odrzucony</option>
                    </select>

                    <input
                        type="text"
                        placeholder="Szukaj..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="border border-gray-300 rounded-md px-3 py-2 text-sm flex-grow"
                    />

                    <button
                        onClick={fetchRecipients}
                        className="ml-auto px-3 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
                    >
                        Odśwież
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center items-center py-12">
                        <LoadingSpinner/>
                    </div>
                ) : (
                    <RecipientsList
                        recipients={filteredRecipients}
                        onUpdateStatus={handleUpdateStatus}
                        onDelete={handleDeleteRecipient}
                    />
                )}
            </div>

            {/* Modal dodawania odbiorcy */}
            <Dialog open={isAddModalOpen} onOpenChange={setIsAddModalOpen}>
                <DialogContent className="sm:max-w-lg">
                    <DialogHeader>
                        <DialogTitle>Dodaj nowego odbiorcę</DialogTitle>
                        <DialogDescription>
                            Wprowadź dane nowego odbiorcy, którego chcesz dodać do listy.
                        </DialogDescription>
                    </DialogHeader>
                    <AddRecipientForm
                        onSubmit={handleAddRecipient}
                        onCancel={() => setIsAddModalOpen(false)}
                        categories={categories}
                    />
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default ExternalRecipientsManager;