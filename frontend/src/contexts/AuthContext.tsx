import {signInWithEmailAndPassword, signOut, User as FirebaseUser} from 'firebase/auth';
import React, {createContext, useContext, useEffect, useState} from "react";
import {auth} from '../config/firebase';
import {User, UserRole} from '../types/nutrilog/user';
import api from "../config/axios";
import axios from 'axios';
import {useRouteRestoration} from "./RouteRestorationContext";
import {ApplicationType} from "../types/application";
import {SupabaseAuthService, SupabaseUser} from "../services/scandallShuffle/SupabaseAuthService";
import {useApplication} from "./ApplicationContext";

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
    supabaseUser: SupabaseUser | null;
    loginWithApplication: (email: string, password: string, applicationType: ApplicationType) => Promise<any>;
    isAuthenticated: () => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);
const SUPABASE_TOKEN_KEY = 'supabase_token';

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
    const [supabaseUser, setSupabaseUser] = useState<SupabaseUser | null>(null);

    const {currentApplication} = useApplication();
    const {clearSavedRoute} = useRouteRestoration();
    const {setApplication} = useApplication();

    useEffect(() => {
        let firebaseUnsubscribe: (() => void) | undefined;
        let supabaseUnsubscribe: { unsubscribe: () => void } | undefined;

        const initializeAuth = async () => {
            try {
                if (currentApplication === null) {
                    setLoading(false);
                    return;
                }

                if (currentApplication === ApplicationType.NUTRILOG) {
                } else if (currentApplication === ApplicationType.SCANDAL_SHUFFLE) {
                    const initialData = await SupabaseAuthService.initializeSession();
                    if (initialData) {
                        setSupabaseUser(initialData.user);
                        setUserData(mapSupabaseUserToUser(initialData.user));
                        localStorage.setItem(SUPABASE_TOKEN_KEY, initialData.session.access_token);
                    }

                    const {data: {subscription}} = SupabaseAuthService.onAuthStateChange((user, session) => {
                        setSupabaseUser(user);
                        if (user && session) {
                            setUserData(mapSupabaseUserToUser(user));
                            localStorage.setItem(SUPABASE_TOKEN_KEY, session.access_token);
                        } else {
                            setUserData(null);
                            localStorage.removeItem(SUPABASE_TOKEN_KEY);
                        }
                    });

                    supabaseUnsubscribe = subscription;
                    setLoading(false);
                }
            } catch (error) {
                console.error("Error initializing auth:", error);
                setLoading(false);
            }
        };

        initializeAuth().catch(console.error);

        return () => {
            if (firebaseUnsubscribe) firebaseUnsubscribe();
            if (supabaseUnsubscribe) supabaseUnsubscribe.unsubscribe();
        };
    }, [currentApplication]);

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
            if (currentApplication === ApplicationType.SCANDAL_SHUFFLE) {
                const supabaseUser = await SupabaseAuthService.login(email, password);
                setSupabaseUser(supabaseUser);

                const userData: User = {
                    id: supabaseUser.id,
                    email: supabaseUser.email,
                    nickname: supabaseUser.displayName || supabaseUser.email.split('@')[0],
                    role: supabaseUser.role as any,
                    gender: null,
                    birthDate: null,
                    storedAge: 0,
                    profileCompleted: false,
                    note: '',
                    createdAt: Date.now()
                };

                setUserData(userData);
                return userData;
            } else {
                const credential = await signInWithEmailAndPassword(auth, email, password);
                const token = await credential.user.getIdToken(true);

                const response = await api.post('/auth/login', {email}, {
                    headers: {'Authorization': `Bearer ${token}`}
                });

                const userData = response.data as User;
                setUserData(userData);

                const tokenResult = await credential.user.getIdTokenResult();
                setUserClaims(tokenResult.claims);

                return userData;
            }
        } catch (error) {
            console.error('Authentication error:', error);
            await logout();

            if (axios.isAxiosError(error)) {
                throw new Error(error.response?.data?.message || 'Błąd uwierzytelniania');
            }
            throw error;
        }
    };

    const loginWithApplication = async (email: string, password: string, applicationType: ApplicationType) => {
        try {
            setApplication(applicationType);

            if (applicationType === ApplicationType.NUTRILOG) {
                return await login(email, password);
            } else {
                const user = await SupabaseAuthService.login(email, password);
                setSupabaseUser(user);

                const userData: User = {
                    id: user.id,
                    email: user.email,
                    nickname: user.displayName || user.email.split('@')[0],
                    role: user.role as any,
                    gender: null,
                    birthDate: null,
                    storedAge: 0,
                    profileCompleted: false,
                    note: '',
                    createdAt: Date.now()
                };

                setUserData(userData);
                return user;
            }
        } catch (error) {
            console.error('Login with application error:', error);
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

            if (supabaseUser) {
                await SupabaseAuthService.logout();
                setSupabaseUser(null);
                setUserData(null);
                localStorage.removeItem(SUPABASE_TOKEN_KEY);
            }
        } catch (error) {
            console.error('Błąd wylogowania:', error);
            throw error;
        }
    };

    const isOwner = (): boolean => {
        if (currentApplication === ApplicationType.NUTRILOG) {
            return userClaims?.owner === true || userData?.role === UserRole.OWNER;
        }
        return supabaseUser?.role === 'owner';
    };

    const isAdmin = (): boolean => {
        if (currentApplication === ApplicationType.NUTRILOG) {
            return isOwner() || userClaims?.admin === true || userData?.role === UserRole.ADMIN;
        }
        return supabaseUser?.role === 'admin' || supabaseUser?.role === 'owner';
    };

    const hasRole = (requiredRole: UserRole | string): boolean => {
        if (currentApplication === ApplicationType.NUTRILOG) {
            if (!userData) return false;

            const roleHierarchy: Record<UserRole, number> = {
                [UserRole.USER]: 1,
                [UserRole.ADMIN]: 2,
                [UserRole.OWNER]: 3
            };

            const userRole = userData.role as UserRole;
            const userRoleLevel = roleHierarchy[userRole] || 0;
            const requiredRoleLevel = roleHierarchy[requiredRole as UserRole] || 0;

            return userRoleLevel >= requiredRoleLevel;
        }

        if (currentApplication === ApplicationType.SCANDAL_SHUFFLE) {
            if (!supabaseUser) return false;

            const userRole = supabaseUser.role;
            if (requiredRole === 'owner') return userRole === 'owner';
            if (requiredRole === 'admin') return userRole === 'admin' || userRole === 'owner';
            return requiredRole === 'user';
        }

        return false;
    };

    const isAuthenticated = (): boolean => {
        if (currentApplication === ApplicationType.NUTRILOG) {
            return !!currentUser && !!userData;
        }
        if (currentApplication === ApplicationType.SCANDAL_SHUFFLE) {
            return !!supabaseUser;
        }
        return false;
    };

    const mapSupabaseUserToUser = (supabaseUser: SupabaseUser): User => {
        return {
            id: supabaseUser.id,
            email: supabaseUser.email,
            nickname: supabaseUser.displayName || supabaseUser.email.split('@')[0],
            role: supabaseUser.role as any,
            gender: null,
            birthDate: null,
            storedAge: 0,
            profileCompleted: false,
            note: '',
            createdAt: Date.now()
        };
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
        supabaseUser,
        loginWithApplication,
        isAuthenticated
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
};