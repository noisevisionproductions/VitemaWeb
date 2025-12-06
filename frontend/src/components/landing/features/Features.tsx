import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {features} from "./featuresData";
import FeatureCard from "./FeatureCard";
import {useTranslation} from "react-i18next";

const Features = () => {
    const {t} = useTranslation();

    const sortedFeatures = [...features].sort((a, b) => {
        if (!a.status || a.status === 'available') return -1;
        if (!b.status || b.status === 'available') return 1;
        return 0;
    });

    return (
        <section id="features" className="py-20 bg-background">
            <Container>
                <SectionHeader
                    title={t('features.title')}
                    subtitle={t('features.subtitle')}
                />

                <div className="mt-16 grid gap-8 md:grid-cols-2 lg:grid-cols-3">
                    {sortedFeatures.map((feature) => (
                        <FeatureCard
                            key={feature.id}
                            {...feature}
                            title={t(`features.items.${feature.id}.title`)}
                            description={t(`features.items.${feature.id}.description`)}
                        />
                    ))}
                </div>

                <div className="mt-12 text-center">
                    <p className="text-text-secondary">
                        {t('features.footer')}
                        <span className="block mt-2">
                            {t('features.footerLinkPrefix')}{" "}
                            <a href="#contact" className="text-primary hover:underline">
                                {t('features.footerLink')}
                            </a>
                        </span>
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default Features;