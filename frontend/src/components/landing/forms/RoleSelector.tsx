import {UseFormRegister} from 'react-hook-form';
import {CheckCircle, User, Building2} from 'lucide-react';
import {useTranslation} from "react-i18next";

interface RoleSelectorProps {
    selectedRole: string | undefined;
    register: UseFormRegister<any>;
    error?: boolean;
}

const RoleSelector = ({selectedRole, register, error}: RoleSelectorProps) => {
    const {t} = useTranslation();

    return (
        <div className="flex flex-col sm:flex-row gap-4">
            <label className="relative flex-1 cursor-pointer group">
                <input
                    type="radio"
                    value="freelancer"
                    className="absolute opacity-0 w-0 h-0"
                    {...register('role', {required: true})}
                />
                <div className={`px-4 py-3 rounded-lg border-2 transition-all duration-200 flex items-center justify-center gap-3
                    ${selectedRole === 'freelancer'
                    ? 'border-primary bg-primary/5 shadow-sm'
                    : 'border-border hover:bg-white/50 hover:border-primary/30 bg-white'}`}
                >
                    <User className={`w-5 h-5 ${selectedRole === 'freelancer' ? 'text-primary' : 'text-text-secondary'}`}/>
                    <span className={`font-medium ${selectedRole === 'freelancer' ? 'text-primary' : 'text-text-primary'}`}>
                        {t('newsletter.roles.freelancer')}
                    </span>
                    {selectedRole === 'freelancer' && (
                        <CheckCircle className="w-5 h-5 text-primary ml-auto"/>
                    )}
                </div>
            </label>

            <label className="relative flex-1 cursor-pointer group">
                <input
                    type="radio"
                    value="studio"
                    className="absolute opacity-0 w-0 h-0"
                    {...register('role', {required: true})}
                />
                <div className={`px-4 py-3 rounded-lg border-2 transition-all duration-200 flex items-center justify-center gap-3
                    ${selectedRole === 'studio'
                    ? 'border-primary bg-primary/5 shadow-sm'
                    : 'border-border hover:bg-white/50 hover:border-primary/30 bg-white'}`}
                >
                    <Building2 className={`w-5 h-5 ${selectedRole === 'studio' ? 'text-primary' : 'text-text-secondary'}`}/>
                    <span className={`font-medium ${selectedRole === 'studio' ? 'text-primary' : 'text-text-primary'}`}>
                        {t('newsletter.roles.studio')}
                    </span>
                    {selectedRole === 'studio' && (
                        <CheckCircle className="w-5 h-5 text-primary ml-auto"/>
                    )}
                </div>
            </label>

            {error && (
                <p className="w-full text-sm text-status-error text-center mt-1">
                    {t('newsletter.validation.roleRequired')}
                </p>
            )}
        </div>
    );
};

export default RoleSelector;