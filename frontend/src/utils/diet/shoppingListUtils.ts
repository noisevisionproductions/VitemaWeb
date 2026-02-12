export function formatCategoryName(key: string): string {
    const map: Record<string, string> = {
        'warzywa_i_owoce': 'Warzywa i Owoce',
        'nabial_i_jaja': 'Nabiał i Jaja',
        'mieso_i_ryby': 'Mięso i Ryby',
        'zbozowe': 'Produkty Zbożowe',
        'tluszcze': 'Tłuszcze i Orzechy',
        'przyprawy': 'Przyprawy i Dodatki',
        'napoje': 'Napoje',
        'inne': 'Inne',
        'uncategorized': 'Nieskategoryzowane'
    };
    return map[key] || key.replace(/_/g, ' ');
}

export function getCategoryColor(key: string): string {
    const map: Record<string, string> = {
        'warzywa_i_owoce': 'bg-green-500',
        'mieso_i_ryby': 'bg-red-500',
        'nabial_i_jaja': 'bg-yellow-400',
        'zbozowe': 'bg-amber-600',
        'tluszcze': 'bg-yellow-600',
        'inne': 'bg-gray-400'
    };
    return map[key] || 'bg-primary';
}