package com.vartanian.testing.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.vartanian.testing.cuncurrent.Parser;
import com.vartanian.testing.cuncurrent.impl.MultiThreadParserFastImpl;
import com.vartanian.testing.cuncurrent.impl.MultiThreadParserImpl;
import com.vartanian.testing.model.Item;
import com.vartanian.testing.utils.JsonUtil;
import com.vartanian.testing.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(urlPatterns = "/parse")
public class Main extends HttpServlet {

    private static Logger LOG = Logger.getLogger(Main.class.getName());

    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    private final Utils utils = new Utils();

    private final JsonUtil jsonUtil = new JsonUtil();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ExecutorService executorService = Executors.newCachedThreadPool(); //TODO: customize

        ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap();
        Map<String, Object> parameters = new HashMap<>();

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

        parameters.put("maxDeep", maxDeep);
        parameters.put("site", site);
        parameters.put("hrefQuery", hrefQuery);

        ArrayBlockingQueue<String>[] queues = new ArrayBlockingQueue[maxDeep];
        for (int i = 0; i < maxDeep; i++) {
            queues[i] = new ArrayBlockingQueue<String>(1000);
        }
        for (int i = 0; i <= NCPU; i++) {
            Parser task = new MultiThreadParserImpl(concurrentHashMap, parameters);
            task.init();
            executorService.submit(task);
        }
        executorService.shutdown();

        boolean termination = false;
        try {
            termination = executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!termination){
            System.out.println("!!!!!!!!! termination = " + termination);
            executorService.shutdownNow();
        }

        System.out.println("-----------------------------------------------");
        Set<Map.Entry<String, Object>> entries = concurrentHashMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println("key: " + entry.getKey() + "; value: " + entry.getValue());
        }
        System.out.println("Total links = " + concurrentHashMap.size());

//        startParsing();
//
//        startChecking();

        request.setAttribute("resultItems", resultItems);
        getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
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

//    private Map<String, Object> getThreadLocalParameters() {
//        Map<String, Object> stringObjectMap = threadLocal.get();
//        if (stringObjectMap == null) {
//            stringObjectMap = new ConcurrentHashMap<>();
//            threadLocal.set(stringObjectMap);
//        }
//        return stringObjectMap;
//    }
//
//    private Object getThreadLocalParameter(String key) {
//        Map<String, Object> threadLocalParameters = getThreadLocalParameters();
//        if (threadLocalParameters.containsKey(key)) {
//            return threadLocalParameters.get(key);
//        }
//        return null;
//    }
//
//    private void setThreadLocalParameter(String key, Object value) {
//        Map<String, Object> stringObjectMap = threadLocal.get();
//        if (stringObjectMap == null) {
//            stringObjectMap = new ConcurrentHashMap<>();
//        }
//        stringObjectMap.put(key, value);
//        threadLocal.set(stringObjectMap);
//    }

//    private void startChecking() {
//        Set<String> passedRef = (Set<String>) getThreadLocalParameter("passedRef");
//        Set<Item> resultItems = (Set<Item>) getThreadLocalParameter("resultItems");
//        for (String href : passedRef) {
//            JsonNode resultNode = null;
//            try {
//                resultNode = getResultFriendlyJsonNode(href, "ru_RU");
//                Item resultFriendlyFinal = getResultFriendlyFinal(resultNode, null);
//                if (resultFriendlyFinal != null) {
//                    resultItems.add(resultFriendlyFinal);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
