package com.qlh.base;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class QlhConcurrent {
    /**
     * 缓存线程变量
     */
    private static final ThreadLocal threadLocal = new ThreadLocal();
    /**
     * 管理单例对象
     */
    private static final Map<String, Object> singletons = new ConcurrentHashMap<>();

    public static void set(Object o) {
        threadLocal.set(o);
    }

    public static Object get() {
        return threadLocal.get();
    }

    public static <T> T getSingletonObject(Class<T> clazz, ObjectCreator creator) {
        return getSingletonObject(clazz.getName(), clazz, creator);
    }

    public static <T> T getSingletonObject(String objectName, Class<T> clazz, ObjectCreator creator) {
        objectName = clazz.getName() + ":" + objectName;
        T t = (T) singletons.get(objectName);
        if (t == null) {
            synchronized (singletons) {
                t = (T) singletons.get(objectName);
                if (t == null) {
                    t = (T) creator.create();
                    singletons.put(objectName, t);
                    log.info("create new object to pool: {}, total objects in pool: {}", objectName, singletons.size());
                }
            }
        }
        return t;
    }

    public static void sleep(long ms) {
        QlhException.runtime(() -> Thread.sleep(ms));
    }

    public interface ObjectCreator<T> {
        T create();
    }

    public static void asyncDaemon(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public static ThreadPoolExecutor newThreadPool(int nThread) {
        return new ThreadPoolExecutor(nThread, nThread,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue());
    }

    public static ThreadPoolExecutor newThreadPool(int nThread, int maxQueue) {
        return new ThreadPool(nThread, nThread,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(maxQueue));
    }

    static class ThreadPool extends ThreadPoolExecutor {

        public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public Future<?> submit(Runnable task) {
            try {
                return super.submit(task);
            } catch (RejectedExecutionException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

}
