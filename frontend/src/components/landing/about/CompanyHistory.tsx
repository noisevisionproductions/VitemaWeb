import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";
import {useTranslation} from "react-i18next";

const CompanyHistory = () => {
    const {t} = useTranslation();

    return (
        <section className="py-20 bg-background">
            <Container>
                <motion.div
                    initial={{opacity: 0}}
                    whileInView={{opacity: 1}}
                    viewport={{once: true}}
                    className="max-w-4xl mx-auto"
                >
                    <h2 className="text-3xl font-bold text-text-primary mb-8 text-center">
                        {t('history.title')}
                    </h2>

                    <div className="space-y-8">
                        <div className="bg-surface p-6 rounded-xl border border-border">
                            <h3 className="text-xl font-semibold text-text-primary mb-4">
                                {t('history.stage1.title')}
                            </h3>
                            <p className="text-text-secondary">
                                {t('history.stage1.desc')}
                            </p>
                        </div>

                        <div className="bg-surface p-6 rounded-xl border border-border">
                            <h3 className="text-xl font-semibold text-text-primary mb-4">
                                {t('history.stage2.title')}
                            </h3>
                            <p className="text-text-secondary">
                                {t('history.stage2.desc')}
                            </p>
                        </div>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default CompanyHistory;