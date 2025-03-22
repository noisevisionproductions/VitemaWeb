import Container from "../../ui/landing/Container";
import SectionHeader from "../../ui/landing/SectionHeader";
import {features} from "./featuresData";
import FeatureCard from "./FeatureCard";

const Features = () => {
    const sortedFeatures = [...features].sort((a, b) => {
        if (!a.status || a.status === 'available') return -1;
        if (!b.status || b.status === 'available') return 1;
        return 0;
    });

    return (
        <section id="features" className="py-20 bg-background">
            <Container>
                <SectionHeader
                    title="Oszczędzaj czas dzięki automatyzacji"
                    subtitle="Skoncentruj się na pracy z klientami, a rutynowe zadania zostaw naszemu systemowi"
                />

                <div className="mt-16 grid gap-8 md:grid-cols-2 lg:grid-cols-3">
                    {sortedFeatures.map((feature) => (
                        <FeatureCard key={feature.id} {...feature}/>
                    ))}
                </div>

                <div className="mt-12 text-center">
                    <p className="text-text-secondary">
                        Stale rozwijamy naszą platformę o nowe funkcje na podstawie potrzeb dietetyków.
                        <span className="block mt-2">Masz pomysł? <a href="#contact"
                                                                     className="text-primary hover:underline">Daj nam znać!</a></span>
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default Features;