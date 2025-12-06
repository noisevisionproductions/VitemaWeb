import Container from "../../shared/ui/landing/Container";
import MobileMockup from "./MobileMockup";
import {useTranslation} from "react-i18next";

const DownloadAppSection = () => {
    const {t} = useTranslation();
    const googlePlayLink = "https://play.google.com/store/apps/details?id=com.noisevisionsoftware.szytadieta";

    return (
        <section className="py-20 bg-gradient-to-br from-surface via-primary/5 to-surface overflow-hidden">
            <Container>
                <div className="grid lg:grid-cols-2 gap-12 items-center">
                    {/* Tekst i Przycisk */}
                    <div className="order-2 lg:order-1">
                        <h2 className="text-3xl sm:text-4xl font-bold text-text-primary mb-6">
                            {t('download.titlePart1')} <br className="hidden sm:block"/>
                            <span className="text-primary">{t('download.titlePart2')}</span>
                        </h2>
                        <p className="text-text-secondary text-lg mb-8 max-w-xl">
                            {t('download.description')}
                        </p>

                        <div className="flex flex-col sm:flex-row gap-4">
                            <a
                                href={googlePlayLink}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center gap-3 px-6 py-3 bg-[#000000] text-white rounded-xl hover:bg-primary/90 hover:scale-105 transition-all duration-300 shadow-md hover:shadow-xl group w-full sm:w-auto justify-center"
                                aria-label={t('download.googlePlayAlt')}
                            >
                                <svg className="w-8 h-8 flex-shrink-0" viewBox="0 0 24 24" fill="currentColor">
                                    <path
                                        d="M18.763 11.573l-10.61-6.053c-1.007-.573-2.153.187-2.153 1.333v12.334c0 1.146 1.146 1.906 2.153 1.333l10.61-6.053c.96-.547.96-2.347 0-2.894zM4.993 4.79c.54-.306 1.153-.713 1.153 1.333v11.867c0 2.046-.613 1.64-1.153 1.333l10.61-6.053-10.61-6.053zM6.993 17.99v-11.98l9.514 5.99-9.514 5.99z"/>
                                    <path
                                        d="M3 20.405V3.596C3 2.677 3.793 2 4.656 2.284L21.19 10.877c0.719 0.373 0.719 1.875 0 2.248L4.656 21.717 C3.793 22.001 3 21.323 3 20.405z"/>
                                </svg>
                                <div className="text-left">
                                    <div
                                        className="text-[10px] uppercase font-medium opacity-80 leading-tight">{t('download.googlePlay')}</div>
                                    <div className="text-xl font-bold font-primary leading-tight">Google Play</div>
                                </div>
                            </a>
                        </div>

                        <p className="mt-4 text-sm text-text-secondary flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-secondary"
                                 viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd"
                                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                      clipRule="evenodd"/>
                            </svg>
                            {t('download.requirements')}
                        </p>
                    </div>

                    {/* Mockup telefonu */}
                    <div className="order-1 lg:order-2 flex justify-center lg:justify-end">
                        <div
                            className="relative max-w-[240px] sm:max-w-[260px] w-full transform rotate-6 hover:rotate-0 transition-transform duration-500 ease-in-out z-10">
                            <MobileMockup/>
                        </div>
                    </div>
                </div>
            </Container>
        </section>
    );
};

export default DownloadAppSection;