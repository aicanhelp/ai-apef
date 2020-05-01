package io.apef.repository.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class RedisMockServer {
    private final static Set<Integer> startedPorts = new ConcurrentSkipListSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                startedPorts.forEach(RedisMockServer::stopMockServer)));
    }

    public static synchronized void startMockServer(int port) {
        if (startedPorts.contains(port)) {
            log.warn("Mock redis is started on port: " + port);
            return;
        }
        stopMockServer(port);
        int retried = 0;
        while (retried++ < 5) {
            try {
                RedisServer redisServer = new RedisServer(port);
                redisServer.start();
                startedPorts.add(port);
            } catch (Exception ex) {
                log.warn("Failed to start Redis Mock Server on port:" + port, ex);
            }
            if (!StringUtils.isEmpty(redisProcessId(port))) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (Exception ex) {

            }
        }
        throw new RuntimeException("Failed to start redis Mock Server on port " + port + ": Already retry 5 times, please check the system");
    }

    public static synchronized void stopMockServer(int port) {
        String redisId = redisProcessId(port);
        if (StringUtils.isEmpty(redisId)) return;
        ProcessBuilder pb = new ProcessBuilder("kill", "-9", redisId);
        try {
            pb.start();
            startedPorts.remove(port);
        } catch (Exception ex) {

        }
    }

    public static String redisProcessId(int port) {
        ProcessBuilder pb = new ProcessBuilder("pgrep", "-f","redis.*" + port);
        try {
            Process queryProcess = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(queryProcess.getInputStream()));
            return reader.readLine();
        } catch (Exception ex) {
            log.warn("Failed to get Redis Process Id on port:" + port , ex);
        }
        return null;
    }
}
