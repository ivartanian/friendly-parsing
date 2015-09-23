package com.vartanian.testing.cuncurrent.impl;

import com.vartanian.testing.cuncurrent.Parser;
import com.vartanian.testing.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by super on 9/23/15.
 */
public class MultiThreadParserImpl implements Parser {

    private final int MIN_DELAY = 100;
    private final int MAX_DELAY = 300;

    private ConcurrentHashMap<String, Object> concurrentHashMap;
    private Map<String, Object> parameters;

    public MultiThreadParserImpl(ConcurrentHashMap<String, Object> concurrentHashMap, final Map<String, Object> parameters) {
        this.concurrentHashMap = concurrentHashMap;
        this.parameters = parameters;
    }

    @Override
    public void init() {

    }

    @Override
    public void run() {
        startParsing();
    }

    public void startParsing() {

        int currentLevel = 0;

        String url = (String) parameters.get("site");
        Boolean first = new Boolean(true);
        getRef(url, currentLevel, first);

    }

    private void getRef(String currentURL, int currentLevel, Boolean first) {

        currentLevel++;
        int maxDeep = (int) parameters.get("maxDeep");
        if (currentLevel > maxDeep) {
            if (currentLevel > 0) {
                currentLevel--;
            }
            return;
        }

//        Set<String> passedRef = (Set<String>) parameters.get("passedRef");
        String hrefQuery = (String) parameters.get("hrefQuery");

        if (!first && concurrentHashMap.contains(currentURL)) {
            if (currentLevel > 0) {
                currentLevel--;
            }
            return;
        }
        if (first){
            first = !first;
        }
        concurrentHashMap.put(currentURL, currentURL);

        String ch = "";
        for (int i = 0; i < currentLevel; i++) {
            ch = ch + "-";
        }
        System.out.println(ch + currentURL + " ||| Thread: " + Thread.currentThread().getName() + "|||| level: " + currentLevel);

        randomPause(MIN_DELAY, MAX_DELAY);

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
            String newURI = Utils.toFullForm(url, false);
            if (newURI == null){
                continue;
            }
            getRef(newURI, currentLevel, first);
        }

    }

    public void randomPause(int MIN_DELAY, int MAX_DELAY) {
        Random rnd = new Random();
        long delay = MIN_DELAY + rnd.nextInt(MAX_DELAY - MIN_DELAY);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
