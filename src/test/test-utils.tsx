import React from 'react';
import { render as rtlRender } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';

function render(ui: React.ReactElement, { route = '/' } = {}) {
    window.history.pushState({}, 'Test page', route);

    return rtlRender(ui, {
        wrapper: ({ children }) => (
            <BrowserRouter>
                <AuthProvider>
                    {children}
                </AuthProvider>
            </BrowserRouter>
        ),
    });
}

export * from '@testing-library/react';
export { render };