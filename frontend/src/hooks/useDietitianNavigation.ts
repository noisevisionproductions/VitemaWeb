import {useNavigate, useLocation} from 'react-router-dom';
import {MainNav} from '../types/navigation';

// Mapowanie typów nawigacji na URL
const navToUrlMap: Record<MainNav, string> = {
    'dietitianDashboard': '',
    'dietCreation': 'diet-creation',
    'dietTemplates': 'diet-templates',
    'upload': 'upload',
    'diets': 'diets',
    'users': 'users',
    'stats': 'stats',
    'guide': 'guide',
    'changelog': 'changelog',
    'landing': '',
    'recipes': 'recipes'
};

// Mapowanie URL na typy nawigacji
const getNavFromPath = (pathname: string): MainNav => {
    // Usuń prefix /dashboard
    const path = pathname.replace('/dashboard/', '').replace('/dashboard', '');

    // Obsługuj zagnieżdżone ścieżki
    if (path.startsWith('diet-creation')) {
        return 'dietCreation';
    }

    // Bezpośrednie mapowania
    const directMapping: Record<string, MainNav> = {
        '': 'dietitianDashboard',
        'upload': 'upload',
        'diet-templates': 'dietTemplates',
        'diets': 'diets',
        'users': 'users',
        'stats': 'stats',
        'guide': 'guide',
        'changelog': 'changelog',
        'recipes': 'recipes'
    };

    return directMapping[path] || 'dietitianDashboard';
};

export const useDietitianNavigation = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // Pobierz aktualną zakładkę z URL
    const getCurrentTab = (): MainNav => {
        return getNavFromPath(location.pathname);
    };

    const isInTab = (tab: MainNav): boolean => {
        return getCurrentTab() === tab;
    };

    // Nawiguj do zakładki
    const navigateToTab = (tab: MainNav) => {
        const url = navToUrlMap[tab];
        if (url === '') {
            navigate('/dashboard');
        } else {
            navigate(`/dashboard/${url}`);
        }
    };

    const navigateToSubPath = (subPath: string) => {
        navigate(`/dashboard/${subPath}`);
    };

    // Pobierz URL dla zakładki
    const getTabUrl = (tab: MainNav): string => {
        const url = navToUrlMap[tab];
        return url === '' ? '/dashboard' : `/dashboard/${url}`;
    };

    // Sprawdź, czy jesteś w pod ścieżce (np. /dashboard/diet-creation/excel)
    const isInSubPath = (): boolean => {
        const path = location.pathname.replace('/dashboard/', '').replace('/dashboard', '');
        return path.includes('/');
    };

    // Pobierz aktualną pod ścieżkę (np. "excel" z "/dashboard/diet-creation/excel")
    const getCurrentSubPath = (): string | null => {
        const path = location.pathname.replace('/dashboard/', '').replace('/dashboard', '');
        const segments = path.split('/');
        return segments.length > 1 ? segments[1] : null;
    };

    // Nawiguj o poziom wyżej (np. z /dashboard/diet-creation/excel do /dashboard/diet-creation)
    const navigateUp = () => {
        const currentPath = location.pathname;
        const segments = currentPath.split('/').filter(Boolean);

        if (segments.length > 2) { // dashboard + co najmniej dwa segmenty
            const parentPath = '/' + segments.slice(0, -1).join('/');
            navigate(parentPath);
        } else {
            navigate('/dashboard');
        }
    };

    return {
        currentTab: getCurrentTab(),
        navigateToTab,
        navigateToSubPath,
        navigateUp,
        getTabUrl,
        isInTab,
        isInSubPath: isInSubPath(),
        currentSubPath: getCurrentSubPath(),
        currentPath: location.pathname
    };
};