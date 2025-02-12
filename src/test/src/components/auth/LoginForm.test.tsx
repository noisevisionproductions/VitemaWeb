import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent} from "@testing-library/react";
import LoginForm from "../../../../components/auth/LoginForm";
import { useAuth} from "../../../../contexts/AuthContext";

vi.mock('../../../../contexts/AuthContext', () => ({
    useAuth: vi.fn(() => ({
        login: vi.fn(),
        loading: false
    }))
}));

describe('LoginForm', () => {
    it('renders login form correctly', () => {
        render(<LoginForm />);

        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/hasło/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /zaloguj/i })).toBeInTheDocument();
    });

    it('handles form submission correctly', async () => {
        const mockLogin = vi.fn();
        (useAuth as any).mockImplementation(() => ({
            login: mockLogin,
            loading: false
        }));

        render(<LoginForm />);

        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/hasło/i);
        const submitButton = screen.getByRole('button', { name: /zaloguj/i });

        fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
        fireEvent.change(passwordInput, { target: { value: 'password123' } });
        fireEvent.click(submitButton);

        expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
    });
});