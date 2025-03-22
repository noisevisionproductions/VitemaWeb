import {useEffect} from 'react';
import {useForm} from 'react-hook-form';
import RoleSelector from './RoleSelector';
import {NewsletterFormData} from "../../../types/newsletter";
import {useNewsletterSubscription} from "../../../hooks/useNewsletterSubscription";

interface NewsletterFormProps {
    className?: string;
    buttonClassName?: string;
}

const NewsletterForm = ({className = '', buttonClassName = ''}: NewsletterFormProps) => {
    const {isSubmitting, isSuccess, subscribeToNewsletter, resetSuccess} = useNewsletterSubscription();

    const {register, handleSubmit, formState: {errors}, watch, reset} = useForm<NewsletterFormData>({
        defaultValues: {
            role: 'dietetyk'
        }
    });

    const selectedRole = watch('role');

    useEffect(() => {
        if (isSuccess) {
            reset();
            const timeout = setTimeout(() => {
                resetSuccess();
            }, 5000);

            return () => clearTimeout(timeout);
        }
    }, [isSuccess]);

    const onSubmit = async (data: NewsletterFormData) => {
        try {
            await subscribeToNewsletter(data);
        } catch (error) {
            console.error('Błąd zapisu do newslettera:', error);
        }
    };

    return (
        <div className="isolate">
            {isSuccess ? (
                <div className="p-4 bg-green-50 text-green-800 rounded-lg">
                    <h3 className="font-medium text-lg">Dziękujemy za zapisanie się!</h3>
                    <p>Sprawdź swoją skrzynkę email, aby potwierdzić zapis do newslettera.</p>
                </div>
            ) : (
                <form onSubmit={handleSubmit(onSubmit)} className={className}>
                    <div className="flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <input
                                type="email"
                                placeholder="Twój adres email"
                                className="w-full px-4 py-3 rounded-lg border border-border focus:ring-2 focus:ring-primary focus:border-primary bg-white"
                                {...register('email', {
                                    required: 'Email jest wymagany',
                                    pattern: {
                                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                        message: 'Nieprawidłowy adres email'
                                    }
                                })}
                            />
                            {errors.email && (
                                <p className="mt-1 text-sm text-status-error">{errors.email.message}</p>
                            )}
                        </div>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className={`bg-primary text-white px-8 py-3 rounded-lg hover:bg-primary-dark transition-colors duration-200 disabled:opacity-50 ${buttonClassName}`}
                        >
                            {isSubmitting ? 'Zapisywanie...' : 'Zapisz się do listy oczekujących'}
                        </button>
                    </div>

                    <div className="mt-4">
                        <RoleSelector
                            selectedRole={selectedRole}
                            register={register}
                            error={!!errors.role}
                        />
                    </div>
                </form>
            )}
        </div>
    );
};

export default NewsletterForm;