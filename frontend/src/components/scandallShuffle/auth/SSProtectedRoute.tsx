import React from "react";
import {Navigate} from "react-router-dom";
import {useAuth} from "../../../contexts/AuthContext";
import {ApplicationType} from "../../../types/application";
import {useApplication} from "../../../contexts/ApplicationContext";

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: string;
}

const SSProtectedRoute: React.FC<ProtectedRouteProps> = ({
                                                           children,
                                                           requiredRole = 'user'
                                                       }) => {
    const {isAuthenticated, supabaseUser} = useAuth();
    const {currentApplication} = useApplication();

    // Only protect routes for Scandal Shuffle
    if (currentApplication !== ApplicationType.SCANDAL_SHUFFLE) {
        return <>{children}</>;
    }

    if (!isAuthenticated() || !supabaseUser) {
        return <Navigate to="/login"/>;
    }

    // Check role if required
    if (requiredRole !== 'user') {
        const userRole = supabaseUser.role;
        if (requiredRole === 'admin' && userRole !== 'admin' && userRole !== 'owner') {
            return <Navigate to="/unauthorized"/>;
        }
        if (requiredRole === 'owner' && userRole !== 'owner') {
            return <Navigate to="/unauthorized"/>;
        }
    }

    return <>{children}</>;
};

export default SSProtectedRoute;