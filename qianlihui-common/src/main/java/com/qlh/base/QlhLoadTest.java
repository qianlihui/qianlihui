package com.qlh.base;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class QlhLoadTest {
    /**
     * 并发数量
     */
    private int concurrent;

    private volatile boolean started;
    private List<Runnable> runnableList = new ArrayList<>();
    private List<Result> resultList;


    public QlhLoadTest concurrent(int concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    public QlhLoadTest submit(Runnable runnable) {
        if (started) {
            throw new RuntimeException("already started.");
        }
        runnableList.add(runnable);
        return this;
    }

    public QlhLoadTest start() {
        started = true;
        ExecutorService pool = Executors.newFixedThreadPool(concurrent);
        resultList = new ArrayList<>(runnableList.size());
        runnableList.forEach(e -> pool.submit(() -> {
            long t = System.currentTimeMillis();
            Result result = new Result();
            try {
                result.success = e.run();
            } catch (Exception exp) {
                log.error(exp.getMessage(), exp);
            }
            result.timeCost = System.currentTimeMillis() - t;
            resultList.add(result);
        }));
        runnableList.clear();
        pool.shutdown();
        QlhException.runtime(() -> pool.awaitTermination(1, TimeUnit.DAYS));
        return this;
    }

    public Report report() {
        return new Report(resultList);
    }

    public interface Runnable {
        boolean run();
    }

    public class Report {

        private long tp99;
        private long tp90;
        private long tp50;
        private long avg;
        private long total;
        private long success;
        private double rateOfSuccess;

        Report(List<Result> resultList) {
            Collections.sort(resultList, Comparator.comparing(Result::getTimeCost));
            tp99 = resultList.get((int) (resultList.size() * .99)).timeCost;
            tp90 = resultList.get((int) (resultList.size() * .9)).timeCost;
            tp50 = resultList.get((int) (resultList.size() * .5)).timeCost;
            total = resultList.size();
            success = resultList.stream().filter(e -> e.success).count();
            rateOfSuccess = new Double(success) / total;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("----- Load Test Report -----\n");
            builder.append("args: ")
                    .append("concurrent ").append(concurrent)
                    .append("\n");
            builder.append("----------------------------\n");

            builder.append("total: ").append(total).append("\n");
            builder.append("success: ").append(success).append("\n");
            builder.append("rate of success: ").append(rateOfSuccess).append("\n");
            builder.append("tp99: ").append(tp99).append("ms").append("\n");
            builder.append("tp90: ").append(tp90).append("ms").append("\n");
            builder.append("tp50: ").append(tp50).append("ms").append("\n");
            return builder.toString();
        }
    }

    @Getter
    public class Result {
        public boolean success;
        private long timeCost;
    }
}
