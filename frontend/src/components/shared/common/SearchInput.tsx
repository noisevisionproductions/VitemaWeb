import React from "react";

interface SearchInputProps {
    id?: string;
    value: string;
    onChange: (value: any) => void;
    placeholder?: string;
}

const SearchInput: React.FC<SearchInputProps> = ({id, value, onChange, placeholder}) => {
    return (
        <input
            id={id}
            type="text"
            placeholder={placeholder}
            className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder:text-gray-500 dark:placeholder:text-gray-400 focus:ring-2 focus:ring-primary dark:focus:ring-primary-light focus:border-primary dark:focus:border-primary-light transition-colors"
            value={value}
            onChange={(e) => onChange(e.target.value)}
        />
    );
};

export default SearchInput;