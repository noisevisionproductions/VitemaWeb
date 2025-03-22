interface FooterLink {
    text: string;
    href: string;
}

interface FooterSection {
    title: string;
    links: FooterLink[];
}

export const footerLinks: FooterSection[] = [
    {
        title: "Produkt",
        links: [
            {text: "Funkcje", href: "/#features"},
            {text: "Dla kogo", href: "/#for-who"},
            {text: "FAQ", href: "/#faq"},
        ]
    },
    {
        title: "Firma",
        links: [
            {text: "O nas", href: "/about"},
            {text: "Kontakt", href: "/#contact"},
        ]
    },
    {
        title: "Inne",
        links: [
            {text: "Polityka prywatności", href: "/privacy-policy"}
        ]
    }
   /* {
        /!* //TODO czesc dla zalogowanych uzytkownikow, a czesc publiczna*!/
        title: "Pomoc",
        links: [
            {text: "Centrum pomocy", href: "/help"},
            {text: "Zgłoś problem", href: "/support"}
        ]
    }*/
];