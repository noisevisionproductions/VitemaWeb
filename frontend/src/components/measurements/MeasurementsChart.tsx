import React from "react";
import {BodyMeasurements} from "../../types/measurements";
import {formatTimestamp} from "../../utils/dateFormatters";
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";

interface MeasurementsChartProps {
    measurements: BodyMeasurements[];
}

const MeasurementsChart: React.FC<MeasurementsChartProps> = ({measurements}) => {
    const data = measurements.map(m => ({
        date: formatTimestamp(m.date),
        waga: m.weight,
        pas: m.waist,
        biodra: m.hips,
        klatka: m.chest,
        ramie: m.biceps,
        udo: m.thigh
    })).reverse();

    return (
        <div className="h-96 w-full">
            <ResponsiveContainer>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                        type="monotone"
                        dataKey="waga"
                        stroke="#8884d8"
                        name="Waga [kg]"
                        strokeWidth={2}
                    />
                    <Line
                        type="monotone"
                        dataKey="pas"
                        stroke="#82ca9d"
                        name="Pas [cm]"
                        strokeWidth={2}
                    />
                    <Line
                        type="monotone"
                        dataKey="biodra"
                        stroke="#ffc658"
                        name="Biodra [cm]"
                        strokeWidth={2}
                    />
                    <Line
                        type="monotone"
                        dataKey="klatka"
                        stroke="#ff7300"
                        name="Klatka [cm]"
                        strokeWidth={2}
                    />
                    <Line
                        type="monotone"
                        dataKey="ramie"
                        stroke="#0088fe"
                        name="Ramię [cm]"
                        strokeWidth={2}
                    />
                    <Line
                        type="monotone"
                        dataKey="udo"
                        stroke="#00C49F"
                        name="Udo [cm]"
                        strokeWidth={2}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
};

export default MeasurementsChart;