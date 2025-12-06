import React, {useState} from "react";
import {CardContent} from "../../../../../shared/ui/Card";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "../../../../../shared/ui/Tabs";
import {AlertTriangle, Clock, FileSpreadsheet, ListChecks} from "lucide-react";
import ExcelGuide from "./ExcelGuide";
import RulesGuide from "../../../../guide/RulesGuide";
import ScheduleGuide from "../../../../guide/ScheduleGuide";
import WarningSystemGuide from "../../../../guide/WarningSystemGuide";
import SectionHeader from "../../../../../shared/common/SectionHeader";

const DietGuide: React.FC = () => {
    const [activeTab, setActiveTab] = useState('excel');

    return (
        <div className="space-y-6 pb-8">
            <SectionHeader title="Przewodnik"
                           description="Wszystko co musisz wiedzieć o dostępnych funkcjach panelu"
            />
            <CardContent className="p-3 sm:p-6 bg-white">
                <Tabs value={activeTab} onValueChange={(val) => setActiveTab(val)}>
                    <TabsList
                        className="grid grid-cols-2 sm:grid-cols-4 w-full p-1 rounded-xl bg-slate-300">
                        <TabsTrigger
                            value="excel"
                            className="flex items-center justify-center gap-1 sm:gap-2 rounded-lg py-2 px-2 sm:px-4 text-xs sm:text-sm m-1
                            data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm
                            data-[state=inactive]:bg-slate-100 data-[state=inactive]:text-slate-600 data-[state=inactive]:hover:bg-slate-200 transition-all"
                        >
                            <FileSpreadsheet className="w-4 h-4"/>
                            <span className="whitespace-nowrap">Struktura Excel</span>
                        </TabsTrigger>
                        <TabsTrigger
                            value="rules"
                            className="flex items-center justify-center gap-1 sm:gap-2 rounded-lg py-2 px-2 sm:px-4 text-xs sm:text-sm m-1
                            data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm
                            data-[state=inactive]:bg-slate-100 data-[state=inactive]:text-slate-600 data-[state=inactive]:hover:bg-slate-200 transition-all"
                        >
                            <ListChecks className="w-4 h-4"/>
                            <span className="whitespace-nowrap">Zasady diet</span>
                        </TabsTrigger>
                        <TabsTrigger
                            value="schedule"
                            className="flex items-center justify-center gap-1 sm:gap-2 rounded-lg py-2 px-2 sm:px-4 text-xs sm:text-sm m-1
                            data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm
                            data-[state=inactive]:bg-slate-100 data-[state=inactive]:text-slate-600 data-[state=inactive]:hover:bg-slate-200 transition-all"
                        >
                            <Clock className="w-4 h-4"/>
                            <span className="whitespace-nowrap">Harmonogram</span>
                        </TabsTrigger>
                        <TabsTrigger
                            value="warnings"
                            className="flex items-center justify-center gap-1 sm:gap-2 rounded-lg py-2 px-2 sm:px-4 text-xs sm:text-sm m-1
                            data-[state=active]:bg-white data-[state=active]:text-blue-700 data-[state=active]:shadow-sm
                            data-[state=inactive]:bg-slate-100 data-[state=inactive]:text-slate-600 data-[state=inactive]:hover:bg-slate-200 transition-all"
                        >
                            <AlertTriangle className="w-4 h-4"/>
                            <span className="whitespace-nowrap">System ostrzeżeń</span>
                        </TabsTrigger>
                    </TabsList>

                    <div className="mt-6">
                        <TabsContent value="excel" className="m-0">
                            <ExcelGuide/>
                        </TabsContent>

                        <TabsContent value="rules" className="m-0">
                            <RulesGuide/>
                        </TabsContent>

                        <TabsContent value="schedule" className="m-0">
                            <ScheduleGuide/>
                        </TabsContent>

                        <TabsContent value="warnings" className="m-0">
                            <WarningSystemGuide/>
                        </TabsContent>
                    </div>
                </Tabs>
            </CardContent>
        </div>
    );
};

export default DietGuide;