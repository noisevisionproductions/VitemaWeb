import * as Icons from 'lucide-react';
import { LucideProps } from 'lucide-react';
import { ForwardRefExoticComponent } from 'react';

type IconComponent = ForwardRefExoticComponent<LucideProps>;

export const getLucideIcon = (iconName: string | undefined): IconComponent | null => {
    const icon = Icons[iconName as keyof typeof Icons] as IconComponent | undefined;
    return icon || null;
};