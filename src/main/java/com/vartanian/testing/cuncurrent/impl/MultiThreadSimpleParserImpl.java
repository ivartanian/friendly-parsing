package com.vartanian.testing.cuncurrent.impl;

import com.vartanian.testing.cuncurrent.Parser;
import com.vartanian.testing.model.LevelLink;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by super on 9/23/15.
 */
public class MultiThreadSimpleParserImpl implements Parser {

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 300;

    private final LevelLink site;
    private final String hrefQuery;
    private final int maxDeep;

    private ArrayBlockingQueue<String> resultLinks;
    private ArrayBlockingQueue<LevelLink> queue;

    public MultiThreadSimpleParserImpl(ArrayBlockingQueue<String> resultLinks, String site, int maxDeep, String hrefQuery, ArrayBlockingQueue<LevelLink> queue) {
        this.resultLinks = resultLinks;
        this.queue = queue;
        this.site = new LevelLink(site, 0);
        this.hrefQuery = hrefQuery;
        this.maxDeep = maxDeep;
    }

    @Override
    public void init() {

    }

    @Override
    public void run() {

        putLink(site);

        while (true) {

            LevelLink levelLink = readNextLink();
            if (levelLink == null) {
                break;
            }
            requestNextLinks(levelLink);

        }

    }

    public LevelLink readNextLink() {

        LevelLink levelLink = null;
        try {
            levelLink = queue.poll(10, TimeUnit.MILLISECONDS);
            if (levelLink != null) {
                putResultLink(levelLink);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return levelLink;
    }

    public synchronized void putLink(LevelLink levelLink) {

        try {
            if (!resultLinks.contains(levelLink.getLink())) {
                queue.offer(levelLink, 1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void requestNextLinks(LevelLink levelLink) {

        int currentLevel = levelLink.getLevel() + 1;

        String ch = "";
        for (int i = 0; i < currentLevel; i++) {
            ch = ch + "--";
        }
        System.out.println(" " + ch + " " + levelLink.getLink() + " ||| Thread: " + Thread.currentThread().getName() + " ||| level: " + currentLevel);

        if (currentLevel >= maxDeep) {
            return;
        }

//        randomPause();

        Elements links = getLinks(levelLink.getLink());

        if (links == null) {
            return;
        }

        //get all ref on a page
        for (Element element : links) {
            URL url = null;
            try {
                url = new URL(element.attr("abs:href"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String newURI = Utils.toFullForm(url, false);
            if (newURI == null) {
                continue;
            }
            putLink(new LevelLink(newURI, currentLevel));
        }

        LevelLink nextLevelLink = readNextLink();
        if (nextLevelLink != null) {
            requestNextLinks(nextLevelLink);
        }
    }

    private Elements getLinks(String currentURL) {

        Document doc;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException e) {
            System.err.println("currentURL = "  +currentURL);
            e.printStackTrace();
            return null;
        }

        //get all ref on a page
        return doc.select(hrefQuery);

    }

    private synchronized void putResultLink(LevelLink resultLink) {
        if (!resultLinks.contains(resultLink.getLink())) {
            try {
                resultLinks.offer(resultLink.getLink(), 10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void randomPause() {
        Random rnd = new Random();
        long delay = MIN_DELAY + rnd.nextInt(MAX_DELAY - MIN_DELAY);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
