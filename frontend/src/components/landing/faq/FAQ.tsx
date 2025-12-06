import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {faqItems} from './faqData';
import FAQItem from "./FAQItem";
import {useTranslation} from "react-i18next";

const FAQ = () => {
    const {t} = useTranslation();

    return (
        <section id="faq" className="py-20 bg-background">
            <Container>
                <SectionHeader
                    title={t('faq.title')}
                    subtitle={t('faq.subtitle')}
                />

                <div className="mt-12 max-w-3xl mx-auto divide-y divide-border">
                    {faqItems.map((item) => (
                        <FAQItem
                            key={item.id}
                            question={t(`faq.items.${item.id}.question`)}
                            answer={t(`faq.items.${item.id}.answer`)}
                        />
                    ))}
                </div>
            </Container>
        </section>
    );
};

export default FAQ;