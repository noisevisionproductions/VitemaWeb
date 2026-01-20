import NewsletterForm from "../components/landing/forms/NewsletterForm";
import Container from "../components/shared/ui/landing/Container";

const Newsletter = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-primary py-12">
            <Container>
                <div className="max-w-3xl mx-auto text-center">
                    <h1 className="text-3xl sm:text-4xl font-bold text-white mb-6">
                        Dołącz do Vitema
                    </h1>
                    <p className="text-lg text-white/90 mb-8">
                        Zapisz się do newslettera i zyskaj 6 miesięcy dostępu za darmo
                    </p>

                    <div className="max-w-xl mx-auto">
                        <NewsletterForm
                            className="bg-white p-4 rounded-xl shadow-lg"
                            buttonClassName="bg-secondary hover:bg-secondary-dark"
                        />
                        <p className="mt-4 text-sm text-white/80">
                            Dołączając do listy, zgadzasz się na otrzymywanie informacji o produkcie.
                            Możesz zrezygnować w każdej chwili.
                        </p>
                    </div>
                </div>
            </Container>
        </div>
    );
};

export default Newsletter;