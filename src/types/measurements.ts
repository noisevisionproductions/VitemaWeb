export enum MeasurementSourceType {
    APP = 'APP',
    GOOGLE_SHEET = 'GOOGLE_SHEET'
}

export enum MeasurementType {
    WEIGHT_ONLY = 'WEIGHT_ONLY',
    FULL_BODY = 'FULL_BODY'
}

export interface BodyMeasurements {
    id: string;
    userId: string;
    date: number;
    height: number;
    weight: number;
    neck: number;
    biceps: number;
    chest: number;
    waist: number;
    belt: number;
    hips: number;
    thigh: number;
    calf: number;
    note: string;
    weekNumber: number;
    measurementType: MeasurementType;
    sourceType: MeasurementSourceType;
}