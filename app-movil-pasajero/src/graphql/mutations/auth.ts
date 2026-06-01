import { gql } from '@apollo/client';

export const LOGIN_MUTATION = gql`
  mutation Login($email: String!, $passwordHash: String!) {
    login(email: $email, passwordHash: $passwordHash) {
      token
      usuario {
        idUsuario
        nombreCompleto
        email
        idRol
      }
    }
  }
`;