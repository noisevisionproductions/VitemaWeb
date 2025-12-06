import {useState} from 'react';
import {motion, AnimatePresence} from 'framer-motion';
import {ChevronDownIcon} from '@heroicons/react/24/outline';

interface FAQItemProps {
    question: string;
    answer: string;
}

const FAQItem = ({question, answer}: FAQItemProps) => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <div className="py-4">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="flex w-full items-start justify-between text-left focus:outline-none group"
            >
                <span className={`text-lg font-medium transition-colors duration-200 ${
                    isOpen ? 'text-primary' : 'text-text-primary group-hover:text-primary'
                }`}>
                    {question}
                </span>
                <span className={`ml-6 flex h-7 items-center transition-transform duration-200 ${
                    isOpen ? 'rotate-180' : 'rotate-0'
                }`}>
                    <ChevronDownIcon className="h-5 w-5 text-text-secondary group-hover:text-primary"/>
                </span>
            </button>
            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{height: 0, opacity: 0}}
                        animate={{height: 'auto', opacity: 1}}
                        exit={{height: 0, opacity: 0}}
                        transition={{duration: 0.2}}
                        className="overflow-hidden"
                    >
                        <p className="mt-2 text-text-secondary pr-12">
                            {answer}
                        </p>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};

export default FAQItem;