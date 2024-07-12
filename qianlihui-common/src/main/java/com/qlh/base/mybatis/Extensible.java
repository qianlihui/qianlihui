package com.qlh.base.mybatis;

import com.qlh.base.QlhClass;
import com.qlh.base.QlhException;
import com.qlh.base.QlhJsonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Extensible<T> extends DomainBase {

    private Class<T> extClass;

    public Extensible() {
        extClass = (Class<T>) QlhClass.getParameterizedType(getClass()).getActualTypeArguments()[0];
    }

    @Column(comment = "扩展信息", len = 4096)
    private String ext;
    private T extension;

    public void setExt(String ext) {
        T t;
        if (StringUtils.isNotBlank(ext)) {
            t = QlhJsonUtils.toObject(ext, extClass);
        } else {
            t = QlhException.runtime(() -> extClass.newInstance());
        }
        setExtension(t);
    }

    public String getExt() {
        return extension == null ? "{}" : QlhJsonUtils.toJson(extension);
    }

    public T getExtension() {
        if (extension == null) {
            extension = QlhException.runtime(() -> extClass.newInstance());
        }
        return extension;
    }

    public <C> C convertExtension(Class<? extends C> clazz) {
        if (StringUtils.isNotBlank(getExt())) {
            return QlhJsonUtils.toObject(getExt(), clazz);
        }
        return QlhJsonUtils.toObject("{}", clazz);
    }

}
