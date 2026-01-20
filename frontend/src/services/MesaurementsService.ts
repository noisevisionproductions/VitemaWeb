import api from "../config/axios";
import {BodyMeasurements} from "../types/measurements";

export class MeasurementsService {
    private static readonly BASE_URL = '/measurements';

    static async getUserMeasurements(userId: string): Promise<BodyMeasurements[]> {
        const response = await api.get(`${this.BASE_URL}/user/${userId}`);
        return response.data;
    }

    static async deleteMeasurement(id: string): Promise<void> {
        await api.delete(`${this.BASE_URL}/${id}`);
    }
}