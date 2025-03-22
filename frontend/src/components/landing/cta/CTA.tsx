import Container from "../../ui/landing/Container";
import {motion} from "framer-motion";
import NewsletterForm from "../forms/NewsletterForm";
import CountdownTimer from "../../common/CountdownTimer";
import {useMemo} from "react";

const CTA = () => {

    const targetDate = useMemo(() => {
        const today = new Date();
        const futureDate = new Date(today);
        futureDate.setDate(today.getDate() + 40);
        return futureDate.toISOString();
    }, []);

    return (
        <section id="cta-section" className="relative py-24 bg-primary">
            <Container>
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    className="max-w-3xl mx-auto text-center"
                >
                    <h2 className="text-3xl sm:text-4xl font-bold text-white mb-6">
                        Rozpocznij transformację swojej praktyki dietetycznej
                    </h2>
                    <p className="text-lg text-white/90 mb-8">
                        Dołącz do grona pierwszych użytkowników i zyskaj 6 miesięcy dostępu za darmo
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

                    <div className="mt-12 grid sm:grid-cols-3 gap-8">
                        <div className="text-white">
                            <div className="text-3xl font-bold mb-2">50</div>
                            <p className="text-white/80">Miejsc w fazie testowej</p>
                        </div>
                        <div className="text-white">
                            <CountdownTimer
                                targetDate={targetDate}
                                className="text-white"
                            />
                            <p className="text-white/80 mb-2">Do startu platformy</p>
                        </div>
                        <div className="text-white">
                            <div className="text-3xl font-bold mb-2">6 mies.</div>
                            <p className="text-white/80">Bezpłatnego dostępu</p>
                        </div>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default CTA;