import React from "react";
import {ChangelogEntry} from "../../../types/changeLog";
import {formatTimestamp} from "../../../utils/dateFormatters";
import TypeBadge from "./TypeBadge";

interface ChangelogListProps {
    entries: ChangelogEntry[];
}

const ChangelogList: React.FC<ChangelogListProps> = ({ entries }) => {
    return (
        <div className="space-y-6">
            {entries.map((entry) => (
                <div key={entry.id} className="bg-white p-4 rounded-lg shadow-sm">
                    <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-2">
                        <div>
                            <h3 className="text-lg font-medium">{entry.title}</h3>
                            <div className="mt-1.5">
                                <TypeBadge type={entry.type} />
                            </div>
                        </div>
                        <div className="text-sm text-right text-gray-500 whitespace-nowrap">
                            {formatTimestamp(entry.createdAt)}
                        </div>
                    </div>

                    <p className="mt-3 text-gray-600 whitespace-pre-line">
                        {entry.description}
                    </p>

                    <p className="mt-3 text-sm text-gray-500">
                        Dodał(a): {entry.author}
                    </p>
                </div>
            ))}
        </div>
    );
};

export default ChangelogList;