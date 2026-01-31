import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../contexts/AuthContext';
import {authService} from "../services/AuthService";
import {useTranslation} from 'react-i18next';

type AuthAction = 'login' | 'register';

export const useAuthSubmit = (action: AuthAction) => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const {login} = useAuth();

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (data: any) => {
        setIsLoading(true);
        setError(null);

        try {
            if (action === 'login') {
                await login(data.email, data.password);
                navigate('/dashboard');
            } else {
                await authService.registerTrainer({
                    email: data.email,
                    password: data.password,
                    nickname: data.nickname
                });

                return true;
            }
        } catch (err: any) {
            console.error(`${action} failed:`, err);
            const errorMessage = err.response?.data?.message || err.message;
            setError(errorMessage || t('auth.form.error'));
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    return {handleSubmit, isLoading, error};
};