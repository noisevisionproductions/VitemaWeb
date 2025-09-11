export enum ApplicationType {
    NUTRILOG = 'nutrilog',
    SCANDAL_SHUFFLE = 'scandal-shuffle'
}

export interface ApplicationConfig {
    type: ApplicationType;
    name: string;
    authMethod: 'firebase' | 'supabase';
}

export const APPLICATION_CONFIGS: Record<ApplicationType, ApplicationConfig> = {
    [ApplicationType.NUTRILOG]: {
        type: ApplicationType.NUTRILOG,
        name: 'NutriLog',
        authMethod: 'firebase'
    },
    [ApplicationType.SCANDAL_SHUFFLE]: {
        type: ApplicationType.SCANDAL_SHUFFLE,
        name: 'Scandal Shuffle',
        authMethod: 'supabase'
    }
};