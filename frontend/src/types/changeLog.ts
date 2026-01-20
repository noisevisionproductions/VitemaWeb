import {Timestamp} from "firebase/firestore";

export interface ChangelogEntry {
    id: string;
    title: string;
    description: string;
    createdAt: Timestamp;
    author: string;
    type: 'feature' | 'fix' | 'improvement';
}