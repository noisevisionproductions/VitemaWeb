import { create } from 'zustand';
import { User } from '../types/user';
import {ParsedDietData} from "../types";

interface DietPreviewStore {
    previewData: ParsedDietData | null;
    selectedUser: User | null;
    file: File | null;
    setPreviewData: (data: ParsedDietData | null) => void;
    setSelectedUser: (user: User | null) => void;
    setFile: (file: File | null) => void;
    clearPreview: () => void;
}
create<DietPreviewStore>((set) => ({
    previewData: null,
    selectedUser: null,
    file: null,
    setPreviewData: (data) => set({ previewData: data }),
    setSelectedUser: (user) => set({ selectedUser: user }),
    setFile: (file) => set({ file: file }),
    clearPreview: () => set({
        previewData: null,
        file: null
    }),
}));
