import {FC, useEffect, useState} from "react";
import Container from "../shared/ui/landing/Container";
import {Bars3Icon, XMarkIcon} from "@heroicons/react/16/solid";
import Logo from "../shared/ui/landing/Logo";
import * as React from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext";
import {Link} from 'react-router-dom';
import {AnimatePresence, motion} from "framer-motion";
import {useTranslation} from "react-i18next";
import LanguageSwitcher from "../shared/ui/landing/LanguageSwitcher";

const Header: FC = () => {
    const {t} = useTranslation();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isScrolled, setIsScrolled] = useState(false);
    const [isOverCTA, setIsOverCTA] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const {currentUser} = useAuth();

    const navigation = [
        {name: t('nav.features'), href: '#features'},
        {name: t('nav.forWho'), href: '#for-who'},
        {name: t('nav.faq'), href: '#faq'},
        {name: t('nav.contact'), href: '#contact'},
    ];

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 0);

            if (location.pathname === '/') {
                const ctaSection = document.getElementById('cta-section');
                if (ctaSection) {
                    const ctaRect = ctaSection.getBoundingClientRect();
                    setIsOverCTA(ctaRect.top <= 80);
                }
            }
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [location.pathname]);

    const handleScrollToSection = (event: React.MouseEvent<HTMLAnchorElement | HTMLButtonElement>, sectionId: string) => {
        event.preventDefault();

        if (location.pathname !== '/') {
            navigate('/', {state: {scrollTo: sectionId}});
            setIsMobileMenuOpen(false);
            return;
        }

        const element = document.getElementById(sectionId);
        if (element) {
            const headerOffset = 80;
            const elementPosition = element.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.scrollY - headerOffset;

            window.scrollTo({
                top: offsetPosition,
                behavior: 'smooth'
            });
        }

        setIsMobileMenuOpen(false);
    }

    const headerClassName = `fixed w-full z-50 py-4 transition-all duration-200 ${
        isScrolled || location.pathname !== '/'
            ? isOverCTA
                ? 'bg-primary/80 backdrop-blur-sm shadow-sm'
                : 'bg-white/80 backdrop-blur-sm shadow-sm'
            : 'bg-transparent'
    }`;

    const linkClassName = `transition-colors duration-200 ${
        isOverCTA
            ? 'text-white/90 hover:text-white'
            : 'text-text-secondary hover:text-primary'
    }`;

    const buttonClassName = `px-6 py-2 rounded-lg transition-colors duration-200 ${
        isOverCTA
            ? 'bg-white text-primary hover:bg-white/90'
            : 'bg-primary text-white hover:bg-primary-dark'
    }`;

    const adminButtonClassName = `px-6 py-2 rounded-lg transition-colors duration-200 ${
        isOverCTA
            ? 'bg-secondary text-white hover:bg-secondary/90'
            : 'bg-secondary text-white hover:bg-secondary/90'
    }`;

    return (
        <header className={headerClassName}>
            <Container>
                <div className="flex items-center justify-between">
                    <Logo variant="full"/>

                    {/* Desktop navigation */}
                    <nav className="hidden md:flex items-center gap-8">
                        {navigation.map((item) => (
                            <a
                                key={item.name}
                                href={item.href}
                                onClick={(e) => handleScrollToSection(e, item.href.slice(1))}
                                className={linkClassName}
                            >
                                {item.name}
                            </a>
                        ))}

                        {currentUser ? (
                            <Link
                                to="/dashboard"
                                className={adminButtonClassName}
                            >
                                {t('nav.panel')}
                            </Link>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className={linkClassName}
                                >
                                    {t('nav.login')}
                                </Link>
                                <button
                                    onClick={(e) => handleScrollToSection(e, 'cta-section')}
                                    className={buttonClassName}
                                >
                                    {t('nav.join')}
                                </button>
                            </>
                        )}

                        {/* Language Switcher Desktop */}
                        <div className="px-2 border-l border-gray-300/30">
                            <LanguageSwitcher className={linkClassName}/>
                        </div>

                    </nav>

                    {/* Mobile Menu Button */}
                    <button
                        className="md:hidden"
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    >
                        {isMobileMenuOpen ? (
                            <XMarkIcon className="h-6 w-6"/>
                        ) : (
                            <Bars3Icon className="h-6 w-6"/>
                        )}
                    </button>
                </div>

                {/* Mobile Navigation */}
                <AnimatePresence>
                    {isMobileMenuOpen && (
                        <motion.nav
                            initial={{opacity: 0, y: -20}}
                            animate={{opacity: 1, y: 0}}
                            exit={{opacity: 0, y: -20}}
                            className="md:hidden mt-4 p-4 space-y-4 bg-white rounded-lg shadow-lg absolute left-4 right-4 z-50"
                        >
                            {navigation.map((item) => (
                                <a
                                    key={item.name}
                                    href={item.href}
                                    onClick={(e) => handleScrollToSection(e, item.href.slice(1))}
                                    className="block text-text-secondary hover:text-primary font-medium py-2"
                                >
                                    {item.name}
                                </a>
                            ))}

                            {/* Language Switcher Mobile */}
                            <div className="border-t border-b border-gray-100 py-2">
                                <LanguageSwitcher isMobile={true}/>
                            </div>

                            {currentUser ? (
                                <Link
                                    to="/dashboard"
                                    className="block w-full bg-secondary text-white px-6 py-3 rounded-lg hover:bg-secondary/90 text-center"
                                >
                                    {t('nav.panel')}
                                </Link>
                            ) : (
                                <>
                                    <Link
                                        to="/login"
                                        className="block text-text-secondary hover:text-primary py-2 font-medium w-full text-left"
                                    >
                                        {t('nav.login')}
                                    </Link>
                                    <button
                                        onClick={(e) => handleScrollToSection(e, 'cta-section')}
                                        className="w-full bg-primary text-white px-6 py-3 rounded-lg hover:bg-primary-dark mt-2"
                                    >
                                        {t('nav.join')}
                                    </button>
                                </>
                            )}
                        </motion.nav>
                    )}
                </AnimatePresence>
            </Container>
        </header>
    );
};

export default Header;