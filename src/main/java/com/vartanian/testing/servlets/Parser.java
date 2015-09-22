package com.vartanian.testing.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.vartanian.testing.model.Item;
import com.vartanian.testing.utils.JsonUtil;
import com.vartanian.testing.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(urlPatterns = "/parse")
public class Parser extends HttpServlet {

    private static Logger LOG = Logger.getLogger(Parser.class.getName());

    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    private final Utils utils = new Utils();
    private final JsonUtil jsonUtil = new JsonUtil();

    private final int MIN_DELAY = 100;
    private final int MAX_DELAY = 300;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap();

        Set<String> passedRef = new HashSet<>();
        Set<Item> resultItems = new HashSet<>();
        int maxDeep = 2;
        String site;
        String template;

        String site_url = request.getParameter("site_url");

        if (site_url == null || "".equals(site_url) || !site_url.startsWith("http://")) {
            getServletContext().getRequestDispatcher("/").forward(request, response);
        }

        String max_deep = request.getParameter("max_deep");
        if (max_deep != null && !"".equals(max_deep)) {
            maxDeep = Integer.valueOf(max_deep);
        }

        URL url = new URL(site_url);
        site = toFullForm(url, false);
        template = toFullForm(url, true);

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

        setThreadLocalParameter("passedRef", passedRef);
        setThreadLocalParameter("resultItems", resultItems);
        setThreadLocalParameter("maxDeep", maxDeep);
        setThreadLocalParameter("site", site);
        setThreadLocalParameter("template", template);
        setThreadLocalParameter("hrefQuery", hrefQuery);

        startParsing();

        startChecking();

        request.setAttribute("resultItems", resultItems);
        getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
    }

    private String toFullForm(URL url, boolean root) {

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

    public void startParsing() {

        int currentLevel = 0;

        String url = (String) getThreadLocalParameter("site");
        getRef(url, currentLevel);

    }

    private void getRef(String currentURL, int currentLevel) {

        currentLevel++;
        int maxDeep = (int) getThreadLocalParameter("maxDeep");
        if (currentLevel > maxDeep) {
            if (currentLevel > 0) {
                currentLevel--;
            }
            return;
        }

        Set<String> passedRef = (Set<String>) getThreadLocalParameter("passedRef");
        String hrefQuery = (String) getThreadLocalParameter("hrefQuery");

        if (passedRef.contains(currentURL)) {
            if (currentLevel > 0) {
                currentLevel--;
            }
            return;
        }
        passedRef.add(currentURL);

        System.out.println(currentURL);

        try {
            randomPause();
        } catch (InterruptedException e) {
            //NOP
        }

        Document doc;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException e) {
            if (currentLevel > 0) {
                currentLevel--;
            }
            return;
        }

        //get all ref on a page
        Elements links = doc.select(hrefQuery);
        for (Element element : links) {
            URL url = null;
            try {
                url = new URL(element.attr("abs:href"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String newURI = toFullForm(url, false);
            if (newURI == null){
                continue;
            }
            getRef(newURI, currentLevel);
        }

    }

    public void randomPause() throws InterruptedException {
        Random rnd = new Random();
        long delay = MIN_DELAY + rnd.nextInt(MAX_DELAY - MIN_DELAY);
        Thread.sleep(delay);
    }

    public JsonNode getResultFriendlyJsonNode(String url, String locale) throws IOException {

        String json = utils.getResponse("GET", "https://www.googleapis.com/pagespeedonline/v3beta1/mobileReady/",
                new String[]{"screenshot", "snapshots", "locale", "url", "strategy", "filter_third_party_resources"},
                new String[]{"true", "true", locale, url, "mobile", "false"});

        return jsonUtil.fromJsonToTree(json);

    }

    public Item getResultFriendlyFinal(JsonNode resultNode, String[] fields) {

        JsonNode idNode = null;
        try {
            idNode = jsonUtil.getJsonElement(resultNode, "id");
            if (idNode == null){
                return null;
            }
            JsonNode ruleGroupsNode = jsonUtil.getJsonElement(resultNode, "ruleGroups");
            JsonNode usabilityNode = jsonUtil.getJsonElement(ruleGroupsNode, "USABILITY");
            JsonNode scoreNode = jsonUtil.getJsonElement(usabilityNode, "score");
            JsonNode passNode = jsonUtil.getJsonElement(usabilityNode, "pass");
            return new Item(idNode.toString(), (scoreNode != null ? scoreNode.toString() : null), (passNode != null ? passNode.toString() : null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> getThreadLocalParameters() {
        Map<String, Object> stringObjectMap = threadLocal.get();
        if (stringObjectMap == null) {
            stringObjectMap = new ConcurrentHashMap<>();
            threadLocal.set(stringObjectMap);
        }
        return stringObjectMap;
    }

    private Object getThreadLocalParameter(String key) {
        Map<String, Object> threadLocalParameters = getThreadLocalParameters();
        if (threadLocalParameters.containsKey(key)) {
            return threadLocalParameters.get(key);
        }
        return null;
    }

    private void setThreadLocalParameter(String key, Object value) {
        Map<String, Object> stringObjectMap = threadLocal.get();
        if (stringObjectMap == null) {
            stringObjectMap = new ConcurrentHashMap<>();
        }
        stringObjectMap.put(key, value);
        threadLocal.set(stringObjectMap);
    }

    private void startChecking() {
        Set<String> passedRef = (Set<String>) getThreadLocalParameter("passedRef");
        Set<Item> resultItems = (Set<Item>) getThreadLocalParameter("resultItems");
        for (String href : passedRef) {
            JsonNode resultNode = null;
            try {
                resultNode = getResultFriendlyJsonNode(href, "ru_RU");
                Item resultFriendlyFinal = getResultFriendlyFinal(resultNode, null);
                if (resultFriendlyFinal != null) {
                    resultItems.add(resultFriendlyFinal);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
