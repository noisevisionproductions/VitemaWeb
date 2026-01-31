import {useState} from 'react';
import Logo from '../components/shared/ui/landing/Logo';
import {Link} from 'react-router-dom';
import {ArrowLeftIcon} from '@heroicons/react/24/outline';
import LoginForm from "../components/vitema/auth/LoginForm";
// REJESTRACJA TYMCZASOWO WYŁĄCZONA - panel nie jest jeszcze gotowy dla nowych użytkowników
// import RegisterForm from "../components/vitema/auth/RegisterForm";
import {useTranslation} from "react-i18next";
import {Helmet} from 'react-helmet-async';
// REJESTRACJA TYMCZASOWO WYŁĄCZONA - CheckCircleIcon nie jest używany
// import {CheckCircleIcon} from "@heroicons/react/16/solid";

type AuthMode = 'login' | 'register';

const AuthPage = () => {
    const {t} = useTranslation();
    // REJESTRACJA TYMCZASOWO WYŁĄCZONA - zablokowany tryb rejestracji
    const [authMode] = useState<AuthMode>('login'); // zawsze 'login'
    // const [successMessage, setSuccessMessage] = useState<string | null>(null);

    // const toggleMode = (mode: AuthMode) => {
    //     setAuthMode(mode);
    //     setSuccessMessage(null);
    // };

    // const handleRegisterSuccess = () => {
    //     setAuthMode('login');
    //     setSuccessMessage("Konto zostało utworzone pomyślnie. Możesz się teraz zalogować.");
    //     window.scrollTo({top: 0, behavior: 'smooth'});
    // };

    return (
        <div className="min-h-screen flex flex-col md:flex-row relative">
            <Helmet>
                <title>
                    {authMode === 'login'
                        ? t('auth.loginTitle') + ' - Vitema'
                        : t('auth.createAccountTitle') + ' - Vitema'}
                </title>
                <meta name="description"
                      content={t('auth.registerGreeting')}/>
                <link rel="canonical" href="https://vitema.pl/login"/>
            </Helmet>

            <Link
                to="/"
                className="absolute top-6 left-6 flex items-center gap-2 bg-white/90 px-4 py-2 rounded-lg text-primary hover:bg-white transition-colors group z-10 shadow-sm"
            >
                <ArrowLeftIcon className="h-5 w-5 group-hover:-translate-x-1 transition-transform"/>
                <span className="hidden sm:inline">{t('auth.backToHome')}</span>
            </Link>

            {/* Left side */}
            <div
                className="hidden md:flex md:w-1/2 bg-gradient-to-br from-primary to-primary-dark p-12 text-white items-center justify-center">
                <div className="max-w-md">
                    <h1 className="text-4xl font-bold mb-6">
                        {authMode === 'login' ? t('auth.welcomeTitle') : t('auth.registerTitle')}
                    </h1>
                    <p className="text-lg text-white/90 mb-6">
                        {authMode === 'login' ? t('auth.welcomeGreeting') : t('auth.registerGreeting')}
                    </p>
                </div>
            </div>

            {/* Right side */}
            <div className="flex-1 flex flex-col items-center justify-center p-8 bg-gray-50">
                <div className="w-full max-w-md space-y-8">
                    <div className="flex flex-col items-center">
                        <Link to="/" className="mb-6 hover:opacity-80 transition-opacity">
                            <Logo asLink={false}/>
                        </Link>

                        {/* AuthPage tabs */}
                        {/* REJESTRACJA TYMCZASOWO WYŁĄCZONA - panel nie jest jeszcze gotowy dla nowych użytkowników */}
                        {/* <div className="flex p-1 bg-gray-200 rounded-xl mb-6 w-full max-w-xs">
                            <button
                                onClick={() => toggleMode('login')}
                                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200 ${
                                    authMode === 'login' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'
                                }`}
                            >
                                {t('auth.loginTab')}
                            </button>
                            <button
                                onClick={() => toggleMode('register')}
                                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200 ${
                                    authMode === 'register' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'
                                }`}
                            >
                                {t('auth.registerTab')}
                            </button>
                        </div> */}

                        <h2 className="text-2xl font-bold text-gray-900">
                            {authMode === 'login' ? t('auth.loginTitle') : t('auth.createAccountTitle')}
                        </h2>
                    </div>

                    {/* REJESTRACJA TYMCZASOWO WYŁĄCZONA - komunikat o sukcesie rejestracji wyłączony */}
                    {/* {successMessage && authMode === 'login' && (
                        <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-start gap-3">
                            <CheckCircleIcon className="h-5 w-5 text-green-500 mt-0.5"/>
                            <p className="text-sm text-green-700">{successMessage}</p>
                        </div>
                    )} */}

                    <div className="bg-white p-8 rounded-xl shadow-sm">
                        {/* REJESTRACJA TYMCZASOWO WYŁĄCZONA - zawsze pokazuj tylko LoginForm */}
                        <LoginForm/>
                        {/* {authMode === 'login' ? <LoginForm/> : <RegisterForm onSuccess={handleRegisterSuccess}/>} */}
                    </div>

                    {/* REJESTRACJA TYMCZASOWO WYŁĄCZONA - panel nie jest jeszcze gotowy dla nowych użytkowników */}
                    {/* <p className="text-center text-sm text-gray-600">
                        {authMode === 'login' ? (
                            <>
                                {t('auth.noAccess')} {' '}
                                <button onClick={() => setAuthMode('register')}
                                        className="text-primary hover:text-primary-dark font-medium">
                                    {t('auth.signUpLink')}
                                </button>
                            </>
                        ) : (
                            <>
                                {t('auth.alreadyHaveAccount')} {' '}
                                <button onClick={() => setAuthMode('login')}
                                        className="text-primary hover:text-primary-dark font-medium">
                                    {t('auth.signInLink')}
                                </button>
                            </>
                        )}
                    </p> */}
                </div>
            </div>
        </div>
    );
};

export default AuthPage;