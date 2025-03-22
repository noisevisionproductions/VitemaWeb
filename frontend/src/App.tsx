import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import MainPanel from "./pages/panel/MainPanel";
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
import ScrollToTop from "./components/common/ScrollToTop";
import CookieConsent from "./components/common/CookieConsent";
import AdminPanel from "./pages/panel/AdminPanel";
import {UserRole} from "./types/user";
import Newsletter from './pages/Newsletter';

function App() {
    return (
        <Router
            future={{
                v7_relativeSplatPath: true,
                v7_startTransition: true
            }}
        >
            <ScrollToTop/>
            <ToastProvider>
                <AuthProvider>
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
                                    <SuggestedCategoriesProvider>
                                        <ProductCategoriesProvider>
                                            <MainPanel/>
                                        </ProductCategoriesProvider>
                                    </SuggestedCategoriesProvider>
                                </ProtectedRoute>
                            }
                        />

                        {/* Admin Dashboard */}
                        <Route
                            path="/admin/*"
                            element={
                                <ProtectedRoute requiredRole={UserRole.ADMIN}>
                                    <AdminPanel/>
                                </ProtectedRoute>
                            }
                        />
                    </Routes>
                    <CookieConsent/>
                </AuthProvider>
            </ToastProvider>
        </Router>
    );
}

export default App;