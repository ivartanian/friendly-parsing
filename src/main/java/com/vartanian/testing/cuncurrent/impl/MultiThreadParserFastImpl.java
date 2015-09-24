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
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by super on 9/23/15.
 */
public class MultiThreadParserFastImpl implements Parser {

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 300;

    private final String site;
    private final String hrefQuery;
    private final int maxDeep;

    private ConcurrentHashMap<String, Object> resultLinks;
    private ArrayBlockingQueue<String> queue;

    public MultiThreadParserFastImpl(ConcurrentHashMap<String, Object> resultLinks, String site, int maxDeep, String hrefQuery, ArrayBlockingQueue<String> queue) {
        this.resultLinks = resultLinks;
        this.queue = queue;
        this.site = site;
        this.hrefQuery = hrefQuery;
        this.maxDeep = maxDeep;
    }

    @Override
    public void init() {

    }

    @Override
    public void run() {

        int currentLevel = 0;

        String currentURL = null;
        try {
            currentURL = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        Elements links = getLinks(currentURL);

        if (links == null){
            resultLinks.put(currentURL, currentURL);
            return;
        }

        for (Element element : links) {
            URL url;
            try {
                url = new URL(element.attr("abs:href"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                continue;
            }
            String newURI = Utils.toFullForm(url, false);
            if (newURI == null){
                continue;
            }
            getRef(newURI, currentLevel);
        }

    }

    private void getRef(String currentURL, int currentLevel) {

        if (currentLevel > maxDeep) {
            return;
        }

        if (resultLinks.contains(currentURL)) {
            return;
        }

        resultLinks.put(currentURL, currentURL);

        String ch = "";
        for (int i = 0; i < currentLevel; i++) {
            ch = ch + "-";
        }
        System.out.println(ch + currentURL + " ||| Thread: " + Thread.currentThread().getName() + "|||| level: " + currentLevel);

        randomPause(MIN_DELAY, MAX_DELAY);


    }

    private Elements getLinks(String currentURL) {

        Document doc;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException e) {
            return null;
        }

        //get all ref on a page
        return doc.select(hrefQuery);

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
