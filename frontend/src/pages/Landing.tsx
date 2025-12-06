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
            <Hero/>
            <MarketStats/>
            <Features/>
            <ForWho/>
            <DownloadAppSection/>
            <FAQ/>
            <Contact/>
            <CTA/>
        </>
    );
};

export default Landing;