import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {userTypes} from './forWhoData';
import UserTypeCard from "./UserTypeCard";
import {useTranslation} from "react-i18next";

const ForWho = () => {
    const {t} = useTranslation();

    const sortedUserTypes = [...userTypes].sort((a, b) => {
        if (a.primary && !b.primary) return -1;
        if (!a.primary && b.primary) return 1;
        return 0;
    });

    return (
        <section id="for-who" className="py-20 bg-background">
            <Container>
                <SectionHeader
                    title={t('forWho.title')}
                    subtitle={t('forWho.subtitle')}
                />

                <div className="mt-16 grid gap-8 md:grid-cols-2 lg:grid-cols-4">
                    {sortedUserTypes.map((type) => (
                        <UserTypeCard
                            key={type.id}
                            {...type}
                            title={t(`forWho.items.${type.id}.title`)}
                            description={t(`forWho.items.${type.id}.description`)}
                            benefits={t(`forWho.items.${type.id}.benefits`, {returnObjects: true}) as string[]}
                        />
                    ))}
                </div>

                <div className="mt-12 text-center">
                    <p className="text-text-secondary max-w-2xl mx-auto">
                        {t('forWho.footer')}
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default ForWho;