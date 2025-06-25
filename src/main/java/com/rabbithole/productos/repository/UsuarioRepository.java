package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su email
     * @param email email del usuario
     * @return usuario encontrado o empty si no existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email email del usuario
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
