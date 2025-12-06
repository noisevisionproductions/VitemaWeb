import {ClockIcon, UsersIcon} from "lucide-react";
import {ChartBarIcon} from "@heroicons/react/24/outline";
import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {useTranslation} from "react-i18next";

const MarketStats = () => {
    const {t} = useTranslation();

    const statistics = [
        {
            icon: ClockIcon,
            value: "12h",
            label: t('stats.items.1.label'),
            description: t('stats.items.1.description')
        },
        {
            icon: UsersIcon,
            value: "35%",
            label: t('stats.items.2.label'),
            description: t('stats.items.2.description')
        },
        {
            icon: ChartBarIcon,
            value: "68%",
            label: t('stats.items.3.label'),
            description: t('stats.items.3.description')
        }
    ];

    return (
        <section className="py-20 bg-surface">
            <Container>
                <SectionHeader
                    title={t('stats.title')}
                    subtitle={t('stats.subtitle')}
                />

                <div className="mt-12 grid md:grid-cols-3 gap-8">
                    {statistics.map((stat, index) => (
                        <div
                            key={index}
                            className="p-6 bg-white rounded-xl border border-border hover:border-primary/20 transition-all duration-200 hover:shadow-lg"
                        >
                            <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                                <stat.icon className="w-6 h-6 text-primary"/>
                            </div>

                            <div className="text-3xl font-bold text-primary mb-2">
                                {stat.value}
                            </div>

                            <div className="text-lg font-medium text-text-primary mb-3">
                                {stat.label}
                            </div>

                            <p className="text-text-secondary">
                                {stat.description}
                            </p>
                        </div>
                    ))}
                </div>

                <div className="mt-12 text-xs text-center">
                    <p className="text-text-secondary">
                        {t('stats.note')}
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default MarketStats;