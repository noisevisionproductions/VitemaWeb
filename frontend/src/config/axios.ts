import axios from 'axios';
import {auth} from './firebase';
import {toast} from "../utils/toast";
import {ApplicationType} from "../types/application";

const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const SUPABASE_TOKEN_KEY = 'supabase_token';

let lastRequestTime = 0;

const api = axios.create({
    baseURL: apiUrl,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
});

api.interceptors.request.use(
    async (config) => {
        console.log(`[Interceptor] Starting request to: ${config.url}`);

        try {
            const now = Date.now();
            const timeSinceLastRequest = now - lastRequestTime;
            if (timeSinceLastRequest < 100) {
                await new Promise(resolve =>
                    setTimeout(resolve, 100 - timeSinceLastRequest)
                );
            }
            lastRequestTime = Date.now();

            const currentApplication = localStorage.getItem('selectedApplication') as ApplicationType | null;

            if (currentApplication === ApplicationType.NUTRILOG) {
                const user = auth.currentUser;
                if (user) {
                    const token = await user.getIdToken();
                    config.headers.Authorization = `Bearer ${token}`;
                    console.log('[Interceptor] Attached Firebase token.');
                }
            } else if (currentApplication === ApplicationType.SCANDAL_SHUFFLE) {
                const token = localStorage.getItem(SUPABASE_TOKEN_KEY);
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                    console.log('[Interceptor] Attached Supabase token from localStorage.');
                } else {
                    console.warn('[Interceptor] Supabase token not found in localStorage.');
                }
            }

            console.log(`[Interceptor] Finishing request to: ${config.url}`);
            return config;
        } catch (error) {
            console.error('Request interceptor error:', error);
            return config;
        }
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', {
            status: error.response?.status,
            data: error.response?.data,
            config: {
                method: error.config?.method,
                url: error.config?.url,
                data: error.config?.data
            }
        });

        if (error.response?.status === 429) {
            toast.error('Za dużo żądań. Spróbuj ponownie za chwilę.');
            return new Promise((resolve) => {
                setTimeout(() => {
                    resolve(api.request(error.config));
                }, 2000);
            });
        } else if (error.response?.status === 401) {
            toast.error('Sesja wygasła. Zaloguj się ponownie.');
        } else if (error.response?.status === 403) {
            toast.error('Brak uprawnień do wykonania tej operacji.');
        } else if (error.response?.status >= 500) {
            toast.error('Błąd serwera. Spróbuj ponownie później.');
        } else {
            toast.error(error.response?.data?.message || `Wystąpił błąd: ${error.message}`);
        }

        return Promise.reject(error);
    }
);

export default api;