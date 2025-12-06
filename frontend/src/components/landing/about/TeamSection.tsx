import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";
import {useTranslation} from "react-i18next";

const TeamSection = () => {
    const {t} = useTranslation();

    return (
        <section className="py-20 bg-background">
            <Container>
                <motion.div
                    initial={{opacity: 0}}
                    whileInView={{opacity: 1}}
                    viewport={{once: true}}
                    className="max-w-3xl mx-auto text-center"
                >
                    <h2 className="text-3xl font-bold text-text-primary mb-8">
                        {t('team.title')}
                    </h2>
                    <div className="bg-surface p-8 rounded-xl border border-border">
                        <div className="w-40 h-40 mx-auto rounded-full overflow-hidden mb-6">
                            <img
                                src="/images/noisevisionselfie.jpg"
                                alt={t('team.photoAlt')}
                                className="w-full h-full object-cover"
                            />
                        </div>
                        <h3 className="text-xl font-semibold text-text-primary mb-2">
                            {t('team.founderRole')}
                        </h3>
                        <p className="text-text-secondary mb-4">
                            Tomasz Jurczyk
                        </p>
                        <p className="text-text-secondary">
                            {t('team.founderDesc')}
                        </p>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default TeamSection;