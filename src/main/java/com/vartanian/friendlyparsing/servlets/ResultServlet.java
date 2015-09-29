package com.vartanian.friendlyparsing.servlets;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(name = "ResultServlet", urlPatterns = {"/results/*"})
public class ResultServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String path = getServletContext().getRealPath("");
        String newURI = request.getRequestURI().replaceAll(request.getContextPath(), "");
        if (!new File(path + newURI.substring(1)).exists()){
            getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
        }
        // modifies response
        response.setContentType("application/octet-stream");

        try (InputStream inputStream = getServletContext().getResourceAsStream(newURI);
             ServletOutputStream outputStream = response.getOutputStream();) {

            IOUtils.copy(inputStream, outputStream);

        }

    }

}
