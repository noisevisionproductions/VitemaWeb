interface FooterLink {
    textKey: string;
    href: string;
}

interface FooterSection {
    titleKey: string;
    links: FooterLink[];
}

export const footerLinks: FooterSection[] = [
    {
        titleKey: "product",
        links: [
            {textKey: "features", href: "/#features"},
            {textKey: "forWho", href: "/#for-who"},
            {textKey: "faq", href: "/#faq"},
        ]
    },
    {
        titleKey: "company",
        links: [
            {textKey: "about", href: "/about"},
            {textKey: "contact", href: "/#contact"},
        ]
    },
    {
        titleKey: "other",
        links: [
            {textKey: "privacy", href: "/privacy-policy"}
        ]
    }
];