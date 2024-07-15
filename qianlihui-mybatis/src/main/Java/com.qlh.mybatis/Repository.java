package com.qlh.mybatis;

import com.qlh.base.QlhClass;
import com.qlh.base.QlhException;
import com.qlh.base.QlhJsonUtils;
import lombok.Data;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 通用实体对象操作类
 */
@Data
public class Repository {

    private SqlSessionTemplate sqlSessionTemplate;

    public void insert(Object entity) {
        sqlSessionTemplate.insert(entity.getClass().getSimpleName() + ".insert", entity);
    }

    public void insert(String statement, List<Object> entities) {
        if (CollectionUtils.isEmpty(entities))
            return;
        Object first = entities.get(0);
        for (Object o : entities) {
            if (o == null || o.getClass() != first.getClass()) {
                throw new QlhException(0, "数据不可为空且类型要一致");
            }
        }
        sqlSessionTemplate.insert(wrapStatement(first.getClass(), statement), entities);
    }

    public int update(Object entity) {
        return sqlSessionTemplate.update(entity.getClass().getSimpleName() + ".update", entity);
    }

    public int update(String statement, Object entity) {
        return sqlSessionTemplate.update(wrapStatement(entity.getClass(), statement), entity);
    }

    public <T> List<T> selectList(Class<T> clazz, Object param) {
        return sqlSessionTemplate.selectList(clazz.getSimpleName() + ".list", convertParam(param));
    }

    public <T> List<T> selectList(Class<T> clazz, String statement, Object param) {
        return sqlSessionTemplate.selectList(wrapStatement(clazz, statement), convertParam(param));
    }

    public <T> T selectOne(Class<T> clazz, Object param) {
        List<T> list = selectList(clazz, param);
        if (list.size() > 1) {
            throw new RuntimeException("returned more than one row");
        }
        return list.stream().findAny().orElse(null);
    }

    public <T> T selectOne(String statement, Object param) {
        List<T> list = selectList(null, statement, param);
        if (list.size() > 1) {
            throw new RuntimeException("returned more than one row");
        }
        return list.stream().findAny().orElse(null);
    }

    public <T> T selectOne(Class<T> clazz, String statement, Object param) {
        List<T> list = selectList(clazz, wrapStatement(clazz, statement), param);
        if (list.size() > 1) {
            throw new RuntimeException("returned more than one row");
        }
        return list.stream().findAny().orElse(null);
    }

    private String wrapStatement(Class clazz, String statement) {
        return statement.contains(".") ? statement : (clazz.getSimpleName() + "." + statement);
    }

    private Object convertParam(Object param) {
        if (QlhClass.isPrimaryType(param))
            return param;
        return param instanceof Map ? (Map) param : QlhJsonUtils.convert(param, Map.class);
    }
}
