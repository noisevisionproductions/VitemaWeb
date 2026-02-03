import {ReactNode, InputHTMLAttributes} from 'react';

interface InputFieldProps extends InputHTMLAttributes<HTMLInputElement> {
    id: string;
    label: string;
    icon?: ReactNode;
    error?: string;
}

const InputField = ({
                        id,
                        label,
                        icon,
                        type = 'text',
                        placeholder,
                        required = false,
                        value,
                        onChange,
                        error,
                        ...rest
                    }: InputFieldProps) => {
    return (
        <div>
            <label htmlFor={id} className="block text-sm font-medium text-text-primary dark:text-gray-200 mb-2">
                {label}
            </label>
            <div className="relative">
                {icon && (
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500 dark:text-gray-400">
                        {icon}
                    </div>
                )}
                <input
                    id={id}
                    type={type}
                    value={value}
                    onChange={onChange}
                    required={required}
                    {...rest}
                    className={`${icon ? 'pl-10' : 'pl-4'} w-full px-4 py-3 rounded-lg border ${
                        error ? 'border-status-error' : 'border-gray-300 dark:border-gray-600'
                    } bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder:text-gray-500 dark:placeholder:text-gray-400 focus:ring-2 ${
                        error ? 'focus:ring-status-error' : 'focus:ring-primary dark:focus:ring-primary-light'
                    } ${error ? 'focus:border-status-error' : 'focus:border-primary dark:focus:border-primary-light'} transition-colors`}
                    placeholder={placeholder}
                />
            </div>
            {error && (
                <p className="mt-1 text-sm text-status-error">
                    {error}
                </p>
            )}
        </div>
    );
};

export default InputField;