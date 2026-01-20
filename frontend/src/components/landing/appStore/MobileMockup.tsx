import {motion} from 'framer-motion';

const MobileMockup = () => {
    return (
        <div className="relative w-full mx-auto">
            {/* Główny kontener telefonu */}
            <motion.div
                className="relative shadow-2xl rounded-[2rem] overflow-hidden border-[10px] border-gray-900 bg-gray-900 z-20"
                initial={{opacity: 0, scale: 0.9}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.6}}
            >
                <div className="absolute top-4 left-1/2 -translate-x-1/2 w-3 h-3 bg-black rounded-full z-30"></div>

                <div className="relative bg-white w-full h-full rounded-[1.5rem] overflow-hidden">
                    <img
                        src="/images/app_download_section.jpg"
                        alt="Aplikacja mobilna Vitema"
                        className="w-full h-auto object-cover"
                    />
                </div>
            </motion.div>

            <motion.div
                className="absolute top-1/4 -left-10 w-40 h-40 bg-primary/30 rounded-full blur-3xl -z-10"
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{duration: 0.8, delay: 0.2}}
            />
            <motion.div
                className="absolute bottom-10 -right-10 w-40 h-40 bg-secondary/30 rounded-full blur-3xl -z-10"
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{duration: 0.8, delay: 0.4}}
            />
        </div>
    );
};

export default MobileMockup;