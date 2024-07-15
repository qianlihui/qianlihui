package com.qlh.base;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class QlhXmlUtils {

    public static String toXml(String rootTag, Object obj) {
        return toXml(rootTag, obj, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    public static String toXml(String rootTag, Object obj, String header) {
        StringBuilder builder = new StringBuilder(header);
        builder.append("<" + rootTag + ">");
        for (Field field : QlhClass.getMetadata(obj.getClass()).getFields(false)) {
            String tagName = field.getName();
            Column column = field.getAnnotation(Column.class);
            if (column != null && StringUtils.isNotBlank(column.name())) {
                tagName = column.name();
            }
            Object o = QlhClass.invokeGetter(obj, field.getName());
            if (o == null) {
                builder.append(String.format("<%s></%s>", tagName, tagName));
            } else if (QlhClass.isPrimaryType(o)) {
                builder.append(String.format("<%s>%s</%s>", tagName, wrapperXml(QlhClass.invokeGetter(obj, field.getName())), tagName));
            } else if (o instanceof Date) {
                builder.append(String.format("<%s>%s</%s>", tagName, ((Date) o).getTime(), tagName));
            } else if (o instanceof Collection && column != null && StringUtils.isNotBlank(column.elementName())) {
                builder.append("<" + tagName + ">");
                for (Object t : ((Collection) o)) {
                    builder.append(toXml(column.elementName(), t, ""));
                }
                builder.append("</" + tagName + ">");
            } else if (o.getClass().isArray() && column != null && StringUtils.isNotBlank(column.elementName())) {
                builder.append("<" + tagName + ">");
                Object[] arr = ((Object[]) o);
                for (int i = 0; i < arr.length; i++) {
                    builder.append(toXml(column.elementName(), arr[i], ""));
                }
                builder.append("</" + tagName + ">");
            } else {
                builder.append(toXml(tagName, o, ""));
            }
        }
        builder.append("</" + rootTag + ">");
        return builder.toString();
    }

    private static String wrapperXml(Object val) {
        return val == null ? "" : val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static <T> T toObject(String xml, Class<T> clazz) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return toObject(document.getDocumentElement(), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(Node node, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        T o = clazz.newInstance();
        NodeList childNodes = node.getChildNodes();
        Map<String, Node> nodeMap = new HashMap<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node tNode = childNodes.item(i);
            nodeMap.put(tNode.getNodeName(), tNode);
        }
        for (Field field : QlhClass.getMetadata(clazz).getFields(false)) {
            String tagName = field.getName();
            Column column = field.getAnnotation(Column.class);
            if (column != null && StringUtils.isNotBlank(column.name())) {
                tagName = column.name();
            }
            Node propNode = nodeMap.get(tagName);
            if (propNode == null) {
                // nothing
            } else if (QlhClass.isPrimaryTypeClass(field.getType())) {
                QlhClass.invokeSetter(o, field.getName(), QlhClass.autoTransfer(propNode.getTextContent(), field.getType()));
            } else if (field.getType() == Date.class) {
                QlhClass.invokeSetter(o, field.getName(), new Date(Long.parseLong(propNode.getTextContent())));
            } else if (Collection.class.isAssignableFrom(field.getType()) && column != null && StringUtils.isNotBlank(column.elementName())) {
                Collection collection;
                if (field.getType() == List.class) {
                    collection = new ArrayList();
                } else {
                    collection = (Collection) field.getType().newInstance();
                }
                NodeList propNodeChildren = propNode.getChildNodes();
                for (int i = 0; i < propNodeChildren.getLength(); i++) {
                    collection.add(toObject(propNodeChildren.item(i), (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]));
                }
                QlhClass.invokeSetter(o, field.getName(), collection);
            } else if (o.getClass().isArray() && column != null && StringUtils.isNotBlank(column.elementName())) {
                // builder.append("<" + tagName + ">");
                // for (Object t : ((Collection) o)) {
                //    builder.append(toXml(column.elementName(), t, ""));
                // }
                // builder.append("</" + tagName + ">");
            } else {
                QlhClass.invokeSetter(o, field.getName(), toObject(propNode, field.getType()));
            }
        }
        return o;
    }


    public static void main(String args[]) {
        C c = new C();
        c.setCObject(new C());
        c.setCObjects(Arrays.asList(new C(), new C()));
        String xml = toXml("Root", c);
        log.info(xml);
        log.info(QlhJsonUtils.toFormattedJson(toObject(xml, C.class)));
    }


    @Data
    static class C {
        private String a = "aValue";
        private String b = "bValue";
        private BigDecimal c = BigDecimal.ZERO;
        private Date date = new Date();
        private C cObject;
        @Column(elementName = "Element")
        private List<C> cObjects;
    }
}
