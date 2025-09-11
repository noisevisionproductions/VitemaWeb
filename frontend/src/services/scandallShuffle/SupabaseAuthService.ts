import {supabase} from '../../config/supabase';
import {Session} from '@supabase/supabase-js';

export interface SupabaseUser {
    id: string;
    email: string;
    role: string;
    displayName?: string;
}

export class SupabaseAuthService {

    /**
     * Authenticate user with email and password
     */
    static async login(email: string, password: string): Promise<SupabaseUser> {
        const {data, error} = await supabase.auth.signInWithPassword({
            email,
            password
        });

        if (error || !data.user) {
            throw new Error(error?.message || 'Authentication failed');
        }

        // Get or create user profile
        const profile = await this.getOrCreateProfile(data.user.id, data.user.email!);

        return {
            id: data.user.id,
            email: data.user.email!,
            role: profile.role,
            displayName: profile.display_name || data.user.email
        };
    }

    /**
     * Sign out current user
     */
    static async logout(): Promise<void> {
        const {error} = await supabase.auth.signOut();
        if (error) {
            throw new Error(error.message);
        }
    }

    /**
     * Get current authenticated user
     */
    static async getCurrentUser(): Promise<SupabaseUser | null> {
        const {data: {session}, error} = await supabase.auth.getSession();

        if (error || !session?.user) {
            return null;
        }

        const user = session.user;

        try {
            const {data: profile} = await supabase
                .from('profiles')
                .select('role, display_name')
                .eq('user_id', user.id)
                .single();

            return {
                id: user.id,
                email: user.email!,
                role: profile?.role || 'user',
                displayName: profile?.display_name || user.email
            };
        } catch {
            // Fallback without profile data
            return {
                id: user.id,
                email: user.email!,
                role: 'user',
                displayName: user.email
            };
        }
    }

    /**
     * Listen for auth state changes
     */
    static onAuthStateChange(callback: (user: SupabaseUser | null, session: Session | null) => void) {
        return supabase.auth.onAuthStateChange(async (_event, session) => {
            if (session?.user) {
                try {
                    const user = await this.getCurrentUser();
                    callback(user, session);
                } catch (error) {
                    console.error('Error getting current user:', error);
                    callback(null, null);
                }
            } else {
                callback(null, null);
            }
        });
    }

    static async getSession() {
        return supabase.auth.getSession();
    }

    /**
     * Initialize user session on app start
     */
    static async initializeSession(): Promise<{ user: SupabaseUser, session: Session } | null> {
        try {
            const {data: {session}} = await supabase.auth.getSession();
            if (session?.user) {
                const user = await this.getCurrentUser();
                if (user) {
                    return { user, session };
                }
            }
            return null;
        } catch (error) {
            console.error('Error initializing session:', error);
            return null;
        }
    }

    /**
     * Get or create user profile in profiles table
     */
    private static async getOrCreateProfile(userId: string, email: string) {
        const {data: profile, error} = await supabase
            .from('profiles')
            .select('role, display_name')
            .eq('user_id', userId)
            .single();

        if (error?.code === 'PGRST116') {
            // Profile doesn't exist, create new one
            const {data: newProfile, error: insertError} = await supabase
                .from('profiles')
                .insert({
                    user_id: userId,
                    display_name: email.split('@')[0],
                    role: 'user'
                })
                .select('role, display_name')
                .single();

            if (insertError) {
                throw new Error('Failed to create user profile: ' + insertError.message);
            }

            return newProfile;
        }

        if (error) {
            throw new Error('Failed to fetch user profile: ' + error.message);
        }

        return profile;
    }
}