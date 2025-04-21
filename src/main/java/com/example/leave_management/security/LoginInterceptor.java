package com.example.leave_management.security;

import com.example.leave_management.exception.ForbiddenException;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresLogin requiresLogin = handlerMethod.getMethodAnnotation(RequiresLogin.class);
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);

        if (requiresLogin == null && requiresRole == null) {
            return true;
        }

        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token, null)) {
            sendUnauthorizedResponse(response);
            return false;
        }

        if (requiresRole == null) {
            return true;
        }

        Claims claims = jwtUtil.extractAllClaims(token);
        String userRoleStr = claims.get("roles", String.class);
        if (userRoleStr == null) {
            throw new ForbiddenException("User role not found in token");
        }

        UserRole userRole = UserRole.valueOf(userRoleStr);
        List<UserRole> requiredRoles = Arrays.asList(requiresRole.value());
        if (!requiredRoles.contains(userRole)) {
            throw new ForbiddenException("Insufficient permissions");
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Unauthorized\",\"success\":false,\"status\":\"UNAUTHORIZED\",\"type\":\"auth\"}");
    }
}