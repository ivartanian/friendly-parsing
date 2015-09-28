package com.vartanian.friendlyparsing.servlets;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(name = "StaticServlet", urlPatterns = {"/resources/*", "/webjars/*"})
public class StaticServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String newURI = request.getRequestURI().replaceAll(request.getContextPath(), "");
        try (InputStream inputStream = getServletContext().getResourceAsStream(newURI);
             ServletOutputStream outputStream = response.getOutputStream();) {

            IOUtils.copy(inputStream, outputStream);

        }

    }

}
