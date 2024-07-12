package com.qlh.base;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class QlhOperationSystem {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }

    public static String getTempPath() {
        if (isWindows()) {
            return "c:/tmp";
        } else {
            return "/tmp";
        }
    }

    public static List<String> getIPlist() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        if (Pattern.matches("\\d+\\.\\d+\\.\\d+\\.\\d+", ipAddress)) {
                            ips.add(ipAddress);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取IP地址异常", e);
        }
        return ips;
    }

    public static String getHostName() {
        return QlhException.ignore(() -> Inet4Address.getLocalHost().getHostName());
    }
}
