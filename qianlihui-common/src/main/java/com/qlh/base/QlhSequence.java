package com.qlh.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 编号生成
 */
@Slf4j
public class QlhSequence {

    private static final Map<String, QlhSequence> sequences = new ConcurrentHashMap<>(); // 根据前缀不同使用不同的实例
    private static final String ZERO = "000000000000000000000000000000000000000000";
    private static int prefixIP;
    private final ReentrantLock locker = new ReentrantLock();
    private final Map<Long, AtomicInteger> indexOfSecond = new TreeMap<>();


    static {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        log.info("本机IP地址: {}", ipAddress);
                        try {
                            prefixIP = Integer.valueOf(ipAddress.substring(ipAddress.lastIndexOf('.') + 1));
                        } catch (Exception e) {

                        }
                    }
                }
            }
            log.info("ID前缀 {}", prefixIP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static QlhSequence getInstance() {
        return getInstance("");
    }

    private static QlhSequence getInstance(String prefix) {
        QlhSequence sequence = sequences.get(prefix);
        if (sequence == null) {
            sequences.putIfAbsent(prefix, new QlhSequence());
            sequence = sequences.get(prefix);
        }
        return sequence;
    }

    /**
     * 无前缀
     *
     * @return
     */
    public static String next() {
        return next("");
    }

    /**
     * 指定单号前缀
     *
     * @param prefix
     * @return
     */
    public static String next(String prefix) {
        return getInstance(prefix).next(prefix, 20);
    }

    /**
     * @param prefix
     * @param idLen  默认20, 时间戳占12位, IP地址1-3位
     * @return
     */
    public String next(String prefix, int idLen) {
        Date d = new Date();
        String id = prefix + QlhDateUtils.format(d, "yyMMddHHmmss") + prefixIP;
        String secondId = String.valueOf(getIndexOfCurrentSecond(d.getTime() / 1000));
        if (id.length() + secondId.length() > idLen) {
            return StringUtils.substring(id + secondId, 0, idLen);
        } else {
            return id + StringUtils.substring(ZERO, 0, idLen - id.length() - secondId.length()) + secondId;
        }
    }

    private int getIndexOfCurrentSecond(long second) {
        AtomicInteger integer = indexOfSecond.get(second);
        if (integer == null) {
            try {
                locker.lock();
                integer = indexOfSecond.get(second);
                if (integer == null) {
                    int size = 5;
                    if (indexOfSecond.size() > size) {
                        Iterator<Map.Entry<Long, AtomicInteger>> iterator = indexOfSecond.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<Long, AtomicInteger> next = iterator.next();
                            if (second - next.getKey() > size) {
                                iterator.remove();
                            }
                        }
                    }
                    integer = new AtomicInteger();
                    indexOfSecond.put(second, integer);
                }
            } finally {
                locker.unlock();
            }
        }
        return integer.incrementAndGet();
    }

}
