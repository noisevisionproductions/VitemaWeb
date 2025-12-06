import {motion} from 'framer-motion';
import {useTranslation} from "react-i18next";

const DeviceMockup = () => {
    const {t} = useTranslation();

    return (
        <div className="relative w-full max-w-[600px] mx-auto">
            {/* Desktop/laptop mockup */}
            <div className="relative pb-[80%] w-[90%] mx-auto">
                <motion.div
                    className="absolute inset-0 shadow-xl rounded-xl overflow-hidden"
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6}}
                >
                    <img
                        src="/images/panel_photo.png"
                        alt={t('hero.mockup.panelAlt')}
                        className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
                </motion.div>

                {/* Mobile mockup */}
                <motion.div
                    className="absolute -bottom-[10%] -right-[10%] w-[40%] shadow-xl rounded-xl overflow-hidden border-4 border-white z-20"
                    initial={{opacity: 0, scale: 0.8}}
                    animate={{opacity: 1, scale: 1}}
                    transition={{duration: 0.6, delay: 0.3}}
                >
                    <img
                        src="/images/app_photo.jpg"
                        alt={t('hero.mockup.appAlt')}
                        className="w-full"
                    />
                </motion.div>

                {/* Decorative elements */}
                <motion.div
                    className="absolute -bottom-5 -left-5 w-24 h-24 bg-secondary/30 rounded-full blur-xl"
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    transition={{duration: 0.8, delay: 0.5}}
                />
                <motion.div
                    className="absolute top-10 -right-10 w-20 h-20 bg-primary/20 rounded-full blur-xl"
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    transition={{duration: 0.8, delay: 0.6}}
                />
            </div>
        </div>
    );
};

export default DeviceMockup;