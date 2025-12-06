import {Lightbulb, Shield, Users, Zap} from "lucide-react";
import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";
import {useTranslation} from "react-i18next";

const CompanyValues = () => {
    const {t} = useTranslation();

    const values = [
        {
            icon: Lightbulb,
            key: "innovation"
        },
        {
            icon: Users,
            key: "userFocus"
        },
        {
            icon: Shield,
            key: "security"
        },
        {
            icon: Zap,
            key: "efficiency"
        }
    ];

    return (
        <section className="py-20 bg-surface">
            <Container>
                <h2 className="text-3xl font-bold text-text-primary mb-12 text-center">
                    {t('values.title')}
                </h2>

                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {values.map((value, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{delay: index * 0.1}}
                            className="bg-background p-6 rounded-xl border border-border"
                        >
                            <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                                <value.icon className="w-6 h-6 text-primary"/>
                            </div>
                            <h3 className="text-xl font-semibold text-text-primary mb-2">
                                {t(`values.items.${value.key}.title`)}
                            </h3>
                            <p className="text-text-secondary">
                                {t(`values.items.${value.key}.description`)}
                            </p>
                        </motion.div>
                    ))}
                </div>
            </Container>
        </section>
    );
};

export default CompanyValues;