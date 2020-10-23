package com.tonbu.framework.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CmdUtils {

    protected static Logger logger=LogManager.getLogger(CmdUtils.class.getName());

    public static void exec(String cmd) {

        if (cmd == null || cmd.equals(""))
            return;

        logger.info("Exec Command: " + cmd);

        List<String> command = new ArrayList<String>();

        command = Arrays.asList(StringUtils.split(cmd, " "));

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        Process process = null;

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        WatchThread watchThread4Error = new WatchThread(process.getErrorStream());
        WatchThread watchThread4Input = new WatchThread(process.getInputStream());

        watchThread4Error.start();
        watchThread4Input.start();

        try {
            process.waitFor();
            watchThread4Error.setOver(true);
            watchThread4Input.setOver(true);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("process exception");
        }
    }

    static class WatchThread extends Thread {
        private InputStream inputStream;
        private boolean over = false;

        public WatchThread(InputStream is) {
            this.inputStream = is;
            over = false;
        }

        @Override
        public void run() {
            try {
                if (inputStream == null) {
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "gbk"));

                while (true) {
                    if (inputStream == null || over) {
                        break;
                    }
                    String temp;
                    StringBuffer sb = new StringBuffer("\n");
                    while ((temp = br.readLine()) != null) {
                        if (temp == null || temp.trim().equals(""))
                            continue;
                        sb.append(temp).append(System.getProperty("line.separator"));
                    }

                    if (sb.length() > 1) {
                        logger.info(sb.toString());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException("thread run method exception");
            }
        }

        public void setOver(boolean over) {
            this.over = over;
        }
    }
}
