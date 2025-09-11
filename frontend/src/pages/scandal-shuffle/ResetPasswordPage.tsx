import React, {useEffect, useState} from 'react';
import {supabase} from "../../config/supabase";

export default function ResetPasswordPage() {
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    useEffect(() => {
        // This listener handles the PASSWORD_RECOVERY event.
        // Supabase automatically detects the access_token in the URL fragment
        // and verifies it. If successful, this event is fired.
        const {data: {subscription}} = supabase.auth.onAuthStateChange((event) => {
            if (event === 'PASSWORD_RECOVERY') {
                // This confirms the user is in the password recovery flow.
                // You don't need to manually handle the token.
                console.log('Password recovery mode initiated.');
            }
        });

        // Cleanup the subscription on component unmount
        return () => {
            subscription.unsubscribe();
        };
    }, []);

    const tryOpenApp = () => {
        // Try to open the mobile app with a custom URL scheme
        const appScheme = 'scandalshufflemobile://';

        // Create a hidden iframe to attempt opening the app
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = appScheme;
        document.body.appendChild(iframe);

        // Clean up the iframe after a short delay
        setTimeout(() => {
            document.body.removeChild(iframe);
        }, 1000);
    };

    const handleResetPassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        // Basic validation
        if (password.length < 6) {
            setError('Password must be at least 6 characters long.');
            return;
        }
        if (password !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        setLoading(true);
        try {
            // Update the user's password in Supabase
            const {error} = await supabase.auth.updateUser({password});

            if (error) {
                console.error('Password reset error:', error);
                setError(error.message || 'Failed to reset password. Please try again.');
                return;
            }

            setSuccess('Password successfully changed! You can now log in to the mobile app with your new password.');

        } catch (err: any) {
            console.error('Password reset error:', err);
            setError(err.message || 'Failed to reset password. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            maxWidth: '400px',
            margin: '50px auto',
            padding: '20px',
            border: '1px solid #ccc',
            borderRadius: '8px'
        }}>
            <h2>Set New Password</h2>
            <form onSubmit={handleResetPassword}>
                <div style={{marginBottom: '15px'}}>
                    <label>New Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        style={{width: '100%', padding: '8px', marginTop: '5px'}}
                    />
                </div>
                <div style={{marginBottom: '15px'}}>
                    <label>Confirm New Password</label>
                    <input
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                        style={{width: '100%', padding: '8px', marginTop: '5px'}}
                    />
                </div>

                {error && <p style={{color: 'red'}}>{error}</p>}
                {success && (
                    <div style={{marginBottom: '20px'}}>
                        <p style={{color: 'green', marginBottom: '15px'}}>{success}</p>
                        <button
                            type="button"
                            onClick={tryOpenApp}
                            style={{
                                width: '100%',
                                padding: '10px',
                                background: '#28a745',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                marginBottom: '10px'
                            }}
                        >
                            Open Mobile App
                        </button>
                        <p style={{fontSize: '12px', color: '#666', textAlign: 'center'}}>
                            If the app doesn't open automatically, please open it manually from your device.
                        </p>
                    </div>
                )}

                {!success && (
                    <button type="submit" disabled={loading} style={{
                        width: '100%',
                        padding: '10px',
                        background: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px'
                    }}>
                        {loading ? 'Saving...' : 'Save New Password'}
                    </button>
                )}
            </form>
        </div>
    );
}