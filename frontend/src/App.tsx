import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginForm from './components/auth/LoginForm';
import AdminPanel from "./pages/AdminPanel";
import Unauthorized from "./pages/Unauthorized";
import ErrorPage from "./pages/ErrorPage";
import {SuggestedCategoriesProvider} from "./contexts/SuggestedCategoriesContext";
import {ProductCategoriesProvider} from './hooks/shopping/useProductCategories';
import {ToastProvider} from "./contexts/ToastContext";

function App() {
    return (
        <SuggestedCategoriesProvider>
            <Router
                future={{
                    v7_relativeSplatPath: true,
                    v7_startTransition: true
                }}
            >
                <ToastProvider>
                    <AuthProvider>
                        <ProductCategoriesProvider>
                            <Routes>
                                <Route path="/login" element={<LoginForm/>}/>
                                <Route path="/unauthorized" element={<Unauthorized/>}/>
                                <Route path="/error" element={<ErrorPage/>}/>
                                <Route
                                    path="/dashboard/*"
                                    element={
                                        <ProtectedRoute>
                                            <AdminPanel/>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route path="/" element={<Navigate to="/dashboard" replace/>}/>
                            </Routes>
                        </ProductCategoriesProvider>
                    </AuthProvider>
                </ToastProvider>
            </Router>
        </SuggestedCategoriesProvider>
    );
}

export default App;