import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";
import NewsletterForm from "../forms/NewsletterForm";
import {useTranslation} from "react-i18next";

const CTA = () => {
    const {t} = useTranslation();

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
                        {t('cta.title')}
                    </h2>
                    <p className="text-lg text-white/90 mb-8">
                        {t('cta.subtitle')}
                    </p>

                    <div className="max-w-xl mx-auto">
                        <NewsletterForm
                            className="bg-white p-4 rounded-xl shadow-lg"
                            buttonClassName="bg-secondary hover:bg-secondary-dark"
                        />
                        <p className="mt-4 text-sm text-white/80">
                            {t('cta.disclaimer')}
                        </p>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default CTA;