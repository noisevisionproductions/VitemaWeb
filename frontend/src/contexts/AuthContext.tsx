import {signInWithEmailAndPassword, signOut, User as FirebaseUser} from 'firebase/auth';
import React, {createContext, useContext, useEffect, useState} from "react";
import {auth} from '../config/firebase';
import {RoleHierarchy, User, UserRole} from '../types/user';
import api from "../config/axios";
import axios from 'axios';
import {useRouteRestoration} from "./RouteRestorationContext";

interface AuthContextType {
    currentUser: FirebaseUser | null;
    userData: User | null;
    userClaims: Record<string, any> | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<User>;
    logout: () => Promise<void>;
    refreshUserData: () => Promise<void>;
    isAdmin: () => boolean;
    isOwner: () => boolean;
    hasRole: (requiredRole: UserRole) => boolean;
    isAuthenticated: () => boolean;
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
    const [userClaims, setUserClaims] = useState<Record<string, any> | null>(null);

    const {clearSavedRoute} = useRouteRestoration();

    const validateTokenAndSetUserData = async (user: FirebaseUser) => {
        try {
            const token = await user.getIdToken(true);
            const response = await api.post('/auth/validate-token', {}, {
                headers: {'Authorization': `Bearer ${token}`}
            });

            setUserData(response.data as User);
            const tokenResult = await user.getIdTokenResult(true);
            setUserClaims(tokenResult.claims);
        } catch (error) {
            console.error("Error validating token:", error);
            throw error;
        }
    };

    useEffect(() => {
        setLoading(true);
        const unsubscribe = auth.onAuthStateChanged(async (user) => {
            setCurrentUser(user);

            if (user) {
                try {
                    await validateTokenAndSetUserData(user);
                } catch (error) {
                    console.error("Failed to fetch user data on restoration", error);
                    await signOut(auth);
                    setUserData(null);
                    setCurrentUser(null);
                }
            } else {
                setUserData(null);
                setUserClaims(null);
            }

            setLoading(false);
        });

        return () => {
            unsubscribe();
        };
    }, []);

    const refreshUserData = async () => {
        if (!currentUser) return;

        try {
            await validateTokenAndSetUserData(currentUser);
        } catch (error) {
            console.error("Error refreshing user data:", error);
            await logout();
            throw error;
        }
    };

    const login = async (email: string, password: string): Promise<User> => {
        try {
            const credential = await signInWithEmailAndPassword(auth, email, password);
            const token = await credential.user.getIdToken(true);

            const response = await api.post('/auth/login', {email}, {
                headers: {'Authorization': `Bearer ${token}`}
            });

            const userData = response.data as User;
            setUserData(userData);
            setCurrentUser(credential.user);

            const tokenResult = await credential.user.getIdTokenResult();
            setUserClaims(tokenResult.claims);

            return userData;
        } catch (error) {
            console.error('Authentication error:', error);
            await logout();

            if (axios.isAxiosError(error)) {
                throw new Error(error.response?.data?.message || 'Błąd uwierzytelniania');
            }
            throw error;
        }
    };

    const logout = async () => {
        try {
            clearSavedRoute();

            if (currentUser) {
                await signOut(auth);
                setCurrentUser(null);
                setUserData(null);
                setUserClaims(null);
            }
        } catch (error) {
            console.error('Błąd wylogowania:', error);
            throw error;
        }
    };

    const isOwner = (): boolean => {
        return userClaims?.owner === true || userData?.role === UserRole.OWNER;
    };

    const isAdmin = (): boolean => {
        return isOwner() || userClaims?.admin === true || userData?.role === UserRole.ADMIN;
    };

    const hasRole = (requiredRole: UserRole): boolean => {
        if (!userData) return false;

        const userRole = userData.role as UserRole;
        const userRoleLevel = RoleHierarchy[userRole] || 0;
        const requiredRoleLevel = RoleHierarchy[requiredRole] || 0;

        return userRoleLevel >= requiredRoleLevel;
    };

    const isAuthenticated = (): boolean => {
        return !!currentUser && !!userData;
    };

    const value = {
        currentUser,
        userData,
        userClaims,
        loading,
        login,
        logout,
        refreshUserData,
        isAdmin,
        isOwner,
        hasRole,
        isAuthenticated
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};