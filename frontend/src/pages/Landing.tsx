import {useEffect} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import Hero from "../components/landing/hero/Hero";
import Features from "../components/landing/features/Features";
import ForWho from "../components/landing/forWho/ForWho";
import FAQ from "../components/landing/faq/FAQ";
import Contact from "../components/landing/contact/Contact";
import CTA from "../components/landing/cta/CTA";
import MarketStats from "../components/landing/marketStats/MarketStats";
import DownloadAppSection from "../components/landing/appStore/DownloadAppSection";
import {Helmet} from "react-helmet-async";
import RevenueUpsell from "../components/landing/upsell/RevenueUpsell";

const Landing = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        if (location.state && location.state.scrollTo) {
            const sectionId = location.state.scrollTo;

            const element = document.getElementById(sectionId);

            if (element) {
                setTimeout(() => {
                    const headerOffset = 80;
                    const elementPosition = element.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.scrollY - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });

                    navigate('/', {replace: true, state: {}});
                }, 100);
            }
        }
    }, [location, navigate]);
    return (
        <>
            <Helmet>
                <title>Vitema - Aplikacja dla Trenerów Personalnych i Dietetyków | Szybkie diety online</title>

                <meta name="description"
                      content="Vitema to narzędzie dla Trenerów i Dietetyków. Układaj jadłospisy w 5 minut, generuj automatyczną listę zakupów i prowadź podopiecznych w dedykowanej aplikacji mobilnej. Zastąp Excela nowoczesnym systemem."/>

                <meta name="keywords"
                      content="Vitema, aplikacja dla trenera personalnego, program do układania diet, współpraca online, aplikacja dietetyczna, dieta dla podopiecznego, lista zakupów app"/>

                <meta property="og:title" content="Vitema - Twoja aplikacja do prowadzenia podopiecznych"/>
                <meta property="og:description"
                      content="Koniec z PDF-ami. Daj podopiecznym dietę i listę zakupów w aplikacji na telefon. Sprawdź Vitemę!"/>
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://vitema.pl/"/>
            </Helmet>

            <Hero/>
            <MarketStats/>
            <Features/>
            <RevenueUpsell/>
            <ForWho/>
            <DownloadAppSection/>
            <FAQ/>
            <Contact/>
            <CTA/>
        </>
    );
};

export default Landing;