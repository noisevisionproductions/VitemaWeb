import React from 'react';

interface SurveyResultsChartProps {
    title: string;
    data: Record<string, number>;
    total: number;
    type?: 'bar' | 'pie';
}

const SurveyResultsChart: React.FC<SurveyResultsChartProps> = ({
                                                                   title,
                                                                   data,
                                                                   total,
                                                                   type = 'bar'
                                                               }) => {
    const sortedData = Object.entries(data)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

    const getPercentage = (value: number) => {
        return ((value / total) * 100).toFixed(1);
    };

    return (
        <div className="bg-white rounded-lg border border-gray-200">
            <h3 className="text-base font-medium p-4 border-b border-gray-200">{title}</h3>

            <div className="p-4">
                {sortedData.length === 0 ? (
                    <div className="text-center text-gray-500 py-8">Brak danych</div>
                ) : type === 'bar' ? (
                    <div className="space-y-3">
                        {sortedData.map(([label, value]) => (
                            <div key={label}>
                                <div className="flex justify-between text-sm mb-1">
                                    <span className="font-medium truncate">{label}</span>
                                    <span className="text-gray-500">{value} ({getPercentage(value)}%)</span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2.5">
                                    <div
                                        className="bg-primary h-2.5 rounded-full"
                                        style={{width: `${getPercentage(value)}%`}}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="grid grid-cols-2 gap-2">
                        <div className="relative h-48">
                            <div className="absolute inset-0 flex items-center justify-center">
                                <svg viewBox="0 0 100 100" className="w-full h-full">
                                    {sortedData.map(([label, value], index) => {
                                        const percentage = (value / total) * 100;
                                        const startAngle = index === 0 ? 0 : sortedData
                                            .slice(0, index)
                                            .reduce((sum, [, val]) => sum + (val / total) * 100, 0);

                                        const endAngle = startAngle + percentage;

                                        // Konwersja na radiany
                                        const startRad = (startAngle / 100) * 2 * Math.PI - Math.PI / 2;
                                        const endRad = (endAngle / 100) * 2 * Math.PI - Math.PI / 2;

                                        // Obliczenie punktów łuku
                                        const x1 = 50 + 40 * Math.cos(startRad);
                                        const y1 = 50 + 40 * Math.sin(startRad);
                                        const x2 = 50 + 40 * Math.cos(endRad);
                                        const y2 = 50 + 40 * Math.sin(endRad);

                                        // Flaga dla łuku dużego (większy niż 180 stopni)
                                        const largeArcFlag = percentage > 50 ? 1 : 0;

                                        // Kolory dla różnych segmentów (możesz dostosować)
                                        const colors = [
                                            '#2D87BB', '#43B988', '#4ECDC4', '#FFD93D',
                                            '#FF7E67', '#6DCBA3', '#4FA8D4', '#338F69',
                                            '#1B6690', '#95A5A6'
                                        ];

                                        return (
                                            <path
                                                key={label}
                                                d={`M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArcFlag} 1 ${x2} ${y2} Z`}
                                                fill={colors[index % colors.length]}
                                            />
                                        );
                                    })}
                                </svg>
                            </div>
                        </div>
                        <div className="space-y-2">
                            {sortedData.map(([label, value], index) => {
                                const colors = [
                                    '#2D87BB', '#43B988', '#4ECDC4', '#FFD93D',
                                    '#FF7E67', '#6DCBA3', '#4FA8D4', '#338F69',
                                    '#1B6690', '#95A5A6'
                                ];

                                return (
                                    <div key={label} className="flex items-center">
                                        <div
                                            className="w-3 h-3 mr-2 rounded-sm"
                                            style={{backgroundColor: colors[index % colors.length]}}
                                        ></div>
                                        <div className="text-xs">
                                            <span className="truncate">{label}</span>
                                            <span className="ml-1 text-gray-500">
                                                ({getPercentage(value)}%)
                                            </span>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SurveyResultsChart;