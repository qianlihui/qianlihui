package com.qlh.base;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QlhGit {

    private static QlhMap git = new QlhMap();

    static {
        try {
            String json = QlhIoUtils.readAsString(QlhGit.class.getResourceAsStream("/git.properties"));
            git.putAll(QlhJsonUtils.toObject(json, QlhMap.class));
        } catch (Exception e) {
            log.error("读取Git信息失败", e);
        }
    }

    public static String getCommitId() {
        return git.getString("git.commit.id", "");
    }

}
