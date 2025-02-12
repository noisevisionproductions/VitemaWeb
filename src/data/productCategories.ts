import {Category} from '../types/product-categories';

export const DEFAULT_CATEGORIES: Category[] = [
    {
        id: 'dairy',
        name: 'Nabiał',
        color: '#AED6F1',
        icon: 'Milk',
        order: 1
    },
    {
        id: 'meat-fish',
        name: 'Ryby i Mięso',
        color: '#F5B7B1',
        icon: 'Fish',
        order: 2
    },
    {
        id: 'vegetables',
        name: 'Warzywa',
        color: '#A9DFBF',
        icon: 'Carrot',
        order: 3
    },
    {
        id: 'fruits',
        name: 'Owoce',
        color: '#F9E79F',
        icon: 'Apple',
        order: 4
    },
    {
        id: 'dry-goods',
        name: 'Produkty suche',
        color: '#F5CBA7',
        icon: 'Wheat',
        order: 5
    },
    {
        id: 'spices',
        name: 'Przyprawy',
        color: '#E8DAEF',
        icon: 'Soup',
        order: 6
    },
    {
        id: 'oils',
        name: 'Oleje i tłuszcze',
        color: '#FAD7A0',
        icon: 'Droplet',
        order: 7
    },
    {
        id: 'nuts',
        name: 'Orzechy i nasiona',
        color: '#D5D8DC',
        icon: 'Nut',
        order: 8
    },
    {
        id: 'beverages',
        name: 'Napoje',
        color: '#A3E4D7',
        icon: 'Beer',
        order: 9
    },
    {
        id: 'canned',
        name: 'Produkty konserwowe',
        color: '#D7BDE2',
        icon: 'Box',
        order: 10
    },
    {
        id: 'frozen',
        name: 'Mrożonki',
        color: '#85C1E9',
        icon: 'Snowflake',
        order: 11
    },
    {
        id: 'snacks',
        name: 'Przekąski',
        color: '#F8C471',
        icon: 'Cookie',
        order: 12
    },
    {
        id: 'other',
        name: 'Inne',
        color: '#CCD1D1',
        icon: 'Package',
        order: 13
    }
];

export const getCategoryById = (id: string): Category | undefined => {
    return DEFAULT_CATEGORIES.find(category => category.id === id);
};

export const getCategoryByName = (name: string): Category | undefined => {
    return DEFAULT_CATEGORIES.find(category =>
        category.name.toLowerCase() === name.toLowerCase()
    );
};