import axios from 'axios';
import {auth} from './firebase';
import {toast} from "../utils/toast";

const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: apiUrl,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
});

api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

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

        if (error.response?.status === 401) {
            toast.error('Sesja wygasła. Zaloguj się ponownie.');
        } else if (error.response?.status === 403) {
            toast.error('Brak uprawnień do wykonania tej operacji.');
        } else {
            toast.error(error.response?.data?.message || `Wystąpił błąd: ${error}`);
        }
        return Promise.reject(error);
    }
);

export default api;