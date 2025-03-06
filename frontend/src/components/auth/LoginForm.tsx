import React, { useState } from "react";
import { useAuth } from "../../contexts/AuthContext";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Mail, Lock } from "lucide-react";

interface LoginFormState {
    email: string;
    password: string;
    loading: boolean;
    error: string | null;
}

const LoginForm = () => {
    const [formState, setFormState] = useState<LoginFormState>({
        email: '',
        password: '',
        loading: false,
        error: null
    });

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormState(prev => ({
            ...prev,
            [name]: value,
            error: null
        }));
    };

    const validateForm = (): boolean => {
        if (!formState.email || !formState.password) {
            setFormState(prev => ({
                ...prev,
                error: 'Wszystkie pola są wymagane'
            }));
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formState.email)) {
            setFormState(prev => ({
                ...prev,
                error: 'Nieprawidłowy format adresu email'
            }));
            return false;
        }

        if (formState.password.length < 6) {
            setFormState(prev => ({
                ...prev,
                error: 'Hasło musi mieć co najmniej 6 znaków'
            }));
            return false;
        }

        return true;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setFormState(prev => ({ ...prev, loading: true, error: null }));

        try {
            await login(formState.email, formState.password);
            navigate('/dashboard');
            toast.success('Zalogowano pomyślnie');
        } catch (error) {
            let errorMessage = 'Błąd logowania. Sprawdź dane i spróbuj ponownie.';

            if (error instanceof Error) {
                if (error.message === 'Brak uprawnień administratora') {
                    errorMessage = 'Brak uprawnień do panelu administratora';
                } else if (error.message.includes('auth/wrong-password') ||
                    error.message.includes('auth/user-not-found')) {
                    errorMessage = 'Nieprawidłowy email lub hasło';
                }
            }

            setFormState(prev => ({
                ...prev,
                error: errorMessage,
                password: ''
            }));
            toast.error(errorMessage);
        } finally {
            setFormState(prev => ({ ...prev, loading: false }));
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
                <div>
                    <h2 className="text-center text-3xl font-extrabold text-gray-900">
                        Panel Admina
                    </h2>
                </div>
                {formState.error && (
                    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                        {formState.error}
                    </div>
                )}
                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    <div className="rounded-md shadow-sm -space-y-px">
                        <div className="relative">
                            <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5 pointer-events-none z-10" />
                            <input
                                type="email"
                                name="email"
                                required
                                value={formState.email}
                                onChange={handleInputChange}
                                className="pl-10 appearance-none rounded-t-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                                placeholder="Adres email"
                            />
                        </div>
                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5 pointer-events-none z-10" />
                            <input
                                type="password"
                                name="password"
                                required
                                value={formState.password}
                                onChange={handleInputChange}
                                className="pl-10 appearance-none rounded-b-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                                placeholder="Hasło"
                            />
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={formState.loading}
                            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {formState.loading ? 'Logowanie...' : 'Zaloguj się'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LoginForm;