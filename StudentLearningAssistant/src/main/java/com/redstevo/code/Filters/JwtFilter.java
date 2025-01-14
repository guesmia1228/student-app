package com.redstevo.code.Filters;

import com.redstevo.code.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        /*Checking the request header*/
        log.info("Checking the request header");

        String requestHeader = request.getHeader("Authorization");


        /*Checking if the header is empty*/
        if (requestHeader == null || !requestHeader.startsWith("Bearer ")) {
            log.warn("Request Does Not Contain A jwt Forwarding it to the next filter");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Bearer token present");

        /*Getting the jwt*/
        String jwt = requestHeader.substring(7);

        /*Getting the username*/
        String username = jwtService.getUsername(jwt);

        /*Check if the jwt has been corrupted*/
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            log.info("Bearer token extracted");

            /*Getting the AuthTable by user*/
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isValid(userDetails, jwt)) {


                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());


                token.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                log.info("set the user details.");

                SecurityContextHolder.getContext().setAuthentication(token);

            }

            filterChain.doFilter(request, response);
        }
    }
}