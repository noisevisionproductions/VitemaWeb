import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import ContactCard from "./ContactCard";
import {EnvelopeIcon, PhoneIcon} from "@heroicons/react/16/solid";
import ContactForm from "./ContactForm";
import {useTranslation} from "react-i18next";

const Contact = () => {
    const {t} = useTranslation();

    return (
        <section id="contact" className="py-20 bg-surface">
            <Container>
                <SectionHeader
                    title={t('contact.title')}
                    subtitle={t('contact.subtitle')}
                />

                <div className="mt-12 grid lg:grid-cols-2 gap-12">
                    <div className="space-y-6">
                        <ContactCard
                            icon={PhoneIcon}
                            title={t('contact.cards.phone.title')}
                            description={t('contact.cards.phone.desc')}
                            content="+48 880 172 098"
                            action="tel:+48880172098"
                        />

                        <ContactCard
                            icon={EnvelopeIcon}
                            title={t('contact.cards.email.title')}
                            description={t('contact.cards.email.desc')}
                            content="kontakt@vitema.pl"
                            action="mailto:kontakt@vitema.pl"
                        />
                    </div>

                    <ContactForm/>
                </div>
            </Container>
        </section>
    );
};

export default Contact;