import React from 'react';
import {Navigate} from 'react-router-dom';
import {useAuth} from '../../../contexts/AuthContext';
import {UserRole} from '../../../types/user';

interface AuthGuardProps {
    children: React.ReactNode;
    requiredRole?: UserRole;
}

const roleHierarchy: Record<UserRole, number> = {
    [UserRole.USER]: 1,
    [UserRole.ADMIN]: 2,
    [UserRole.OWNER]: 3
};

const AuthGuard: React.FC<AuthGuardProps> = ({
                                                 children,
                                                 requiredRole = UserRole.ADMIN
                                             }) => {
    const {currentUser, userData, loading} = useAuth();

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"/>
            </div>
        );
    }

    if (!currentUser || !userData) {
        return <Navigate to="/"/>;
    }

    const userRoleLevel = roleHierarchy[userData.role];
    const requiredRoleLevel = roleHierarchy[requiredRole];

    if (userRoleLevel < requiredRoleLevel) {
        return <Navigate to="/unauthorized"/>;
    }

    return <>{children}</>;
};

export default AuthGuard;