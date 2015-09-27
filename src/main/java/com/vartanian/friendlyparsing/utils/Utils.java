package com.vartanian.friendlyparsing.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

public class Utils {

    private static Logger LOG = Logger.getLogger(Utils.class.getName());

    public String getResponse(String methodName, String url, String[] names, String[] values) {

        HttpMethod method = getHttpMethod(methodName, url, names, values);
        return getBodyResult(method);

    }

    public HttpMethod getHttpMethod(String methodName, String url, String[] names, String[] values) {

        if (names == null || values == null){
            return null;
        }

        if (names.length != values.length) {
            return null;
        }
        if (!(methodName.equalsIgnoreCase("GET") | methodName.equalsIgnoreCase("POST"))) {
            return null;
        }

        HttpMethod method;
        if ("GET".equalsIgnoreCase(methodName)) {
            StringBuilder stringBuilder = new StringBuilder();
            ArrayList parameters = new ArrayList();
            for (int i = 0; i < names.length; i++) {
                stringBuilder.append(names[i] + "=" + values[i]);
                if ((names.length - 1) > i) {
                    stringBuilder.append("&");
                }
            }
            method = new GetMethod(url + (stringBuilder.length()!=0?"?":"") + stringBuilder.toString());
        } else {
            method = new PostMethod(url);
            for (int i = 0; i < names.length; i++) {
                ((PostMethod) method).addParameter(names[i], values[i]);
            }
            method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        return method;
    }

    public String getBodyResult(HttpMethod method) {
        if (method == null){
            return null;
        }
        try {
            new HttpClient().executeMethod(method);
            InputStream responseBodyAsStream = method.getResponseBodyAsStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(responseBodyAsStream, writer, "utf8");
            return writer.toString();
        } catch (IOException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }
        return null;
    }

    public static String toFullForm(URL url, boolean root) {

        if (url == null){
            return null;
        }

        StringBuilder result = new StringBuilder();
        result.append(url.getProtocol());
        result.append(":");
        if (url.getAuthority() != null && url.getAuthority().length() > 0) {
            result.append("//");
            result.append(url.getAuthority());
        }
        if (!root && url.getPath() != null) {
            result.append(url.getPath());
        }
        return result.toString();
    }

}
