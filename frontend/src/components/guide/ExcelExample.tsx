import React from "react";

const ExcelExample: React.FC = () => (
    <div className="bg-white shadow-sm border border-slate-200 rounded-lg overflow-x-auto">
        <table className="min-w-full">
            <thead>
            <tr>
                <th className="px-4 py-2 bg-slate-100 border-b">Kolumna A</th>
                <th className="px-4 py-2 bg-slate-100 border-b">Kolumna B</th>
                <th className="px-4 py-2 bg-slate-100 border-b">Kolumna C</th>
                <th className="px-4 py-2 bg-slate-100 border-b">Kolumna D</th>
                <th className="px-4 py-2 bg-slate-100 border-b">Kolumna E</th>
            </tr>
            </thead>
            <tbody>
            <tr className="border-b">
                <td className="px-4 py-2 text-gray-500">Notatki (pomijane)</td>
                <td className="px-4 py-2 font-medium">Nazwa posiłku</td>
                <td className="px-4 py-2 font-medium">Sposób przygotowania</td>
                <td className="px-4 py-2 font-medium">Lista składników</td>
                <td className="px-4 py-2 font-medium">Wartości odżywcze</td>
            </tr>
            <tr>
                <td className="px-4 py-2 text-gray-500">Dowolne notatki</td>
                <td className="px-4 py-2">Owsianka z jabłkiem</td>
                <td className="px-4 py-2">Ugotuj płatki owsiane na mleku...</td>
                <td className="px-4 py-2">płatki owsiane, mleko, jabłko...</td>
                <td className="px-4 py-2">300,15,5,45</td>
            </tr>
            </tbody>
        </table>
    </div>
);

export default ExcelExample;