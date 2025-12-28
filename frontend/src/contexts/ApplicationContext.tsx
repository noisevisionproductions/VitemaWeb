import React, {createContext, useContext} from 'react';

export enum ApplicationType {
    NUTRILOG = 'nutrilog'
}

export interface ApplicationConfig {
    type: ApplicationType;
    name: string;
}

export const APPLICATION_CONFIGS: Record<ApplicationType, ApplicationConfig> = {
    [ApplicationType.NUTRILOG]: {
        type: ApplicationType.NUTRILOG,
        name: 'NutriLog',
    }
};

interface ApplicationContextType {
    currentApplication: ApplicationType;
    applicationConfig: ApplicationConfig;
}

const defaultContextValue: ApplicationContextType = {
    currentApplication: ApplicationType.NUTRILOG,
    applicationConfig: APPLICATION_CONFIGS[ApplicationType.NUTRILOG]
};

const ApplicationContext = createContext<ApplicationContextType>(defaultContextValue);

export const useApplication = () => {
    return useContext(ApplicationContext);
};

export const ApplicationProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    return (
        <ApplicationContext.Provider value={defaultContextValue}>
            {children}
        </ApplicationContext.Provider>
    );
};