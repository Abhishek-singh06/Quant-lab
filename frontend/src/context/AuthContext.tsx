import React, { createContext, useContext, useState, useEffect } from 'react';

export interface User {
  id: string;
  name: string;
  email: string;
  created_at: string;
  status: string;
}

export interface InvestmentPreferences {
  risk_tolerance: string;
  capital_available: number;
  preferred_horizons: string[];
  max_single_stock_alloc_pct: number;
  max_sector_alloc_pct: number;
  preferred_asset_classes: string[];
  investment_objective: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  preferences: InvestmentPreferences | null;
  login: (token: string, user: User) => void;
  signup: (token: string, user: User) => void;
  logout: () => void;
  updatePreferences: (prefs: InvestmentPreferences) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('ql_auth_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [preferences, setPreferences] = useState<InvestmentPreferences | null>(null);

  useEffect(() => {
    async function loadUser() {
      const storedToken = localStorage.getItem('ql_auth_token');
      if (!storedToken) {
        setIsLoading(false);
        return;
      }
      try {
        const res = await fetch('/api/v1/auth/me', {
          headers: { Authorization: `Bearer ${storedToken}` }
        });
        if (res.ok) {
          const userData = await res.json();
          setUser(userData);
          setToken(storedToken);
          
          // Load preferences
          const prefRes = await fetch('/api/v1/settings/investment-profile', {
            headers: { Authorization: `Bearer ${storedToken}` }
          });
          if (prefRes.ok) {
            const prefData = await prefRes.json();
            setPreferences(prefData);
          }
        } else {
          localStorage.removeItem('ql_auth_token');
          setUser(null);
          setToken(null);
        }
      } catch {
        // Safe fallback
      } finally {
        setIsLoading(false);
      }
    }

    loadUser();
  }, []);

  const login = (newToken: string, newUser: User) => {
    localStorage.setItem('ql_auth_token', newToken);
    setToken(newToken);
    setUser(newUser);
  };

  const signup = (newToken: string, newUser: User) => {
    localStorage.setItem('ql_auth_token', newToken);
    setToken(newToken);
    setUser(newUser);
  };

  const logout = () => {
    localStorage.removeItem('ql_auth_token');
    setToken(null);
    setUser(null);
    setPreferences(null);
  };

  const updatePreferences = (newPrefs: InvestmentPreferences) => {
    setPreferences(newPrefs);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user,
        isLoading,
        preferences,
        login,
        signup,
        logout,
        updatePreferences,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
