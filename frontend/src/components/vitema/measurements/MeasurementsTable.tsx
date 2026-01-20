import React from 'react';
import { BodyMeasurements } from '../../../types/measurements';
import { formatTimestamp } from '../../../utils/dateFormatters';

interface MeasurementsTableProps {
    measurements: BodyMeasurements[];
}

const MeasurementsTable: React.FC<MeasurementsTableProps> = ({ measurements }) => {
    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Data
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Waga [kg]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Pas [cm]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Biodra [cm]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Klatka [cm]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Ramię [cm]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Udo [cm]
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Notatka
                    </th>
                </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                {measurements.map((measurement) => (
                    <tr key={measurement.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {formatTimestamp(measurement.date)}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.weight}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.waist}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.hips}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.chest}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.biceps}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                            {measurement.thigh}
                        </td>
                        <td className="px-4 py-3 text-sm">
                            {measurement.note}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default MeasurementsTable;