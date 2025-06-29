import forms from '@tailwindcss/forms';
import aspectRatio from '@tailwindcss/aspect-ratio';

/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: 'var(--color-primary)',
                    light: 'var(--color-primary-light)',
                    dark: 'var(--color-primary-dark)',
                },
                secondary: {
                    DEFAULT: 'var(--color-secondary)',
                    light: 'var(--color-secondary-light)',
                    dark: 'var(--color-secondary-dark)',
                },
                nutrition: {
                    protein: 'var(--color-protein)',
                    carbs: 'var(--color-carbs)',
                    fats: 'var(--color-fats)',
                    calories: 'var(--color-calories)',
                },
                status: {
                    success: 'var(--color-success)',
                    warning: 'var(--color-warning)',
                    error: 'var(--color-error)',
                    info: 'var(--color-info)',
                },
                surface: {
                    DEFAULT: 'var(--color-surface)',
                    dark: 'var(--color-surface-dark)',
                },
                text: {
                    primary: {
                        DEFAULT: 'var(--color-text-primary)',
                        dark: 'var(--color-text-primary-dark)',
                    },
                    secondary: {
                        DEFAULT: 'var(--color-text-secondary)',
                        dark: 'var(--color-text-secondary-dark)',
                    },
                    disabled: {
                        DEFAULT: 'var(--color-text-disabled)',
                        dark: 'var(--color-text-disabled-dark)',
                    },
                },
            },
            // Dodajemy focus ring i border colors dla nutrition
            ringColor: {
                'nutrition-protein': 'var(--color-nutrition-protein-focus)',
                'nutrition-carbs': 'var(--color-nutrition-carbs-focus)',
                'nutrition-fats': 'var(--color-nutrition-fats-focus)',
                'nutrition-calories': 'var(--color-nutrition-calories-focus)',
            },
            borderColor: {
                'nutrition-protein': 'var(--color-protein)',
                'nutrition-carbs': 'var(--color-carbs)',
                'nutrition-fats': 'var(--color-fats)',
                'nutrition-calories': 'var(--color-calories)',
            },
            fontFamily: {
                primary: 'var(--font-primary)',
                secondary: 'var(--font-secondary)',
            },
            boxShadow: {
                'sm': 'var(--shadow-sm)',
                'md': 'var(--shadow-md)',
                'lg': 'var(--shadow-lg)',
            },
            borderRadius: {
                'sm': 'var(--radius-sm)',
                'md': 'var(--radius-md)',
                'lg': 'var(--radius-lg)',
                'xl': 'var(--radius-xl)',
            },
            keyframes: {
                "accordion-down": {
                    from: {height: 0},
                    to: {height: "var(--radix-accordion-content-height)"},
                },
                "accordion-up": {
                    from: {height: "var(--radix-accordion-content-height)"},
                    to: {height: 0},
                },
            },
            animation: {
                "accordion-down": "accordion-down 0.2s ease-out",
                "accordion-up": "accordion-up 0.2s ease-out",
            },
        },
    },
    plugins: [
        forms,
        aspectRatio,
        require("tailwindcss-animate")
    ]
}