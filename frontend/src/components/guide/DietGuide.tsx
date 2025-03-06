import React, { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/Tabs";
import { AlertTriangle, Clock, FileSpreadsheet, ListChecks } from "lucide-react";
import ExcelGuide from "./ExcelGuide";
import RulesGuide from "./RulesGuide";
import ScheduleGuide from "./ScheduleGuide";
import WarningSystemGuide from "./WarningSystemGuide";

const DietGuide: React.FC = () => {
    const [activeTab, setActiveTab] = useState('excel');
    const [activeCard, setActiveCard] = useState('');

    // Zarządzanie aktywną kartą w ramach aktywnej zakładki
    const toggleCard = (cardId: string) => {
        if (activeCard === cardId) {
            setActiveCard('');
        } else {
            setActiveCard(cardId);
        }
    };

    return (
        <Card className="w-full shadow-md border-slate-200">
            <CardHeader className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-t-lg border-b border-slate-200">
                <CardTitle className="text-2xl text-slate-800">
                    Przewodnik po dietach
                </CardTitle>
                <CardDescription className="text-slate-600">
                    Wszystko, co musisz wiedzieć o tworzeniu i zarządzaniu dietami
                </CardDescription>
            </CardHeader>
            <CardContent className="p-6">
                <Tabs value={activeTab} onValueChange={(val) => {
                    setActiveTab(val);
                    setActiveCard(''); // Reset active card when changing tabs
                }}>
                    <TabsList className="grid w-full grid-cols-4 p-1 rounded-xl bg-slate-100">
                        <TabsTrigger
                            value="excel"
                            className="flex items-center gap-2 data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm rounded-lg py-2.5"
                        >
                            <FileSpreadsheet className="w-4 h-4" />
                            Struktura Excel
                        </TabsTrigger>
                        <TabsTrigger
                            value="rules"
                            className="flex items-center gap-2 data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm rounded-lg py-2.5"
                        >
                            <ListChecks className="w-4 h-4" />
                            Zasady diet
                        </TabsTrigger>
                        <TabsTrigger
                            value="schedule"
                            className="flex items-center gap-2 data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm rounded-lg py-2.5"
                        >
                            <Clock className="w-4 h-4" />
                            Harmonogram
                        </TabsTrigger>
                        <TabsTrigger
                            value="warnings"
                            className="flex items-center gap-2 data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm rounded-lg py-2.5"
                        >
                            <AlertTriangle className="w-4 h-4" />
                            System ostrzeżeń
                        </TabsTrigger>
                    </TabsList>

                    {/* Zakładka Excel */}
                    <TabsContent value="excel" className="mt-6 space-y-4">
                        <ExcelGuide activeCard={activeCard} toggleCard={toggleCard} />
                    </TabsContent>

                    {/* Zakładka Zasady */}
                    <TabsContent value="rules" className="mt-6 space-y-6">
                        <RulesGuide activeCard={activeCard} toggleCard={toggleCard} />
                    </TabsContent>

                    {/* Zakładka Harmonogram */}
                    <TabsContent value="schedule" className="mt-6">
                        <ScheduleGuide activeCard={activeCard} toggleCard={toggleCard} />
                    </TabsContent>

                    {/* Zakładka Ostrzeżenia */}
                    <TabsContent value="warnings" className="mt-6">
                        <WarningSystemGuide />
                    </TabsContent>
                </Tabs>
            </CardContent>
        </Card>
    );
};

export default DietGuide;