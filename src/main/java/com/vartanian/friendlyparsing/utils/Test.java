package com.vartanian.friendlyparsing.utils;

import com.vartanian.friendlyparsing.parsers.MultiThreadFriendlyParserImpl;
import com.vartanian.friendlyparsing.parsers.MultiThreadSimpleParserImpl;
import com.vartanian.friendlyparsing.model.Item;
import com.vartanian.friendlyparsing.model.LevelLink;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by super on 9/22/15.
 */
public class Test {

    private static Logger LOG = Logger.getLogger(Test.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {

        int NCPU = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newCachedThreadPool(); //TODO: customize

        int maxDeep = 1;
        String site = "http://kt.ua";
        String template;

        String hrefQuery = new StringBuilder().append("a[href*=")
                .append("http://kt.ua")
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


        long start = System.currentTimeMillis();

        MultiThreadSimpleParserImpl parseTask = new MultiThreadSimpleParserImpl(resultLinks, site, maxDeep, hrefQuery, queue, fullLinks);
        executorService.submit(parseTask);

        Thread.sleep(3000);
        for (int i = 0; i < NCPU-1; i++) {
            executorService.submit(parseTask);
        }

        Set<Item> resultItems = new HashSet<>();
        MultiThreadFriendlyParserImpl friendlyTask = new MultiThreadFriendlyParserImpl(resultLinks, resultItems);

        for (int i = 0; i < NCPU*2; i++) {
            executorService.submit(friendlyTask);
        }

        executorService.shutdown();

        boolean termination = false;
        try {
            termination = executorService.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Exception: ", e);
        }

        if (!termination){
            System.out.println("!!!!!!!!! termination = " + termination);
            executorService.shutdownNow();
        }

        long stop = System.currentTimeMillis();

        System.out.println("-----------------------------------------------");
        for (String resultLink : resultLinks) {
            System.out.println("link: " + resultLink);
        }
        System.out.println("Total links = " + resultLinks.size() + " time, ms: " + (stop-start));

        System.out.println("-----------------------------------------------");
        for (Item resultItem : resultItems) {
            System.out.println("Item: " + resultItem.getUrl() + " pass = " + resultItem.getPass() + " score = " + resultItem.getScore());
        }

    }
}
