import React, {useState} from "react";
import {EnvelopeIcon, LockClosedIcon, UserIcon} from "@heroicons/react/24/outline";
import InputField from "../../shared/ui/InputField";
import {useTranslation} from "react-i18next";
import {useAuthSubmit} from "../../../hooks/useAuthSubmit";

interface RegisterFormProps {
    onSuccess: () => void;
}

const RegisterForm: React.FC<RegisterFormProps> = ({onSuccess}) => {
    const {t} = useTranslation();
    const {handleSubmit, isLoading, error} = useAuthSubmit('register');

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        nickname: ''
    });

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        await handleSubmit(formData);

        onSuccess();
    };

    return (
        <form onSubmit={onSubmit} className="space-y-6">
            {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                    {error}
                </div>
            )}

            <InputField
                id="nickname"
                label={t('auth.form.nicknameLabel')}
                type="text"
                value={formData.nickname}
                onChange={(e) => setFormData({...formData, nickname: e.target.value})}
                icon={<UserIcon className="h-5 w-5 text-gray-400"/>}
                placeholder={t('auth.form.nicknamePlaceholder')}
                required
            />

            <InputField
                id="email"
                label={t('auth.form.emailLabel')}
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
                icon={<EnvelopeIcon className="h-5 w-5 text-gray-400"/>}
                placeholder={t('auth.form.emailPlaceholder')}
                required
            />

            <InputField
                id="password"
                label={t('auth.form.passwordLabel')}
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
                icon={<LockClosedIcon className="h-5 w-5 text-gray-400"/>}
                placeholder="••••••••"
                required
                minLength={6}
            />

            <button
                type="submit"
                disabled={isLoading}
                className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors disabled:opacity-50"
            >
                {isLoading ? (
                    <span>{t('auth.form.loading')}</span>
                ) : (
                    t('auth.form.registerButton') || "Zarejestruj konto"
                )}
            </button>
        </form>
    );
};

export default RegisterForm;