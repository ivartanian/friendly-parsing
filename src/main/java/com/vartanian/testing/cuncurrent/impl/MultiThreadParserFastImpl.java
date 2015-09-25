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
public class MultiThreadParserFastImpl implements Parser {

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 300;

    private final String site;
    private final String hrefQuery;
    private final int maxDeep;

    private ArrayBlockingQueue<String> resultLinks;
    private ArrayBlockingQueue<String>[] queues;

    public MultiThreadParserFastImpl(ArrayBlockingQueue<String> resultLinks, String site, int maxDeep, String hrefQuery, ArrayBlockingQueue<String>[] queues) {
        this.resultLinks = resultLinks;
        this.queues = queues;
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

        putLink(site, currentLevel);

        while (true){

            LevelLink levelLink = readNextLink();
            if (levelLink==null) {
                break;
            }
            requestNextLinks(levelLink.getLink(), levelLink.getLevel());

        }

    }

    public LevelLink readNextLink() {

        LevelLink levelLink = null;
        for (int i = 0; i < maxDeep; i++) {
            ArrayBlockingQueue<String> queue = queues[i];
            try {
                String link = queue.poll(10, TimeUnit.MILLISECONDS);
                if (link != null){
                    levelLink = new LevelLink(link, i);
                    putResultLink(link);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return levelLink;
    }

    public synchronized void putLink(String link, int currentLevel) {

        try {
            if (!queues[currentLevel].contains(link)){
                queues[currentLevel].offer(link, 1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void requestNextLinks(String currentURL, int currentLevel) {

        currentLevel++;

        String ch = "";
        for (int i = 0; i < currentLevel; i++) {
            ch = ch + "--";
        }
        System.out.println(" " + ch + " " + currentURL + " ||| Thread: " + Thread.currentThread().getName() + " ||| level: " + currentLevel);

        if (currentLevel >= maxDeep) {
            return;
        }

        Elements links = getLinks(currentURL);

        if (links == null){
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
            putLink(newURI, currentLevel);
        }

        LevelLink nextLevelLink = readNextLink();
        if (nextLevelLink != null){
            requestNextLinks(nextLevelLink.getLink(), nextLevelLink.getLevel());
        }
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

    private synchronized void putResultLink(String resultLink) {
        if (!resultLinks.contains(resultLink)) {
            try {
                resultLinks.offer(resultLink, 10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
