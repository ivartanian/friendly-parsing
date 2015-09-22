package com.vartanian.testing.utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by super on 9/22/15.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        Utils utils = new Utils();

        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 1; i < 11; i++) {
            final int finalI = i;
            service.submit(new Runnable() {
                public void run() {
                    try {
                        utils.getResponse("POST", "http://127.0.0.1:8080/testing/parse", new String[]{"site_url", "max_deep"}, new String[]{"http://kt.ua", String.valueOf(finalI)});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread: "  +Thread.currentThread().getName() + "---------end() id = " + Thread.currentThread().getId());
                }
            });
        }

    }
}
