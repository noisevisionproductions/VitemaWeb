const Logo = ({asLink = true, variant = 'full'}) => {
    const logoImage = (
        <img
            src="/images/logo-n.png"
            alt="Vitema Logo"
            className="h-8 w-auto"
        />
    );

    const logoText = (
        <span className="text-2xl font-bold text-primary">
            Vitema
        </span>
    );

    // Wybór zawartości w zależności od wybranego wariantu
    const renderContent = () => {
        switch (variant) {
            case 'logoOnly':
                return logoImage;
            case 'textOnly':
                return logoText;
            case 'full':
            default:
                return (
                    <>
                        {logoImage}
                        {logoText}
                    </>
                );
        }
    };

    // Wrapper jako link lub div
    if (asLink) {
        return (
            <a href="/frontend/public" className="flex items-center gap-2">
                {renderContent()}
            </a>
        );
    }

    return (
        <div className="flex items-center gap-2">
            {renderContent()}
        </div>
    );
};

export default Logo;