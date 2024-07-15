package com.qlh.mybatis;

import com.qlh.base.Column;
import com.qlh.base.QlhOperationSystem;
import com.qlh.base.QlhIoUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CassGenerateMapperAndDDL {

    public static void create(Class clazz) throws Exception {
        String tableName = "t_" + toUnderscore(clazz.getSimpleName()).replace("_entity", "");
        String mapperXML;
        try (InputStream stream = CassGenerateMapperAndDDL.class.getResourceAsStream("/MapperTemplate.xml")) {
            mapperXML = QlhIoUtils.readAsString(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Field> fields = new ArrayList();
        Set<String> allProperties = new HashSet<>();

        List<Class> classes = new ArrayList<>();
        Class tc = clazz;
        do {
            classes.add(tc);
            tc = tc.getSuperclass();
        } while (tc != Object.class);

        for (Class c : classes) {
            if (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                        continue;
                    }
                    if (!f.isAnnotationPresent(Column.class)) {
                        continue;
                    }
                    Column column = f.getAnnotation(Column.class);
                    if (!column.cassandra()) {
                        continue;
                    }
                    fields.add(f);
                }
            }
        }

        fields = fields.stream().filter(e -> {
            allProperties.add(e.getName());
            return !e.getName().equals("id") && !e.getName().equals("version") && !e.getName().equals("createTime") && !e.getName().equals("updateTime");
        }).collect(Collectors.toList());

        List<String> columns = new ArrayList<>();
        List<String> updateColumns = new ArrayList<>();
        List<String> columnPlaceHolders = new ArrayList<>();
        for (Field field : fields) {
            columns.add(toUnderscore(field.getName()));
            columnPlaceHolders.add("#{" + field.getName() + "}");
            updateColumns.add(
                    "<if test=\"" + field.getName() + " != null\">" +
                            (toUnderscore(field.getName()) + " = " + "#{" + field.getName() + "}") +
                            ",</if>"
            );
        }

        StringBuilder createSQL = new StringBuilder();
        StringBuilder whereSQL = new StringBuilder();
        StringBuilder alterSQL = new StringBuilder("\n\n");
        createSQL.append("drop table " + tableName + ";\n\n\n\n");
        createSQL.append("create table " + tableName + "(").append("\n");
        if (allProperties.contains("version"))
            createSQL.append("version int,").append("\n");
        if (allProperties.contains("createTime"))
            createSQL.append("create_time timestamp,").append("\n");
        if (allProperties.contains("updateTime"))
            createSQL.append("update_time timestamp,").append("\n");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (field.getAnnotation(Column.class) != null) {
                createSQL.append(toUnderscore(field.getName())
                        + " " + toMySQLType(field)).append(",\n");
                alterSQL.append("alter table " + tableName + " add " + toUnderscore(field.getName()) + " " + toMySQLType(field)).append(";\n");
            }
        }

        Index index = (Index) clazz.getAnnotation(Index.class);
        if (index != null) {
            String[] indexes = index.value();
            int i = 0;
            for (String s : indexes) {
                String[] indexColumns = s.split(",");
                for (int m = 0; m < indexColumns.length; m++) {
                    whereSQL.append(String.format("\t\t\t<if test=\"%s != null\">AND ", indexColumns[m]))
                            .append(toUnderscore(indexColumns[m]) + "=#{" + indexColumns[m] + "}")
                            .append("</if>\n");
                    indexColumns[m] = toUnderscore(indexColumns[m]);
                }
                createSQL.append(String.format(",index idx_%s (%s)\n", i++, StringUtils.join(indexColumns, ",")));
            }
        }

        UniqueIndex uniqueIndex = (UniqueIndex) clazz.getAnnotation(UniqueIndex.class);
        if (uniqueIndex != null) {
            String[] uniqueIndexes = uniqueIndex.value();
            int i = 0;
            for (String s : uniqueIndexes) {
                String[] indexColumns = s.split(",");
                for (int m = 0; m < indexColumns.length; m++) {
                    whereSQL.append(String.format("\t\t\t<if test=\"%s != null\">AND ", indexColumns[m]))
                            .append(toUnderscore(indexColumns[m]) + "=#{" + indexColumns[m] + "}")
                            .append("</if>\n");
                    indexColumns[m] = toUnderscore(indexColumns[m]);
                }
                createSQL.append(String.format("primary key (%s)\n", StringUtils.join(indexColumns, ",")));
            }
        }

        createSQL.append(");");

        String prefix = "\t\t\t";

        mapperXML = mapperXML.replace("${mapperClass}", clazz.getSimpleName());
        mapperXML = mapperXML.replace("${tableName}", tableName);
        mapperXML = mapperXML.replace("${beanClass}", clazz.getName());
        mapperXML = mapperXML.replace("${columns}", StringUtils.join(columns, ",\n" + prefix));
        mapperXML = mapperXML.replace("${values}", StringUtils.join(columnPlaceHolders, ",\n" + prefix));
        mapperXML = mapperXML.replace("${updateColumns}", StringUtils.join(updateColumns, "\n" + prefix));
        mapperXML = mapperXML.replace("${{whereCond}}", whereSQL);

        String xmlPath = QlhOperationSystem.getTempPath() + "/" + clazz.getSimpleName() + ".xml";
        String ddlPath = QlhOperationSystem.getTempPath() + "/" + tableName + ".sql";
        QlhIoUtils.writeToFile(mapperXML.getBytes(), new File(xmlPath));
        QlhIoUtils.writeToFile((createSQL.toString() + alterSQL).getBytes("UTF-8"), new File(ddlPath));

        if (QlhOperationSystem.isWindows()) {
            try {
//                Runtime.getRuntime().exec("cmd /k " + xmlPath);
                Runtime.getRuntime().exec("cmd /k " + ddlPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static String toMySQLType(Field field) {
        Class clazz = field.getType();
        Column annotation = field.getAnnotation(Column.class);
        String columnDDL;
        if (clazz == String.class) {
            columnDDL = " text ";
        } else if (clazz == int.class || clazz == Integer.class) {
            columnDDL = " int ";
        } else if (clazz == double.class || clazz == Double.class) {
            columnDDL = " decimal ";
        } else if (clazz == long.class || clazz == Long.class) {
            columnDDL = " bigint ";
        } else if (clazz == BigDecimal.class) {
            int scale = annotation == null ? 4 : annotation.scale();
            columnDDL = " decimal ";
        } else if (clazz == Date.class) {
            columnDDL = " timestamp  ";
        } else {
            throw new RuntimeException("unknown type " + field.getName() + ":" + clazz);
        }
        return columnDDL;
    }

    public static String toUnderscore(String s) {

        char name[] = s.toCharArray();
        for (int i = 0; i < name.length; i++) {
            if (name[i] >= 'A' && name[i] <= 'Z') {
                int index = i;
                while ((index + 1) < name.length && name[index + 1] >= 'A' && name[index + 1] <= 'Z')
                    index++;
                if (index != name.length - 1) {
                    index--;
                }
                if (index > i) {
                    for (int replaceI = i + 1; replaceI <= index; replaceI++) {
                        name[replaceI] = (char) (name[replaceI] + 32);
                    }
                }
            }
        }
        s = new String(name);

        StringBuilder builder = new StringBuilder();
        char[] arr = s.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            if (ch >= 'A' && ch <= 'Z') {
                if (i == 0) {
                    builder.append((char) (ch + 32));
                } else {
                    builder.append('_').append((char) (ch + 32));
                }
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }


    public static void main(String args[]) {
        System.out.println(toUnderscore("name"));
        System.out.println(toUnderscore("userName"));
        System.out.println(toUnderscore("USERId"));
        System.out.println(toUnderscore("userID"));
    }
}
