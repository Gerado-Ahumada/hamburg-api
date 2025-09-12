package com.hamburg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.PlayerResponse;
import com.hamburg.backend.dto.PlayersPageResponse;
import com.hamburg.backend.model.ERole;
import com.hamburg.backend.model.EStatus;
import com.hamburg.backend.model.Status;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.StatusRepository;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.security.UserDetailsImpl;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return UserDetailsImpl.build(user);
    }
    
    public PlayersPageResponse getPlayers(int page, int size, String name, String category) {
        // Validar parámetros de paginación
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        
        // Crear objeto Pageable con ordenamiento por firstName
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        
        Page<User> usersPage;
        
        // Determinar qué método del repositorio usar según los filtros
        boolean hasName = StringUtils.hasText(name);
        boolean hasCategory = StringUtils.hasText(category);
        
        if (hasName && hasCategory) {
            usersPage = userRepository.findByRoleAndNameContainingAndPlayerCategory(
                ERole.ROLE_PLAYER, name.trim(), category.trim(), pageable);
        } else if (hasName) {
            usersPage = userRepository.findByRoleAndNameContaining(
                ERole.ROLE_PLAYER, name.trim(), pageable);
        } else if (hasCategory) {
            usersPage = userRepository.findByRoleAndPlayerCategory(
                ERole.ROLE_PLAYER, category.trim(), pageable);
        } else {
            usersPage = userRepository.findByRole(ERole.ROLE_PLAYER, pageable);
        }
        
        // Convertir usuarios a PlayerResponse
        List<PlayerResponse> players = usersPage.getContent().stream()
            .map(this::convertToPlayerResponse)
            .collect(Collectors.toList());
        
        // Crear respuesta paginada
        return new PlayersPageResponse(
            players,
            usersPage.getNumber(),
            usersPage.getTotalPages(),
            usersPage.getTotalElements(),
            usersPage.getSize(),
            usersPage.hasNext(),
            usersPage.hasPrevious()
        );
    }
    
    private PlayerResponse convertToPlayerResponse(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        String status = user.getStatus() != null ? user.getStatus().getName().name() : "UNKNOWN";
        
        return new PlayerResponse(
            user.getUsername(),
            fullName,
            user.getEmail(),
            user.getPhone(),
            user.getPlayerCategory(),
            status,
            user.getUuid()
        );
    }
    
    public String editPlayer(String uuid, EditPlayerRequest editRequest) {
        // Buscar el usuario por UUID
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con UUID: " + uuid));
        
        // Verificar que el usuario tenga rol de jugador
        boolean isPlayer = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_PLAYER);
        
        if (!isPlayer) {
            throw new RuntimeException("El usuario especificado no es un jugador");
        }
        
        // Actualizar campos si están presentes en la request
        if (StringUtils.hasText(editRequest.getFirstName())) {
            user.setFirstName(editRequest.getFirstName().trim());
        }
        
        if (StringUtils.hasText(editRequest.getLastName())) {
            user.setLastName(editRequest.getLastName().trim());
        }
        
        if (StringUtils.hasText(editRequest.getPassword())) {
            user.setPassword(passwordEncoder.encode(editRequest.getPassword()));
        }
        
        if (StringUtils.hasText(editRequest.getPlayerCategory())) {
            user.setPlayerCategory(editRequest.getPlayerCategory().trim());
        }
        
        // Actualizar status si está presente
        if (StringUtils.hasText(editRequest.getStatus())) {
            try {
                EStatus statusEnum = EStatus.valueOf(editRequest.getStatus().toUpperCase());
                Status status = statusRepository.findByName(statusEnum)
                        .orElseThrow(() -> new RuntimeException("Status no encontrado: " + editRequest.getStatus()));
                user.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Status inválido: " + editRequest.getStatus() + ". Valores permitidos: ACTIVE, INACTIVE");
            }
        }
        
        // Guardar los cambios
        userRepository.save(user);
        
        return "Jugador actualizado exitosamente";
    }
}