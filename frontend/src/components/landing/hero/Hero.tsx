import {motion} from 'framer-motion';
import Container from "../../shared/ui/landing/Container";
import NewsletterForm from "../forms/NewsletterForm";
import DeviceMockup from "./DeviceMockup";
import {useTranslation} from "react-i18next";

const Hero = () => {
    const {t} = useTranslation();

    return (
        <section
            className="relative min-h-screen flex items-center pt-20 bg-gradient-to-br from-surface to-primary/5 overflow-hidden">
            <Container className="relative py-20">
                <div className="grid lg:grid-cols-2 gap-12 items-center">
                    {/* Content */}
                    <motion.div
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.5}}
                    >
                        <h1 className="text-4xl sm:text-5xl font-bold text-text-primary mb-6">
                            {t('hero.titlePart1')}
                            <span className="text-primary block mt-2">
                                {t('hero.titlePart2')}
                            </span>
                        </h1>
                        <p className="text-text-secondary text-lg mb-8">
                            {t('hero.subtitle')}
                        </p>
                        <NewsletterForm/>
                        <div className="mt-3 flex items-start space-x-2">
                            {t('cta.disclaimer')}
                        </div>
                    </motion.div>

                    {/* Device Mockups */}
                    <motion.div
                        initial={{opacity: 0, x: 20}}
                        animate={{opacity: 1, x: 0}}
                        transition={{duration: 0.5, delay: 0.2}}
                        className="w-full max-w-full"
                    >
                        <DeviceMockup/>
                    </motion.div>
                </div>
            </Container>
        </section>
    );
};

export default Hero;