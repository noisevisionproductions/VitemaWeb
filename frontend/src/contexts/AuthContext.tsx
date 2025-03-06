import {onAuthStateChanged, signInWithEmailAndPassword, signOut, User as FirebaseUser} from 'firebase/auth';
import {doc, getDoc} from 'firebase/firestore';
import React, {createContext, useContext, useEffect, useState} from "react";
import {auth, db} from '../config/firebase';
import {User} from '../types/user';
import api from "../config/axios";
import axios from 'axios';

interface AuthContextType {
    currentUser: FirebaseUser | null;
    userData: User | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<User>;
    logout: () => Promise<void>;
}


const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const [currentUser, setCurrentUser] = useState<FirebaseUser | null>(null);
    const [userData, setUserData] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        return onAuthStateChanged(auth, async (user) => {
            setCurrentUser(user);
            if (user) {
                const userDoc = await getDoc(doc(db, 'users', user.uid));
                if (userDoc.exists()) {
                    setUserData(userDoc.data() as User);
                }
            } else {
                setUserData(null);
            }
            setLoading(false);
        });
    }, []);

/*
    const refreshAuth = async () => {
        const user = auth.currentUser;
        if (!user) {
            await logout();
            return Promise.reject(new Error('Brak zalogowanego użytkownika'));
        }

        try {
            const token = await user.getIdToken(true);
            const userDoc = await getDoc(doc(db, 'users', user.uid));

            if (!userDoc.exists()) {
                await logout();
                return Promise.reject(new Error('Nie znaleziono danych użytkownika'));
            }

            const userData = userDoc.data() as User;
            setUserData(userData);
            return token;
        } catch (error) {
            console.error('Błąd odświeżania autoryzacji:', error);
            await logout();
            return Promise.reject(error);
        }
    };
*/

    const login = async (email: string, password: string) => {
        try {
            const credential = await signInWithEmailAndPassword(auth, email, password);
            const token = await credential.user.getIdToken(true);

            const response = await api.post('/auth/login',
                { email },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                }
            );

            const userData = response.data as User;
            if (userData.role !== 'ADMIN') {
                await logout();
                return Promise.reject(new Error('Brak uprawnień administratora'));
            }

            setUserData(userData);
            return userData;
        } catch (error) {
            console.error('Błąd uwierzytelniania:', error);
            await logout();

            if (axios.isAxiosError(error)) {
                return Promise.reject(new Error(error.response?.data?.message || 'Błąd uwierzytelniania'));
            }
            return Promise.reject(error);
        }
    };

    const logout = async () => {
        try {
            await signOut(auth);
            setCurrentUser(null);
            setUserData(null);
        } catch (error) {
            console.error('Logout error:', error);
            throw error;
        }
    };

    const value = {
        currentUser,
        userData,
        loading,
        login,
        logout
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
};