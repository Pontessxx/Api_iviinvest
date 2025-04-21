package com.Iviinvest.repository;

import com.Iviinvest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório para operações de banco de dados relacionadas à entidade Usuário.
 * <p>
 * Estende JpaRepository para herdar operações CRUD básicas e permite
 * operações customizadas relacionadas a usuários.
 * <p>
 * Repository for database operations related to the User entity.
 * Extends JpaRepository to inherit basic CRUD operations and enables
 * custom user-related operations.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo endereço de e-mail.
     * <p>
     * Retorna um Optional contendo o usuário se encontrado,
     * ou Optional.empty() caso contrário.
     * <p>
     * Finds a user by email address.
     * Returns an Optional containing the user if found,
     * or Optional.empty() otherwise.
     *
     * @param email Endereço de e-mail do usuário | User's email address
     * @return Optional contendo o usuário ou vazio | Optional containing user or empty
     */
    Optional<Usuario> findByEmail(String email);
}