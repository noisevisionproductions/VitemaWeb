import Container from "../../ui/landing/Container";
import SectionHeader from "../../ui/landing/SectionHeader";
import ContactCard from "./ContactCard";
import {EnvelopeIcon, PhoneIcon} from "@heroicons/react/16/solid";
import ContactForm from "./ContactForm";

const Contact = () => {
    return (
        <section id="contact" className="py-20 bg-surface">
            <Container>
                <SectionHeader
                    title="Skontaktuj się z nami"
                    subtitle="Chętnie odpowiemy na Twoje pytania i pomożemy w rozpoczęciu pracy z NutriLog"
                />

                <div className="mt-12 grid lg:grid-cols-2 gap-12">
                    {/* Lewa kolumna-karty kontaktowe */}
                    <div className="space-y-6">
                        <ContactCard
                            icon={PhoneIcon}
                            title="Zadzwoń do nas"
                            description="Porozmawiaj z nami bezpośrednio o Twoich potrzebach"
                            content="+48 880 172 098"
                            action="tel:+48880172098"
                        />

                        <ContactCard
                            icon={EnvelopeIcon}
                            title="Napisz email"
                            description="Odpowiemy na Twoją wiadomość w ciągu 24 godzin"
                            content="kontakt@nutriLog.pl"
                            action="mailto:kontakt@nutriLog.pl"
                        />
                    </div>

                    {/* Prawa kolumna-formularz */}
                    <ContactForm/>
                </div>
            </Container>
        </section>
    );
};

export default Contact;