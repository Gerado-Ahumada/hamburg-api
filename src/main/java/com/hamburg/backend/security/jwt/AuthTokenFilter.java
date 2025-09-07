package com.hamburg.backend.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hamburg.backend.service.UsuarioService;
import com.hamburg.backend.repository.SesionRepository;
import com.hamburg.backend.model.Sesion;

import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private SesionRepository sesionRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        logger.info("=== AuthTokenFilter ejecutándose ===");
        logger.info("Request URI: " + request.getRequestURI());
        logger.info("Request Method: " + request.getMethod());
        
        try {
            // Saltear el filtro JWT para el endpoint de login
            if ("/api/auth/login".equals(request.getRequestURI())) {
                logger.info("Saltando filtro JWT para login");
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = parseJwt(request);
            logger.info("JWT Token extraído: {}", jwt != null ? "Token presente" : "Token ausente");
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt) && validarSesionToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("Usuario extraído del token: {}", username);

                UserDetails userDetails = usuarioService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Autenticación establecida para usuario: {}", username);
            } else {
                logger.info("Token JWT inválido, sesión inválida o ausente para la solicitud: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("No se puede establecer la autenticación del usuario: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
    
    private boolean validarSesionToken(String token) {
        logger.info("Validando sesión token");
        Optional<Sesion> sesionOpt = sesionRepository.findByTokenAndActivaTrue(token);
        logger.info("Sesión encontrada: {}", sesionOpt.isPresent());
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            boolean expirada = sesion.isExpirada();
            logger.info("Sesión expirada: {}", expirada);
            logger.info("Fecha expiración: {}", sesion.getFechaExpiracion());
            logger.info("Fecha actual: {}", java.time.LocalDateTime.now());
            return !expirada;
        }
        return false;
    }
}