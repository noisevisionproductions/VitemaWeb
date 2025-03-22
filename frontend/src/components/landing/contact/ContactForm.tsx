import {useForm} from "react-hook-form";
import {useState} from "react";
import {ContactFormData} from "../../../types/contact";
import {ContactService} from "../../../services/contact/ContactService";
import {toast} from "../../../utils/toast";

const ContactForm = () => {
    const {register, handleSubmit, formState: {errors, isSubmitting}, reset} = useForm<ContactFormData>();
    const [submitSuccess, setSubmitSuccess] = useState(false);

    const onSubmit = async (data: ContactFormData) => {
        try {
            await ContactService.sendContactForm(data);
            setSubmitSuccess(true);
            toast.success("Twoja wiadomość została wysłana. Skontaktujemy się wkrótce.");
            reset();
        } catch (error) {
            console.error("Błąd wysyłania formularza:", error);
            toast.error("Wystąpił błąd podczas wysyłania wiadomości. Spróbuj ponownie później.");
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="p-8 bg-background rounded-xl border border-border">
            <h3 className="text-xl font-semibold text-text-primary mb-6">
                Wyślij wiadomość
            </h3>

            {submitSuccess && (
                <div className="mb-6 p-4 bg-status-success/10 text-status-success rounded-lg">
                    Twoja wiadomość została wysłana. Skontaktujemy się wkrótce.
                </div>
            )}

            <div className="space-y-6">
                <div>
                    <label htmlFor="name" className="block text-sm font-medium text-text-primary mb-2">
                        Imię lub nazwa firmy
                    </label>
                    <input
                        type="text"
                        id="name"
                        className="w-full px-4 py-3 rounded-lg border border-border focus:ring-2 focus:ring-primary focus:border-primary"
                        {...register('name', {required: 'To pole jest wymagane'})}
                    />
                    {errors.name && (
                        <p className="mt-1 text-sm text-status-error">
                            {errors.name.message}
                        </p>
                    )}
                </div>

                <div>
                    <label htmlFor="email" className="block text-sm font-medium text-text-primary mb-2">
                        Email
                    </label>
                    <input
                        type="email"
                        id="email"
                        className="w-full px-4 py-3 rounded-lg border border-border focus:ring-2 focus:ring-primary focus:border-primary"
                        {...register('email', {
                            required: 'To pole jest wymagane',
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: 'Nieprawidłowy adres email'
                            }
                        })}
                    />
                    {errors.email && (
                        <p className="mt-1  text-sm text-status-error">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <label htmlFor="phone" className="block text-sm font-medium text-text-primary mb-2">
                        Telefon (opcjonalnie)
                    </label>
                    <input
                        type="tel"
                        id="phone"
                        className="w-full px-4 py-3 rounded-lg border border-border focus:ring-2 focus:ring-primary focus:border-primary"
                        {...register('phone')}
                    />
                </div>

                <div>
                    <label htmlFor="message" className="block text-sm font-medium text-text-primary mb-2">
                        Wiadomość
                    </label>
                    <textarea
                        id="message"
                        rows={6}
                        className="w-full px-4 py-3 rounded-lg border border-border focus:ring-2 focus:ring-primary focus:border-primary"
                        {...register('message', {required: 'To pole jest wymagane'})}
                    />
                    {errors.message && (
                        <p className="mt-1 text-sm text-status-error">
                            {errors.message.message}
                        </p>
                    )}
                </div>

                {/*   <div className="mt-4 mb-4">
                    <ReCAPTCHA
                        sitekey="YOUR_RECAPTCHA_SITE_KEY"
                        onChange={(value: any) => setValue('captcha', value || '')}
                    />
                    {errors.captcha && (
                        <p className="mt-1 text-sm text-status-error">
                            {errors.captcha.message}
                        </p>
                    )}
                </div>*/}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-primary text-white px-6 py-3 rounded-lg hover:bg-primary-dark transition-colors duration-200 disabled:opacity-50"
                >
                    {isSubmitting ? 'Wysyłanie...' : 'Wyślij wiadomość'}
                </button>
            </div>
        </form>
    );
};

export default ContactForm;