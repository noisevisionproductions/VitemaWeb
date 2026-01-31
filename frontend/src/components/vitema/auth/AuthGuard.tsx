import React from 'react';
import {Navigate} from 'react-router-dom';
import {useAuth} from '../../../contexts/AuthContext';
import {RoleHierarchy, UserRole} from '../../../types/user';

interface AuthGuardProps {
    children: React.ReactNode;
    requiredRole?: UserRole;
}

const AuthGuard: React.FC<AuthGuardProps> = ({
                                                 children,
                                                 requiredRole = UserRole.TRAINER
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

    const userRoleLevel = RoleHierarchy[userData.role];
    const requiredRoleLevel = RoleHierarchy[requiredRole];

    if (userRoleLevel < requiredRoleLevel) {
        return <Navigate to="/unauthorized"/>;
    }

    return <>{children}</>;
};

export default AuthGuard;