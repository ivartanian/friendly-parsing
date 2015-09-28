package com.vartanian.friendlyparsing.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.vartanian.friendlyparsing.parsers.MultiThreadFriendlyParserImpl;
import com.vartanian.friendlyparsing.parsers.MultiThreadSimpleParserImpl;
import com.vartanian.friendlyparsing.model.Item;
import com.vartanian.friendlyparsing.model.LevelLink;
import com.vartanian.friendlyparsing.utils.JsonUtil;
import com.vartanian.friendlyparsing.utils.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(name = "Main", urlPatterns = "/parse")
public class Main extends HttpServlet {

    private static Logger LOG = Logger.getLogger(Main.class.getName());

    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    private final Utils utils = new Utils();

    private final JsonUtil jsonUtil = new JsonUtil();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ExecutorService executorService = Executors.newCachedThreadPool(); //TODO: customize

        int maxDeep = 2;
        String site;
        String template;

        String site_url = request.getParameter("site_url");

        if (site_url == null || "".equals(site_url) || !site_url.startsWith("http://")) {
            getServletContext().getRequestDispatcher("/").forward(request, response);
        }

        String max_deep = request.getParameter("max_deep");
        if (max_deep != null && !"".equals(max_deep)) {
            maxDeep = Math.abs(Integer.valueOf(max_deep));
        }

        URL url = new URL(site_url);
        site = Utils.toFullForm(url, false);
        template = Utils.toFullForm(url, true);

        String hrefQuery = new StringBuilder().append("a[href*=")
                .append(template)
                .append("]")
                .append(":not([href$=.jpg])")
                .append(":not([href$=.xls])")
                .append(":not([href$=.gif])")
                .append(":not([href$=.png])")
                .append(":not([href$=.jpeg])")
                .append(":not([href$=.css])")
                .append(":not([href$=.js])")
                .toString();

        ArrayBlockingQueue<LevelLink> queue = new ArrayBlockingQueue<>(100000);
        ArrayBlockingQueue<String> resultLinks = new ArrayBlockingQueue<>(100000);
        List<String> fullLinks = new ArrayList<>();

        MultiThreadSimpleParserImpl parseTask = new MultiThreadSimpleParserImpl(resultLinks, site, maxDeep, hrefQuery, queue, fullLinks);
        executorService.submit(parseTask);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }
        for (int i = 0; i < NCPU-1; i++) {
            executorService.submit(parseTask);
        }

        Set<Item> resultItems = new HashSet<>();
        MultiThreadFriendlyParserImpl friendlyTask = new MultiThreadFriendlyParserImpl(resultLinks, resultItems);

        for (int i = 0; i < NCPU*2; i++) {
            executorService.submit(friendlyTask);
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }

        request.setAttribute("site", site);
        request.setAttribute("resultItems", resultItems);
        getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
    }

}
