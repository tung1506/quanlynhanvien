package com.project.employee.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.project.employee.exception.ApiException;
import com.project.employee.model.User;
import com.project.employee.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            log.info("Processing request to: {}", request.getRequestURI());

            // Try to get access token from cookie
            String accessToken = null;
            String refreshToken = null;
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        log.debug("Found access token in cookies");
                    } else if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        log.debug("Found refresh token in cookies");
                    }
                }
            } else {
                log.debug("No cookies found in request");
            }

            if (accessToken == null) {
                log.debug("No access token found, proceeding with chain");
                chain.doFilter(request, response);
                return;
            }

            // Try to validate access token
            if (jwtUtil.validateJwtToken(accessToken)) {
                log.debug("Access token is valid");
                String username = jwtUtil.getUsernameFromJwt(accessToken);
                String role = jwtUtil.getRoleFromJwt(accessToken);
                log.info("User authenticated - Username: {}, Role: {}", username, role);
                setAuthentication(request, accessToken);
                chain.doFilter(request, response);
                return;
            } else {
                log.debug("Access token is invalid, trying refresh token");
            }

            // If access token is invalid but we have refresh token, try to refresh
            if (refreshToken != null) {
                try {
                    // Validate refresh token
                    if (jwtUtil.validateJwtToken(refreshToken)) {
                        log.debug("Refresh token is valid");
                        // Find user by refresh token
                        String username = jwtUtil.getUsernameFromJwt(refreshToken);
                        log.debug("Username from refresh token: {}", username);

                        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ApiException("User not found"));

                        if (refreshToken.equals(user.getRefreshToken())) {
                            log.debug("Refresh token matches stored token");
                            // Generate new access token
                            String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
                            log.info("Generated new access token for user: {}", username);

                            // Set new access token in cookie
                            Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
                            newAccessTokenCookie.setHttpOnly(true);
                            newAccessTokenCookie.setSecure(true);
                            newAccessTokenCookie.setPath("/");
                            newAccessTokenCookie.setMaxAge(30 * 60);
                            response.addCookie(newAccessTokenCookie);
                            log.debug("Set new access token cookie");

                            // Set authentication with new token
                            setAuthentication(request, newAccessToken);
                            chain.doFilter(request, response);
                            return;
                        } else {
                            log.warn("Stored refresh token doesn't match the provided one");
                        }
                    } else {
                        log.warn("Refresh token is invalid");
                    }
                } catch (Exception e) {
                    log.error("Error during token refresh: {}", e.getMessage());
                    clearAuthCookies(response);
                    sendErrorResponse(response, "Authentication failed");
                    return;
                }
            }

            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            sendErrorResponse(response, "JWT token has expired");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            sendErrorResponse(response, "Authentication failed");
        }
    }

    private void setAuthentication(HttpServletRequest request, String token) {
        try {
            var authentication = jwtUtil.getAuthentication(token);
            if (authentication != null) {
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    ((UsernamePasswordAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    log.debug("Setting authentication in SecurityContext");
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set successfully");
            } else {
                log.warn("Authentication object is null");
            }
        } catch (Exception e) {
            log.error("Error setting authentication: {}", e.getMessage(), e);
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", message);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}

