import React from "react";
import AuthGuard from "./AuthGuard";
import { UserRole } from "../../../types/user";

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: UserRole;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
                                                           children,
                                                           requiredRole = UserRole.ADMIN
                                                       }) => {
    return (
        <AuthGuard requiredRole={requiredRole}>
            {children}
        </AuthGuard>
    );
};

export default ProtectedRoute;