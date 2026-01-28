import Container from "../../shared/ui/landing/Container";
import {BanknotesIcon, SparklesIcon, TrophyIcon} from "@heroicons/react/24/outline";
import {useTranslation} from "react-i18next";
import {motion} from "framer-motion";
import SectionHeader from "../../shared/ui/landing/SectionHeader";

const RevenueUpsell = () => {
    const {t} = useTranslation();

    const cards = [
        {
            id: 1,
            icon: BanknotesIcon,
            color: "text-green-600",
            bg: "bg-green-100"
        },
        {
            id: 2,
            icon: SparklesIcon,
            color: "text-blue-600",
            bg: "bg-blue-100"
        },
        {
            id: 3,
            icon: TrophyIcon,
            color: "text-yellow-600",
            bg: "bg-yellow-100"
        }
    ];

    return (
        <section className="py-20 bg-surface border-y border-border">
            <Container>

                <SectionHeader
                    title={t('upsell.title')}
                    subtitle={t('upsell.subtitle')}
                />
                {/*    <div className="text-center max-w-3xl mx-auto mb-16">
                    <h2 className="text-3xl font-bold text-text-primary mb-4">
                        {t('upsell.title')}
                    </h2>
                    <p className="text-lg text-text-secondary">
                        {t('upsell.subtitle')}
                    </p>
                </div>*/}

                <div className="mt-16 grid md:grid-cols-3 gap-8">
                    {cards.map((card, index) => (
                        <motion.div
                            key={card.id}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{delay: index * 0.2}}
                            className="bg-background p-8 rounded-2xl shadow-sm border border-border hover:border-primary/20 transition-all"
                        >
                            <div className={`w-14 h-14 rounded-xl ${card.bg} flex items-center justify-center mb-6`}>
                                <card.icon className={`w-8 h-8 ${card.color}`}/>
                            </div>
                            <h3 className="text-xl font-bold text-text-primary mb-3">
                                {t(`upsell.cards.${card.id}.title`)}
                            </h3>
                            <p className="text-text-secondary leading-relaxed">
                                {t(`upsell.cards.${card.id}.description`)}
                            </p>
                        </motion.div>
                    ))}
                </div>
            </Container>
        </section>
    );
};

export default RevenueUpsell;