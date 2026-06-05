export interface UsuarioPerfil {
    idUsuario: number;  // Mapeado desde el schema: UsuarioPerfil.idUsuario: ID!
    ciPasaporte: string;
    nombreCompleto: string;
    email: string;
    telefono?: string;
    idRol: number;
}

export interface AuthResponse {
    token: string;
    perfil: UsuarioPerfil;
}

export interface LoginCredentials {
    email: string;
    password: string;
}
