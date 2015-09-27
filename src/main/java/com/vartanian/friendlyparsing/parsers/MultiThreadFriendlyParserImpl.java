package com.vartanian.friendlyparsing.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.vartanian.friendlyparsing.model.Item;
import com.vartanian.friendlyparsing.model.LevelLink;
import com.vartanian.friendlyparsing.utils.JsonUtil;
import com.vartanian.friendlyparsing.utils.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by super on 9/23/15.
 */
public class MultiThreadFriendlyParserImpl implements Runnable {

    private static Logger LOG = Logger.getLogger(MultiThreadFriendlyParserImpl.class.getName());

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 300;

    private final Utils utils = new Utils();
    private final JsonUtil jsonUtil = new JsonUtil();

    private ArrayBlockingQueue<String> resultLinks;
    private ArrayBlockingQueue<LevelLink> queue;
    private Set<Item> resultItems;

    public MultiThreadFriendlyParserImpl(ArrayBlockingQueue<String> resultLinks, Set<Item> resultItems) {
        this.resultLinks = resultLinks;
        this.resultItems = resultItems;
    }

    @Override
    public void run() {

        try {
            String currentLink = null;
            while ((currentLink = resultLinks.poll(5000, TimeUnit.MILLISECONDS)) != null){
                JsonNode resultNode = null;
                try {
                    resultNode = getResultFriendlyJsonNode(currentLink, "ru_RU");
                    Item resultFriendlyFinal = getResultFriendlyFinal(resultNode, null);
                    if (resultFriendlyFinal != null) {
                        LOG.log(Level.DEBUG, "resultFriendlyFinal = " + resultFriendlyFinal.getUrl() + " score = " + resultFriendlyFinal.getScore() + " pass = " + resultFriendlyFinal.getPass());
                        resultItems.add(resultFriendlyFinal);
                    }
                } catch (IOException e) {
                    LOG.log(Level.INFO, "Exception: ", e);
                }

            }
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }


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
            LOG.log(Level.INFO, "Exception: ", e);
        }
        return null;
    }

    public void randomPause() throws InterruptedException {
        Random rnd = new Random();
        long delay = MIN_DELAY + rnd.nextInt(MAX_DELAY - MIN_DELAY);
        Thread.sleep(delay);
    }

}
