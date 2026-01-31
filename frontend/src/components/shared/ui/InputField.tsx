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
            <label htmlFor={id} className="block text-sm font-medium text-text-primary mb-2">
                {label}
            </label>
            <div className="relative">
                {icon && (
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
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
                        error ? 'border-status-error' : 'border-border'
                    } focus:ring-2 ${
                        error ? 'focus:ring-status-error' : 'focus:ring-primary'
                    } ${error ? 'focus:border-status-error' : 'focus:border-primary'}`}
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