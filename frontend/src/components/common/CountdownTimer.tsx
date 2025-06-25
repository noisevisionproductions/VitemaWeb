import React, {useState, useEffect} from 'react';

interface CountdownTimerProps {
    targetDate: string;
    className?: string;
}

const CountdownTimer: React.FC<CountdownTimerProps> = ({targetDate, className = ''}) => {
    const [timeLeft, setTimeLeft] = useState({
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0
    });

    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const target = new Date(targetDate);

        // Funkcja obliczająca pozostały czas
        const calculateTimeLeft = () => {
            const difference = target.getTime() - new Date().getTime();

            if (difference > 0) {
                setTimeLeft({
                    days: Math.floor(difference / (1000 * 60 * 60 * 24)),
                    hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
                    minutes: Math.floor((difference / 1000 / 60) % 60),
                    seconds: Math.floor((difference / 1000) % 60)
                });
            } else {
                setTimeLeft({days: 0, hours: 0, minutes: 0, seconds: 0});
            }

            if (isLoading) {
                setIsLoading(false);
            }
        };

        // Inicjalne obliczenie
        calculateTimeLeft();

        // Aktualizacja co sekundę
        const timer = setInterval(() => {
            calculateTimeLeft();
        }, 1000);

        // Czyszczenie interwału przy odmontowaniu komponentu
        return () => clearInterval(timer);
    }, [targetDate, isLoading]);

    // Formatowanie liczby do zawsze dwucyfrowej postaci
    const formatNumber = (num: number) => {
        return num < 10 ? `0${num}` : num;
    };

    if (isLoading) {
        return <div className={className}>Ładowanie...</div>;
    }

    return (
        <div className={`flex space-x-4 justify-center ${className}`}>
            <div className="text-center">
                <div className="text-3xl font-bold">{timeLeft.days}</div>
                <p className="text-sm opacity-80">Dni</p>
            </div>
            <div className="text-center">
                <div className="text-3xl font-bold">{formatNumber(timeLeft.hours)}</div>
                <p className="text-sm opacity-80">Godzin</p>
            </div>
            <div className="text-center">
                <div className="text-3xl font-bold">{formatNumber(timeLeft.minutes)}</div>
                <p className="text-sm opacity-80">Minut</p>
            </div>
            <div className="text-center">
                <div className="text-3xl font-bold">{formatNumber(timeLeft.seconds)}</div>
                <p className="text-sm opacity-80">Sekund</p>
            </div>
        </div>
    );
};

export default CountdownTimer;