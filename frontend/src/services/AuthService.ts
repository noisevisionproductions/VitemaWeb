import api from "../config/axios";
import {RegisterRequest} from "../types/auth";

export const authService = {
    registerTrainer: async (data: RegisterRequest) => {
        const response = await api.post('/auth/register-trainer', data);
        return response.data;
    }
};