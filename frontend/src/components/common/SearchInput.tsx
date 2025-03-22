import React from "react";

interface SearchInputProps {
    value: string;
    onChange: (value: any) => void;
    placeholder?: string;
}

const SearchInput: React.FC<SearchInputProps> = ({ value, onChange, placeholder }) => {
    return (
        <input
            type="text"
            placeholder={placeholder}
            className="w-full p-2 border rounded-lg"
            value={value}
            onChange={(e) => onChange(e.target.value)}
        />
    );
};

export default SearchInput;