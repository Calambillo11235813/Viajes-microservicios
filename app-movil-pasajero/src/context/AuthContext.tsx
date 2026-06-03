import React, { createContext, useState, useContext, ReactNode } from 'react';

export interface UsuarioInfo {
  idUsuario: number;
  nombreCompleto: string;
  email: string;
  idRol: number;
}

interface AuthContextType {
  user: UsuarioInfo | null;
  setUser: (user: UsuarioInfo | null) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<UsuarioInfo | null>(null);

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
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
