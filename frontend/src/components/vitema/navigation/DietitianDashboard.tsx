import React from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {BookOpenIcon, ChartBarIcon} from '@heroicons/react/24/outline';
import {useAuth} from "../../../contexts/AuthContext";
import {BookAudio, ClipboardList, ClipboardListIcon, HelpCircle, PlusCircle, Users} from "lucide-react";
import {toast} from "../../../utils/toast";
import SectionHeader from "../../shared/common/SectionHeader";

const DietitianDashboard: React.FC = () => {
    const {currentUser, logout} = useAuth();
    const navigate = useNavigate();

    /*   const [theme, setTheme] = useState(
           localStorage.getItem('theme') || 'light'
       );*/

    /*  useEffect(() => {
          const root = document.documentElement;
          if (theme === 'dark') {
              root.classList.add('dark');
          } else {
              root.classList.remove('dark');
          }
          localStorage.setItem('theme', theme);
      }, [theme]);*/

    /*const toggleTheme = () => {
        setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
    };*/

    const dietitianModules = [
        {
            name: 'Tworzenie diety',
            description: 'Utwórz nową dietę dla klienta - wybierz między importem z Excel a tworzeniem ręcznym.',
            icon: PlusCircle,
            tabName: 'dietCreation'
        },
        {
            name: 'Gotowe szablony diet',
            description: 'Przeglądaj stworzone wcześniej szablony całych diet.',
            icon: BookAudio,
            tabName: 'dietTemplates'
        },
        {
            name: 'Zarządzanie dietami',
            description: 'Przeglądaj i edytuj diety przypisane do klientów.',
            icon: ClipboardListIcon,
            tabName: 'diets'
        },
        {
            name: 'Przepisy kulinarne',
            description: 'Zarządzaj bazą przepisów kulinarnych.',
            icon: BookOpenIcon,
            tabName: 'recipes'
        },
        {
            name: 'Klienci',
            description: 'Zarządzaj kontami klientów i ich dietami.',
            icon: Users,
            tabName: 'users'
        },
        {
            name: 'Statystyki',
            description: 'Zobacz statystyki korzystania z diet i postępów klientów.',
            icon: ChartBarIcon,
            tabName: 'stats'
        },
        {
            name: 'Przewodnik',
            description: 'Instrukcje i porady dotyczące korzystania z systemu.',
            icon: HelpCircle,
            tabName: 'guide'
        },
        {
            name: 'Historia zmian',
            description: 'Śledź najnowsze zmiany w panelu.',
            icon: ClipboardList,
            tabName: 'changelog'
        }
    ];

    const handleModuleClick = (tabName: string) => {
        window.dispatchEvent(new CustomEvent('panel-tab-change', {detail: tabName}));
    };

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/');
            toast.success('Wylogowano pomyślnie');
        } catch (error) {
            toast.error('Wystąpił błąd podczas wylogowywania');
        }
    };

    return (
        <div className="space-y-6 pb-8">
            <SectionHeader
                title="Panel Trenera"
                description="Zarządzaj dietami, przepisami i klientami."
                welcomeUser={currentUser}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {dietitianModules.map((module) => {
                    const Icon = module.icon;

                    return (
                        <div
                            key={module.name}
                            // Updated classes for Dark Mode support (dark:bg-gray-800, dark:border-gray-700, etc.)
                            className="relative bg-white dark:bg-gray-800 p-6 border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden hover:shadow-md transition duration-200 cursor-pointer hover:border-primary-light group"
                            onClick={() => handleModuleClick(module.tabName)}
                        >
                            <div className="flex items-center mb-4">
                                <div
                                    className="p-2 rounded-md bg-primary-light/10 group-hover:bg-primary-light/20 transition-colors">
                                    <Icon className="h-6 w-6 text-primary dark:text-primary-light" aria-hidden="true"/>
                                </div>
                                {/* Added dark:text-white */}
                                <h3 className="ml-3 text-lg font-medium text-gray-900 dark:text-white group-hover:text-primary dark:group-hover:text-primary-light">
                                    {module.name}
                                </h3>
                            </div>
                            {/* Added dark:text-gray-400 */}
                            <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
                                {module.description}
                            </p>
                            <span
                                className="text-sm font-medium text-primary hover:text-primary-dark dark:text-primary-light group-hover:underline">
                                Przejdź &rarr;
                            </span>
                        </div>
                    );
                })}
            </div>

            {/* Updated container background and border for Dark Mode */}
            <div
                className="mt-8 bg-gray-50 dark:bg-gray-800 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-3">
                    Szybki dostęp
                </h3>
                <div className="flex flex-wrap gap-4">
                    {/* Changed space-x-4 to flex-wrap gap-4 for better mobile handling */}

                    {useAuth().isAdmin() && (
                        <Link
                            to="/admin"
                            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-secondary group-hover:bg-secondary/10"
                        >
                            Panel administratora
                        </Link>
                    )}

                    {/* Dark Mode Toggle Button */}
                    {/*     <button
                        onClick={toggleTheme}
                        className="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
                    >
                        {theme === 'light' ? (
                            <>
                                <Moon className="mr-2 h-4 w-4"/> Tryb ciemny
                            </>
                        ) : (
                            <>
                                <Sun className="mr-2 h-4 w-4"/> Tryb jasny
                            </>
                        )}
                    </button>
*/}
                    <Link
                        to="/frontend/public"
                        className="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
                    >
                        Strona główna
                    </Link>
                    <button
                        onClick={handleLogout}
                        className="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-red-800 shadow-sm text-sm font-medium rounded-md text-white bg-red-500 hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700 transition-colors"
                    >
                        Wyloguj się
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DietitianDashboard;