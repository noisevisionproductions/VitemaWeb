import React, {useState} from 'react';
import {ArrowDownUp, BarChart3, PieChart} from 'lucide-react';

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
                                                                   type: initialType = 'bar'
                                                               }) => {
    const [chartType, setChartType] = useState<'bar' | 'pie'>(initialType);
    const [sortBy, setSortBy] = useState<'value' | 'label'>('value');

    const sortedData = Object.entries(data)
        .sort((a, b) => {
            if (sortBy === 'value') return b[1] - a[1];
            return a[0].localeCompare(b[0]);
        })
        .slice(0, 10);

    const getPercentage = (value: number) => {
        return ((value / total) * 100).toFixed(1);
    };

    // Paleta kolorów zgodna z theme.css
    const colors = [
        'var(--color-primary)', 'var(--color-secondary)',
        'var(--color-protein)', 'var(--color-carbs)',
        'var(--color-fats)', 'var(--color-primary-light)',
        'var(--color-secondary-light)', 'var(--color-primary-dark)',
        'var(--color-secondary-dark)', 'var(--color-calories)'
    ];

    // Sprawdzamy, czy mamy tylko jedną odpowiedź (100%)
    const hasSingleResult = sortedData.length === 1;

    return (
        <div
            className="bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-200">
            <div className="flex justify-between items-center p-4 border-b border-gray-200">
                <h3 className="text-base font-medium text-text-primary">{title}</h3>
                <div className="flex space-x-2">
                    <button
                        onClick={() => setSortBy(sortBy === 'value' ? 'label' : 'value')}
                        className="p-1.5 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md transition-colors"
                        title={sortBy === 'value' ? 'Sortuj alfabetycznie' : 'Sortuj według wartości'}
                    >
                        <ArrowDownUp size={16}/>
                    </button>
                    <button
                        onClick={() => setChartType('bar')}
                        className={`p-1.5 rounded-md transition-colors ${
                            chartType === 'bar'
                                ? 'text-primary bg-primary-light bg-opacity-20'
                                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                        }`}
                        title="Wykres słupkowy"
                    >
                        <BarChart3 size={16}/>
                    </button>
                    <button
                        onClick={() => setChartType('pie')}
                        className={`p-1.5 rounded-md transition-colors ${
                            chartType === 'pie'
                                ? 'text-primary bg-primary-light bg-opacity-20'
                                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                        }`}
                        title="Wykres kołowy"
                    >
                        <PieChart size={16}/>
                    </button>
                </div>
            </div>

            <div className="p-4">
                {sortedData.length === 0 ? (
                    <div className="text-center text-gray-500 py-8">Brak danych</div>
                ) : chartType === 'bar' ? (
                    <div className="space-y-3 max-h-[350px] overflow-y-auto pr-2">
                        {sortedData.map(([label, value], index) => (
                            <div key={label}>
                                <div className="flex justify-between text-sm mb-1">
                                    <span className="font-medium max-w-[75%] truncate" title={label}>{label}</span>
                                    <span
                                        className="text-gray-500 whitespace-nowrap">{value} ({getPercentage(value)}%)</span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                                    <div
                                        className="h-2.5 rounded-full transition-all duration-500 ease-out"
                                        style={{
                                            width: `${getPercentage(value)}%`,
                                            backgroundColor: colors[index % colors.length]
                                        }}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="relative h-64 flex items-center justify-center">
                            {hasSingleResult ? (
                                // Specjalny widok dla jednej odpowiedzi (100%)
                                <div className="text-center">
                                    <div
                                        className="mx-auto w-32 h-32 rounded-full flex items-center justify-center mb-2"
                                        style={{backgroundColor: colors[0]}}
                                    >
                                        <span className="text-white text-xl font-bold">100%</span>
                                    </div>
                                    <div className="text-sm font-medium">{sortedData[0][0]}</div>
                                </div>
                            ) : (
                                <svg viewBox="0 0 100 100" className="w-full h-full">
                                    {/* Dodajemy cień dla diagramu */}
                                    <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                                        <feDropShadow dx="0" dy="1" stdDeviation="2" floodOpacity="0.1"/>
                                    </filter>

                                    {/* Biały okrąg w środku dla lepszego wyglądu */}
                                    <circle cx="50" cy="50" r="30" fill="white"/>

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

                                        // Obliczenie punktu środkowego segmentu dla etykiety
                                        const midAngle = startRad + (endRad - startRad) / 2;
                                        const labelX = 50 + 30 * Math.cos(midAngle);
                                        const labelY = 50 + 30 * Math.sin(midAngle);

                                        return (
                                            <g key={label} filter="url(#shadow)"
                                               className="hover:opacity-90 transition-opacity">
                                                <path
                                                    d={`M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArcFlag} 1 ${x2} ${y2} Z`}
                                                    fill={colors[index % colors.length]}
                                                    stroke="white"
                                                    strokeWidth="1"
                                                />
                                                {percentage >= 5 && (
                                                    <text
                                                        x={labelX}
                                                        y={labelY}
                                                        textAnchor="middle"
                                                        fontSize="8"
                                                        fill="white"
                                                        fontWeight="bold"
                                                    >
                                                        {percentage >= 10 ? `${Math.round(percentage)}%` : ''}
                                                    </text>
                                                )}
                                            </g>
                                        );
                                    })}
                                </svg>
                            )}
                        </div>
                        <div className="space-y-2 max-h-[350px] overflow-y-auto pr-2">
                            {sortedData.map(([label, value], index) => (
                                <div key={label} className="flex items-center group hover:bg-gray-50 p-1 rounded-md">
                                    <div
                                        className="w-3 h-3 mr-2 rounded-sm flex-shrink-0"
                                        style={{backgroundColor: colors[index % colors.length]}}
                                    ></div>
                                    <div className="text-sm flex justify-between w-full">
                                        <span className="truncate max-w-[180px]" title={label}>{label}</span>
                                        <span className="text-gray-500 ml-1 whitespace-nowrap">
                                            {value} ({getPercentage(value)}%)
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SurveyResultsChart;