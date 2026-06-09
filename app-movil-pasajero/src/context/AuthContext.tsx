import React, { createContext, useState, useContext, ReactNode } from 'react';
import { setAuthToken } from '@/utils/authToken';

export interface UsuarioInfo {
  idUsuario: number;
  nombreCompleto: string;
  email: string;
  idRol: number;
}

interface AuthContextType {
  user: UsuarioInfo | null;
  token: string | null;
  setUser: (user: UsuarioInfo | null) => void;
  setSession: (user: UsuarioInfo, token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<UsuarioInfo | null>(null);
  const [token, setToken] = useState<string | null>(null);

  const setSession = (nextUser: UsuarioInfo, nextToken: string) => {
    setUser(nextUser);
    setToken(nextToken);
    setAuthToken(nextToken);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    setAuthToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, setUser, setSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
