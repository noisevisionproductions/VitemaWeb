import { motion } from 'framer-motion';
import Container from "../../ui/landing/Container";
import NewsletterForm from "../forms/NewsletterForm";
import DeviceMockup from "./DeviceMockup";

const Hero = () => {
    return (
        <section className="relative min-h-screen flex items-center pt-20 bg-gradient-to-br from-surface to-primary/5 overflow-hidden">
            <Container className="relative py-20">
                <div className="grid lg:grid-cols-2 gap-12 items-center">
                    {/* Content */}
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.5 }}
                    >
                        <h1 className="text-4xl sm:text-5xl font-bold text-text-primary mb-6">
                            Stwórz i zarządzaj
                            <span className="text-primary block mt-2">
                                spersonalizowanymi dietami
                            </span>
                        </h1>
                        <p className="text-text-secondary text-lg mb-8">
                            Kompleksowe narzędzie do zarządzania dietami i komunikacji z klientami.
                            Oszczędź czas, zwiększ efektywność i rozwijaj swoją praktykę.
                        </p>
                        <NewsletterForm />
                        <div className="mt-4 flex items-start space-x-2">
                            <span className="inline-block mt-1 text-primary">🎁</span>
                            <p className="text-sm text-text-secondary">
                                <span className="font-medium">Oferta specjalna:</span> Pierwszych <span className="font-medium text-primary">50 użytkowników</span> otrzyma 6 miesięcy dostępu za darmo! Zapisz się na listę oczekujących.
                            </p>
                        </div>
                    </motion.div>

                    {/* Device Mockups-w kontenerze z lepszymi proporcjami */}
                    <motion.div
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.5, delay: 0.2 }}
                        className="w-full max-w-full"
                    >
                        <DeviceMockup />
                    </motion.div>
                </div>
            </Container>
        </section>
    );
};

export default Hero;