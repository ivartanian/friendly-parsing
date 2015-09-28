//package com.vartanian.friendlyparsing.filters;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * Created by super on 9/28/15.
// */
//@WebFilter(filterName = "StaticFilter", urlPatterns = "/resources/*")
//public class StaticFilter implements Filter {
//    public void destroy() {
//    }
//
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String newURI = httpRequest.getRequestURI().replaceAll(httpRequest.getContextPath(), "");
//        httpRequest.getRequestDispatcher(newURI).forward(httpRequest, response);
//
////        chain.doFilter(httpRequest, response);
//    }
//
//    public void init(FilterConfig config) throws ServletException {
//    }
//
//}
