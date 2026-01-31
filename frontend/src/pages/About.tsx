import CompanyMission from "../components/landing/about/CompanyMission";
import CompanyHistory from "../components/landing/about/CompanyHistory";
import CompanyValues from "../components/landing/about/CompanyValues";
import TeamSection from "../components/landing/about/TeamSection";
import {Helmet} from 'react-helmet-async';

const About = () => {
    return (
        <div className="pt-20">
            <Helmet>
                <title>O nas - Misja i Zespół Vitema</title>
                <meta name="description"
                      content="Poznaj zespół Noise Vision Software tworzący aplikację Vitema. Naszą misją jest wspieranie trenerów nowoczesną technologią."/>
                <link rel="canonical" href="https://vitema.pl/about"/>
            </Helmet>
            <CompanyMission/>
            <CompanyHistory/>
            <CompanyValues/>
            <TeamSection/>
        </div>
    );
};

export default About;