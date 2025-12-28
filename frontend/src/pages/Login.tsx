import Logo from '../components/shared/ui/landing/Logo';
import {Link} from 'react-router-dom';
import {ArrowLeftIcon} from '@heroicons/react/24/outline';
import LoginForm from "../components/nutrilog/auth/LoginForm";
import {useTranslation} from "react-i18next";

const Login = () => {
    const {t} = useTranslation();

    return (
        <div className="min-h-screen flex flex-col md:flex-row relative">
            <Link
                to="/"
                className="absolute top-6 left-6 flex items-center gap-2 bg-white/90 px-4 py-2 rounded-lg text-primary hover:bg-white transition-colors group z-10"
            >
                <ArrowLeftIcon className="h-5 w-5 group-hover:-translate-x-1 transition-transform"/>
                <span>{t('auth.backToHome')}</span>
            </Link>

            <div
                className="hidden md:flex md:w-1/2 bg-gradient-to-br from-primary to-primary-dark p-12 text-white items-center justify-center">
                <div className="max-w-md">
                    <h1 className="text-4xl font-bold mb-6">{t('auth.welcomeTitle')}</h1>
                    <p className="text-lg text-white/90 mb-6">
                        {t('auth.welcomeGreeting')}
                    </p>
                </div>
            </div>

            <div className="flex-1 flex flex-col items-center justify-center p-8 bg-gray-50">
                <div className="w-full max-w-md space-y-8">
                    <div className="flex flex-col items-center">
                        <Link to="/" className="mb-6 hover:opacity-80 transition-opacity">
                            <Logo asLink={false}/>
                        </Link>
                        <h2 className="mt-2 text-2xl font-bold text-gray-900">
                            {t('auth.loginTitle')}
                        </h2>
                    </div>

                    <div className="bg-white p-8 rounded-xl shadow-sm">
                        <LoginForm/>
                    </div>

                    <p className="text-center text-sm text-gray-600">
                        {t('auth.noAccess')} {' '}
                        <a href="mailto:kontakt@szytadieta.pl" className="text-primary hover:text-primary-dark">
                            {t('auth.contactAdmin')}
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;