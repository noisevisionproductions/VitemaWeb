import {useEffect, useState} from 'react';

export default function EmailVerifiedPage() {
    const [countdown, setCountdown] = useState(5);

    // This function attempts to open the mobile application using its custom URL scheme.
    // It creates a temporary, hidden iframe which is a common technique to trigger the app link.
    const tryOpenApp = () => {
        window.location.href = 'scandalshufflemobile://';
    };

    // On component mount, we start a countdown. When it reaches zero,
    // we attempt to automatically redirect the user to the mobile app.
    useEffect(() => {
        if (countdown <= 0) {
            tryOpenApp();
            return;
        }

        const timer = setTimeout(() => {
            setCountdown(countdown - 1);
        }, 1000);

        return () => clearTimeout(timer);
    }, [countdown]);

    return (
        <div style={{
            fontFamily: 'Arial, sans-serif',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            backgroundColor: '#f4f4f9'
        }}>
            <div style={{
                maxWidth: '420px',
                textAlign: 'center',
                padding: '40px',
                border: '1px solid #e0e0e0',
                borderRadius: '12px',
                backgroundColor: 'white',
                boxShadow: '0 4px 12px rgba(0,0,0,0.08)'
            }}>
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="64"
                    height="64"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#28a745"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    style={{marginBottom: '20px'}}
                >
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>

                <h1 style={{fontSize: '24px', color: '#333', marginBottom: '10px'}}>
                    Email Verified Successfully!
                </h1>
                <p style={{color: '#666', marginBottom: '25px', lineHeight: '1.5'}}>
                    Your account is now active. You can close this page and return to the application.
                </p>

                <button
                    type="button"
                    onClick={tryOpenApp}
                    style={{
                        width: '100%',
                        padding: '12px',
                        background: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        fontSize: '16px',
                        cursor: 'pointer',
                        marginBottom: '15px'
                    }}
                >
                    Open Mobile App
                </button>
                <p style={{fontSize: '12px', color: '#777'}}>
                    If the app doesn't open, please launch it manually.
                    Redirecting automatically in {countdown}...
                </p>
            </div>
        </div>
    );
}