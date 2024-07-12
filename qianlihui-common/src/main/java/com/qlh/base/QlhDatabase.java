package com.qlh.base;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Data
@Accessors(chain = true)
public class QlhDatabase {

    protected String host;
    protected String port;
    protected String dbName;
    protected String params;
    protected String userName;
    protected String password;
    protected String jdbcUrl;
    protected String driverClass;

    protected DataSource dataSource;

    public QlhDatabase() {
        if (QlhStringUtils.isNotBlank(driverClass)) {
            QlhException.runtime(() -> Class.forName(driverClass));
        }
    }

    public QlhDatabase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static List<Map<String, Object>> toList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            list.add(map);
        }
        return list;
    }

    public Connection getConn() {
        try {
            return dataSource != null ? dataSource.getConnection() : DriverManager.getConnection(
                    StringUtils.isNotBlank(jdbcUrl) ? jdbcUrl : String.format("jdbc:mysql://%s:%s/%s?%s", host, port, dbName, params)
                    , userName, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int update(String sql) {
        try {
            try (Connection conn = getConn(); Statement st = conn.createStatement()) {
                int count = st.executeUpdate(sql);
                log.info("影响行数: {}, SQL: {}", count, sql);
                return count;
            }
        } catch (Exception e) {
            log.info("SQL: {}", sql);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> select(String sql, Class<T> clazz) {
        return select(sql).stream()
                .map(e -> QlhJsonUtils.toObject(QlhJsonUtils.toJson(e), clazz))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> select(String sql) {
        log.info("查询SQL: {}", sql);
        return QlhException.runtime(() -> {
            try (Connection conn = getConn();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                return toList(rs);
            }
        });
    }
}
