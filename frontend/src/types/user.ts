export enum Gender {
    MALE = 'MALE',
    FEMALE = 'FEMALE'
}

export enum UserRole {
    USER = 'USER',
    ADMIN = 'ADMIN'
}

export interface User {
    id: string;
    createdAt: number;
    email: string;
    nickname: string;
    gender: Gender | null;
    birthDate: number | null;
    storedAge: number;
    profileCompleted: boolean;
    role: UserRole;
    note?: string;
}
