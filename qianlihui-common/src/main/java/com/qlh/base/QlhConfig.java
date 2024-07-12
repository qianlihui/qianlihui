package com.qlh.base;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

public class QlhConfig extends QlhMap implements ApplicationListener {

    private QlhConfig() {
    }

    public static String getAppCode() {
        return getInstance().getString("app.code", "");
    }

    public static QlhConfig getInstance() {
        QlhConfig config = QlhConcurrent.getSingletonObject(QlhConfig.class, () -> {
            QlhConfig INSTANCE = new QlhConfig();
            QlhException.runtime(() -> {
                Properties properties = new Properties();
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                for (Resource resource : resolver.getResources("classpath*:config/*.properties")) {
                    properties.load(resource.getInputStream());
                }
                // 从环境变量加载配置
                Object appCode = properties.get("app.code");
                if (appCode != null) {
                    String path = System.getenv("ENV_APPCODE_" + appCode);
                    if (StringUtils.isBlank(path) && String.valueOf(appCode).contains("-")) {
                        // Linux系统环境变量名称不能包含 ‘-’，此处兼容自动将 - 替换为 _
                        path = System.getenv("ENV_APPCODE_" + appCode.toString().replace("-", "_"));
                    }
                    if (StringUtils.isBlank(path)
                            && QlhOperationSystem.isLinux()) {
                        path = "/opt/cfg/" + appCode;
                    }
                    if (StringUtils.isBlank(path) || !new File(path).exists()) {
                        throw new RuntimeException("configuration not found " + path);
                    }
                    Arrays.stream(new File(path).listFiles()).forEach(file -> {
                        if (file.getName().endsWith(".properties")) {
                            QlhException.runtime(() -> properties.load(new FileInputStream(file)));
                        }
                    });
                }
                INSTANCE.putAll(properties);
            });
            return INSTANCE;
        });

        QlhException.runtime(() -> {

        });

        return config;
    }

    /* 加载配置文件到 spring */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ApplicationEnvironmentPreparedEvent envEvent = (ApplicationEnvironmentPreparedEvent) event;
            envEvent.getEnvironment().getPropertySources().addLast(new PropertySourceInner(this.getClass().getSimpleName()));
        }
    }

    class PropertySourceInner extends PropertySource {

        public PropertySourceInner(String name) {
            super(name);
        }

        @Override
        public Object getProperty(String key) {
            return getInstance().getString(key);
        }
    }
}
