package com.vartanian.testing.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.vartanian.testing.utils.JsonUtil;
import com.vartanian.testing.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by super on 9/16/15.
 */
@WebServlet(urlPatterns = "/parse")
public class Parser extends HttpServlet {

    private Utils utils = new Utils();
    private JsonUtil jsonUtil = new JsonUtil();

    private static Set<String> passedRef = new HashSet<String>();
    private static String url;
    private static String template;

    private static int MIN_DELAY = 100;
    private static int MAX_DELAY = 300;

    private static int maxLevel = 2;

    private static String hrefQuery;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String site_url = request.getParameter("site_url");

        if (site_url == null){
            getServletContext().getRequestDispatcher("/").forward(request, response);
        }

        URI uri;
        try {
            uri = new URI(site_url);
            site_url = uri.getScheme() != null ? uri.getScheme() + "://" : "http://";
            site_url = uri.getHost() != null ? site_url + uri.getHost() : site_url + template;
            site_url = site_url + uri.getPath();
            url = site_url;
            template = uri.getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        //startParsing();

        for (String href : passedRef) {
            JsonNode resultNode = getResultFriendlyJsonNode(href, "ru_RU");
            String resultFriendlyFinal = getResultFriendlyFinal(resultNode, null);
            out.println(resultFriendlyFinal);
        }
        out.print("Хух");
        out.close();

    }

    public void startParsing() {

        hrefQuery = new StringBuilder().append("a[href*=")
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

        int currentLevel = 0;

        getRef(url, currentLevel);

    }

    private void getRef(String currentURL, int currentLevel) {

        currentLevel++;

        if (currentLevel > maxLevel){
            if (currentLevel > 0){
                currentLevel--;
            }
            return;
        }
        if (passedRef.contains(currentURL)){
            if (currentLevel > 0){
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

        Document doc = null;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException e) {
            e.printStackTrace();
            if (currentLevel > 0){
                currentLevel--;
            }
            return;
        }

        //get all ref on a page
        Elements links = doc.select(hrefQuery);
        for (Element element:links) {
            URI uri;
            String newURI = "";
            try {
                uri = new URI(element.attr("abs:href"));
                newURI = uri.getScheme() != null ? newURI + uri.getScheme() + "://" : newURI + "http://";
                newURI = uri.getHost() != null ? newURI + uri.getHost() : newURI + template;
                newURI = newURI + uri.getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            getRef(newURI, currentLevel);
        }

    }

    public static void randomPause() throws InterruptedException {
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

    public String getResultFriendlyFinal(JsonNode resultNode, String[] fields) throws IOException {

        JsonNode idNode = jsonUtil.getJsonElement(resultNode, "id");

        JsonNode ruleGroupsNode = jsonUtil.getJsonElement(resultNode, "ruleGroups");
        JsonNode usabilityNode = jsonUtil.getJsonElement(ruleGroupsNode, "USABILITY");
        JsonNode scoreNode = jsonUtil.getJsonElement(usabilityNode, "score");
        JsonNode passNode = jsonUtil.getJsonElement(usabilityNode, "pass");

        return "<strong> - " + idNode + ":</strong> score: " + scoreNode + ", pass: " + passNode + "<br/>";

    }

}
