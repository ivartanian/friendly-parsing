package com.vartanian.testing.utils;

import com.vartanian.testing.cuncurrent.Parser;
import com.vartanian.testing.cuncurrent.impl.MultiThreadParserFastImpl;
import com.vartanian.testing.cuncurrent.impl.MultiThreadSimpleParserImpl;
import com.vartanian.testing.model.Item;
import com.vartanian.testing.model.LevelLink;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by super on 9/22/15.
 */
public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {

        int NCPU = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newCachedThreadPool(); //TODO: customize

        ArrayBlockingQueue<String> resultLinks = new ArrayBlockingQueue<>(100000);

        Set<Item> resultItems = new HashSet<>();
        int maxDeep = 3;
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

        long start = System.currentTimeMillis();

        Parser task = new MultiThreadSimpleParserImpl(resultLinks, site, maxDeep, hrefQuery, queue);
        for (int i = 0; i < NCPU; i++) {
            Thread.sleep(3000);
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

        long stop = System.currentTimeMillis();

        System.out.println("-----------------------------------------------");
        for (String resultLink : resultLinks) {
            System.out.println("link: " + resultLink);
        }
        System.out.println("Total links = " + resultLinks.size() + " time, ms: " + (stop-start));
    }
}
