import React, {createContext, useContext} from 'react';

export enum ApplicationType {
    VITEMA = 'vitema'
}

export interface ApplicationConfig {
    type: ApplicationType;
    name: string;
}

export const APPLICATION_CONFIGS: Record<ApplicationType, ApplicationConfig> = {
    [ApplicationType.VITEMA]: {
        type: ApplicationType.VITEMA,
        name: 'Vitema',
    }
};

interface ApplicationContextType {
    currentApplication: ApplicationType;
    applicationConfig: ApplicationConfig;
}

const defaultContextValue: ApplicationContextType = {
    currentApplication: ApplicationType.VITEMA,
    applicationConfig: APPLICATION_CONFIGS[ApplicationType.VITEMA]
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