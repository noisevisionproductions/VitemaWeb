import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authService } from '../../../services/AuthService';
import api from '../../../config/axios';
import { RegisterRequest } from '../../../types/auth';

vi.mock('../../../config/axios', () => ({
    default: {
        post: vi.fn(),
    },
}));

describe('AuthService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('registerTrainer', () => {
        it('should successfully register a trainer and return response data', async () => {
            const mockRegisterData: RegisterRequest = {
                email: 'trainer@example.com',
                password: 'password123',
                nickname: 'trainer123',
            };

            const mockResponseData = {
                message: 'Trainer registered successfully',
                userId: 'user123',
            };

            (api.post as any).mockResolvedValue({
                data: mockResponseData,
            });

            const result = await authService.registerTrainer(mockRegisterData);

            expect(api.post).toHaveBeenCalledWith('/auth/register-trainer', mockRegisterData);
            expect(api.post).toHaveBeenCalledTimes(1);
            expect(result).toEqual(mockResponseData);
        });

        it('should handle API errors correctly', async () => {
            const mockRegisterData: RegisterRequest = {
                email: 'trainer@example.com',
                password: 'password123',
                nickname: 'trainer123',
            };

            const mockError = {
                response: {
                    status: 400,
                    data: {
                        message: 'Email already exists',
                    },
                },
            };

            (api.post as any).mockRejectedValue(mockError);

            await expect(authService.registerTrainer(mockRegisterData)).rejects.toEqual(mockError);
            expect(api.post).toHaveBeenCalledWith('/auth/register-trainer', mockRegisterData);
            expect(api.post).toHaveBeenCalledTimes(1);
        });

        it('should handle network errors', async () => {
            const mockRegisterData: RegisterRequest = {
                email: 'trainer@example.com',
                password: 'password123',
                nickname: 'trainer123',
            };

            const mockNetworkError = new Error('Network Error');

            (api.post as any).mockRejectedValue(mockNetworkError);

            await expect(authService.registerTrainer(mockRegisterData)).rejects.toThrow('Network Error');
            expect(api.post).toHaveBeenCalledWith('/auth/register-trainer', mockRegisterData);
            expect(api.post).toHaveBeenCalledTimes(1);
        });

        it('should pass correct data structure to API', async () => {
            const mockRegisterData: RegisterRequest = {
                email: 'test@test.com',
                password: 'securePassword123',
                nickname: 'testTrainer',
            };

            (api.post as any).mockResolvedValue({
                data: { success: true },
            });

            await authService.registerTrainer(mockRegisterData);

            expect(api.post).toHaveBeenCalledWith(
                '/auth/register-trainer',
                expect.objectContaining({
                    email: 'test@test.com',
                    password: 'securePassword123',
                    nickname: 'testTrainer',
                })
            );
        });

        it('should handle empty response data', async () => {
            const mockRegisterData: RegisterRequest = {
                email: 'trainer@example.com',
                password: 'password123',
                nickname: 'trainer123',
            };

            (api.post as any).mockResolvedValue({
                data: {},
            });

            const result = await authService.registerTrainer(mockRegisterData);

            expect(result).toEqual({});
            expect(api.post).toHaveBeenCalledWith('/auth/register-trainer', mockRegisterData);
        });
    });
});
