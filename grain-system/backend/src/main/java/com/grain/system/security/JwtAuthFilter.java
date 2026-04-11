package com.grain.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.result.R;
import com.grain.system.common.result.ResultCode;
import com.grain.system.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 验证Token有效性
        if (!jwtUtil.isValid(token)) {
            writeUnauthorized(response, "Token无效或已过期");
            return;
        }

        // 从Redis查LoginUser（Token已在Redis中存储）
        Long userId = jwtUtil.getUserId(token);
        String redisKey = RedisKey.USER_TOKEN + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        if (!token.equals(storedToken)) {
            writeUnauthorized(response, "Token已失效，请重新登录");
            return;
        }

        // 从Redis获取用户信息
        String userKey = RedisKey.USER_INFO + userId;
        String userJson = redisTemplate.opsForValue().get(userKey);
        if (userJson == null) {
            writeUnauthorized(response, "用户会话已失效，请重新登录");
            return;
        }

        LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);

        // 设置SecurityContext
        var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();
        if (loginUser.getPermissions() != null) {
            authorities.addAll(loginUser.getPermissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }
        if (loginUser.getRoleCodes() != null) {
            authorities.addAll(loginUser.getRoleCodes().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList());
        }
        
        var authentication = new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<?> result = R.fail(ResultCode.UNAUTHORIZED.getCode(), msg);
        response.getOutputStream().write(objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8));
    }
}
