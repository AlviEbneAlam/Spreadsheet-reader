package com.example.documentreader.security;

import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@ControllerAdvice
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @SneakyThrows
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(String.valueOf(MediaType.APPLICATION_JSON));

        PrintWriter writer = response.getWriter();


        JSONObject obj=new JSONObject();
        obj.put("timestamp", LocalDateTime.now().toString());
        obj.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        obj.put("error","Unauthorized");
        obj.put("message", authException.getMessage());
        obj.put("path", request.getServletPath());
        writer.print(obj);

    }

}
