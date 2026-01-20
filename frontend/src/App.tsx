import {BrowserRouter as Router, Route, Routes, useLocation, useNavigate} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import ProtectedRoute from './components/vitema/auth/ProtectedRoute';
import Unauthorized from "./pages/Unauthorized";
import ErrorPage from "./pages/ErrorPage";
import {SuggestedCategoriesProvider} from "./contexts/SuggestedCategoriesContext";
import {ProductCategoriesProvider} from './hooks/shopping/useProductCategories';
import {ToastProvider} from "./contexts/ToastContext";
import LandingLayout from "./components/landing/layout/LandingLayout";
import Landing from "./pages/Landing";
import About from "./pages/About";
import Login from "./pages/Login";
import Unsubscribe from "./pages/newsletter/Unsubscribe";
import VerifyEmail from "./pages/newsletter/VerifyEmail";
import PrivacyPolicy from "./pages/PrivacyPolicy";
import ScrollToTop from "./components/shared/common/ScrollToTop";
import CookieConsent from "./components/shared/common/CookieConsent";
import {UserRole} from "./types/user";
import Newsletter from './pages/Newsletter';
import {SettingsProvider} from './contexts/SettingsContextType';
import {RouteRestorationProvider} from "./contexts/RouteRestorationContext";
import {ApplicationProvider} from "./contexts/ApplicationContext";
import {lazy, Suspense, useEffect} from "react";
import LoadingSpinner from "./components/shared/common/LoadingSpinner";

const DietitianPanel = lazy(() => import('./pages/panel/DietitianPanel'));
const AdminPanel = lazy(() => import('./pages/panel/AdminPanel'));

const AuthRedirectHandler = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        if (location.hash.includes('access_token') && location.pathname === '/') {
            navigate('/auth/callback', {replace: true});
        }
    }, [location, navigate]);

    return null;
};

function App() {
    const SuspenseFallback = () => (
        <div className="flex items-center justify-center h-screen w-screen">
            <LoadingSpinner size="lg"/>
        </div>
    );

    return (
        <Router
            future={{
                v7_relativeSplatPath: true,
                v7_startTransition: true
            }}
        >
            <ScrollToTop/>
            <AuthRedirectHandler/>

            <ToastProvider>
                <ApplicationProvider>
                    <RouteRestorationProvider>
                        <AuthProvider>
                            <Suspense fallback={<SuspenseFallback/>}>

                                <Routes>

                                    {/* Landing page routes */}
                                    <Route path="/" element={
                                        <LandingLayout>
                                            <Landing/>
                                        </LandingLayout>
                                    }/>
                                    <Route path="/about" element={
                                        <LandingLayout>
                                            <About/>
                                        </LandingLayout>
                                    }/>
                                    <Route path="/privacy-policy" element={
                                        <LandingLayout>
                                            <PrivacyPolicy/>
                                        </LandingLayout>
                                    }/>

                                    {/* Newsletter routes */}
                                    <Route path="/verify-email" element={<VerifyEmail/>}/>
                                    <Route path="/unsubscribe" element={<Unsubscribe/>}/>
                                    <Route path="/newsletter" element={
                                        <LandingLayout>
                                            <Newsletter/>
                                        </LandingLayout>
                                    }/>

                                    {/* Auth routes */}
                                    <Route path="/login" element={<Login/>}/>
                                    <Route path="/unauthorized" element={<Unauthorized/>}/>
                                    <Route path="/error" element={<ErrorPage/>}/>

                                    {/* Main Dashboard */}
                                    <Route
                                        path="/dashboard/*"
                                        element={
                                            <ProtectedRoute requiredRole={UserRole.ADMIN}>
                                                <SettingsProvider>
                                                    <SuggestedCategoriesProvider>
                                                        <ProductCategoriesProvider>
                                                            <DietitianPanel/>
                                                        </ProductCategoriesProvider>
                                                    </SuggestedCategoriesProvider>
                                                </SettingsProvider>
                                            </ProtectedRoute>
                                        }
                                    />

                                    {/* Admin Dashboard */}
                                    <Route
                                        path="/admin/*"
                                        element={
                                            <ProtectedRoute requiredRole={UserRole.OWNER}>
                                                <AdminPanel/>
                                            </ProtectedRoute>
                                        }
                                    />

                                </Routes>
                            </Suspense>
                            <CookieConsent/>
                        </AuthProvider>
                    </RouteRestorationProvider>
                </ApplicationProvider>
            </ToastProvider>
        </Router>

    );
}

export default App;