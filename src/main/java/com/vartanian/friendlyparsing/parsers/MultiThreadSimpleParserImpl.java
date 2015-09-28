package com.vartanian.friendlyparsing.parsers;

import com.vartanian.friendlyparsing.model.LevelLink;
import com.vartanian.friendlyparsing.utils.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by super on 9/23/15.
 */
public class MultiThreadSimpleParserImpl implements Runnable {

    private static Logger LOG = Logger.getLogger(MultiThreadSimpleParserImpl.class.getName());

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 300;

    private final LevelLink site;
    private final String hrefQuery;
    private final int maxDeep;

    private ArrayBlockingQueue<String> resultLinks;
    private ArrayBlockingQueue<LevelLink> queue;
    private List<String> fullLinks;

    public MultiThreadSimpleParserImpl(ArrayBlockingQueue<String> resultLinks, String site, int maxDeep, String hrefQuery, ArrayBlockingQueue<LevelLink> queue, List<String> fullLinks) {
        this.resultLinks = resultLinks;
        this.queue = queue;
        this.fullLinks = fullLinks;
        this.site = new LevelLink(site, 0);
        this.hrefQuery = hrefQuery;
        this.maxDeep = maxDeep;
    }

    @Override
    public void run() {

        try {
            putLink(site);
            while (true) {

                LevelLink levelLink = readNextLink();
                if (levelLink == null) {
                    break;
                }
                requestNextLinks(levelLink);

            }

        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }

    }

    public LevelLink readNextLink() throws InterruptedException {

        LevelLink levelLink = queue.poll(10, TimeUnit.MILLISECONDS);
        if (levelLink != null) {
            putResultLink(levelLink);
        }
        return levelLink;
    }

    public synchronized void putLink(LevelLink levelLink) throws InterruptedException {

        if (levelLink == null) return;

        if (!fullLinks.contains(levelLink.getLink())) {
            queue.offer(levelLink, 10, TimeUnit.MILLISECONDS);
        }

    }

    private void requestNextLinks(LevelLink levelLink) throws InterruptedException {

        int currentLevel = levelLink.getLevel() + 1;

        String ch = "";
        for (int i = 0; i < currentLevel; i++) {
            ch = ch + "--";
        }

        if (currentLevel >= maxDeep) {
            return;
        }

        randomPause();

        Elements links = getLinks(levelLink.getLink());

        if (links == null) {
            return;
        }

        LOG.log(Level.DEBUG, " " + ch + " " + levelLink.getLink() + " ||| Thread: " + Thread.currentThread().getName() + " ||| level: " + currentLevel + " newLinks=" + links.size());

        //get all ref on a page
        for (Element element : links) {
            URL url = null;
            try {
                url = new URL(element.attr("abs:href"));
            } catch (MalformedURLException e) {
                LOG.log(Level.INFO, "Exception: ", e);
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
            doc = Jsoup.connect(currentURL).timeout(10000).get();
        } catch (IOException e) {
            LOG.log(Level.INFO, "Exception connection URL: " + currentURL, e);
            return null;
        }

        //get all ref on a page
        return doc.select(hrefQuery);

    }

    private synchronized void putResultLink(LevelLink resultLink) throws InterruptedException {
        if (!fullLinks.contains(resultLink.getLink())) {
            resultLinks.offer(resultLink.getLink(), 10, TimeUnit.MILLISECONDS);
            fullLinks.add(resultLink.getLink());
        }
    }

    public void randomPause() throws InterruptedException {
        Random rnd = new Random();
        long delay = MIN_DELAY + rnd.nextInt(MAX_DELAY - MIN_DELAY);
        Thread.sleep(delay);
    }

}
