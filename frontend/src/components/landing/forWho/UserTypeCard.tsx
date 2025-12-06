import {UserType} from "./forWhoData";
import {motion} from "framer-motion";
import {CheckCircleIcon} from "@heroicons/react/16/solid";

interface UserTypeCardProps extends UserType {
    title: string;
    description: string;
    benefits: string[];
}

const UserTypeCard = ({title, description, benefits, icon: Icon, primary}: UserTypeCardProps) => {
    return (
        <motion.div
            whileHover={{scale: 1.02}}
            className={`p-8 rounded-xl transition-all duration-200 ${
                primary
                    ? 'bg-background border border-border hover:border-primary/20'
                    : 'bg-gradient-to-br from-primary/5 to-secondary/5 border border-secondary/10 hover:border-secondary/20'
            }`}
        >
            <div className={`w-14 h-14 rounded-lg flex items-center justify-center mb-6 ${
                primary ? 'bg-primary/10' : 'bg-secondary/10'
            }`}>
                <Icon className={`w-8 h-8 ${primary ? 'text-primary' : 'text-secondary'}`}/>
            </div>

            <h3 className="text-2xl font-semibold text-text-primary mb-4">
                {title}
            </h3>

            <p className="text-text-secondary mb-6">
                {description}
            </p>

            <ul className="space-y-3">
                {benefits.map((benefit, index) => (
                    <li key={index} className="flex items-start gap-3">
                        <CheckCircleIcon className={`w-6 h-6 flex-shrink-0 ${
                            primary ? 'text-primary' : 'text-secondary'
                        }`}/>
                        <span className="text-text-secondary">
                            {benefit}
                        </span>
                    </li>
                ))}
            </ul>
        </motion.div>
    );
};

export default UserTypeCard;