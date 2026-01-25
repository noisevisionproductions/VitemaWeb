import {UseFormRegister} from 'react-hook-form';
import {CheckCircle} from 'lucide-react';
import {useTranslation} from "react-i18next";

interface RoleSelectorProps {
    selectedRole: 'dietetyk' | 'firma' | undefined;
    register: UseFormRegister<any>;
    error?: boolean;
}

const RoleSelector = ({selectedRole, register, error}: RoleSelectorProps) => {
    const {t} = useTranslation();

    return (
        <div className="flex flex-wrap gap-4">
            <label className="relative flex items-center gap-2 cursor-pointer group">
                <input
                    type="radio"
                    value="dietetyk"
                    className="absolute opacity-0 w-0 h-0"
                    {...register('role', {required: true})}
                />
                <div className={`px-4 py-2 rounded-lg border-2 transition-all duration-200 flex items-center gap-2
                    ${selectedRole === 'dietetyk'
                    ? 'border-primary bg-white shadow-sm'
                    : 'border-border hover:bg-white/50 hover:border-primary/30'}`}
                >
                    <span className="text-text-primary font-medium">
                        👩‍⚕️ {t('newsletter.roles.dietitian')}
                    </span>
                    {selectedRole === 'dietetyk' && (
                        <CheckCircle className="w-5 h-5 text-primary"/>
                    )}
                </div>
            </label>
            <label className="relative flex items-center gap-2 cursor-pointer">
                <input
                    type="radio"
                    value="firma"
                    className="absolute opacity-0 w-0 h-0"
                    {...register('role', {required: true})}
                />
                <div className={`px-4 py-2 rounded-lg border-2 transition-all duration-200 flex items-center gap-2
                    ${selectedRole === 'firma'
                    ? 'border-primary bg-white shadow-sm'
                    : 'border-border hover:bg-white/50 hover:border-primary/30'}`}
                >
                    <span className="text-text-primary font-medium">
                        💪 {t('newsletter.roles.company')}
                    </span>
                    {selectedRole === 'firma' && (
                        <CheckCircle className="w-5 h-5 text-primary"/>
                    )}
                </div>
            </label>
            {error && (
                <p className="w-full mt-2 text-sm text-status-error">
                    {t('newsletter.validation.roleRequired')}
                </p>
            )}
        </div>
    );
};

export default RoleSelector;