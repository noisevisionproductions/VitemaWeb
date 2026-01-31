export enum Gender {
    MALE = 'MALE',
    FEMALE = 'FEMALE'
}

export enum UserRole {
    USER = 'USER',
    TRAINER = 'TRAINER',
    ADMIN = 'ADMIN',
    OWNER = 'OWNER'
}

export const RoleHierarchy: Record<UserRole, number> = {
    [UserRole.USER]: 1,
    [UserRole.TRAINER]: 2,
    [UserRole.ADMIN]: 3,
    [UserRole.OWNER]: 4
};

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
    trainerId?: string
}
