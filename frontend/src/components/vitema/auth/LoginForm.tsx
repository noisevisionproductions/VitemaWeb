import React, {useState} from "react";
import {useAuth} from "../../../contexts/AuthContext";
import {useNavigate} from "react-router-dom";
import {EnvelopeIcon, LockClosedIcon} from "@heroicons/react/24/outline";
import InputField from "../../shared/ui/InputField";
import {useTranslation} from "react-i18next";

const LoginForm = () => {
    const {t} = useTranslation();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const {login} = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);

        try {
            await login(email, password);
            navigate('/dashboard');
        } catch (error) {
            console.error('AuthPage failed:', error);
            setError(t('auth.form.error'));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                    {error}
                </div>
            )}

            <InputField
                id="email"
                label={t('auth.form.emailLabel')}
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                icon={<EnvelopeIcon className="h-5 w-5 text-gray-400"/>}
                placeholder={t('auth.form.emailPlaceholder')}
                required
            />

            <InputField
                id="password"
                label={t('auth.form.passwordLabel')}
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                icon={<LockClosedIcon className="h-5 w-5 text-gray-400"/>}
                placeholder="••••••••"
                required
            />

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors disabled:opacity-50"
            >
                {isSubmitting ? (
                    <span className="flex items-center">
                       <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                           <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                   strokeWidth="4"/>
                           <path className="opacity-75" fill="currentColor"
                                 d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                       </svg>
                        {t('auth.form.loading')}
                   </span>
                ) : (
                    t('auth.form.submit')
                )}
            </button>
        </form>
    );
};

export default LoginForm;