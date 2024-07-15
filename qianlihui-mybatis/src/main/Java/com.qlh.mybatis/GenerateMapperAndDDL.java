package com.qlh.mybatis;

import com.qlh.base.Column;
import com.qlh.base.QlhIoUtils;
import com.qlh.base.QlhOperationSystem;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerateMapperAndDDL {

    public static void create(Class clazz) throws Exception {
        String tableName = "t_" + toUnderscore(clazz.getSimpleName()).replace("_entity", "");
        String mapperXML;
        try (InputStream stream = GenerateMapperAndDDL.class.getResourceAsStream("/MapperTemplate.xml")) {
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
        createSQL.append("id bigint auto_increment not null comment '主键',").append("\n");
        if (allProperties.contains("version"))
            createSQL.append("version int not null default 0 comment '乐观锁',").append("\n");
        if (allProperties.contains("createTime"))
            createSQL.append("create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',").append("\n");
        if (allProperties.contains("updateTime"))
            createSQL.append("update_time timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '更新时间',").append("\n");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (field.getAnnotation(Column.class) != null) {
                createSQL.append(toUnderscore(field.getName())
                        + " " + toMySQLType(field)).append(",\n");
                alterSQL.append("alter table " + tableName + " add " + toUnderscore(field.getName()) + " " + toMySQLType(field)).append(";\n");
            }
        }
        createSQL.append("primary key (id)").append("\n");

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
                createSQL.append(String.format(",unique index u_idx_%s (%s)\n", i++, StringUtils.join(indexColumns, ",")));
            }
        }


        Table table = (Table) clazz.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.comment())) {
            createSQL.append(") charset utf8mb4 comment '").append(table.comment()).append("';");
        } else {
            createSQL.append(") charset utf8mb4 comment '';");
        }

        String prefix = "\t\t\t";

        mapperXML = mapperXML.replace("${mapperClass}", clazz.getSimpleName());
        mapperXML = mapperXML.replace("${tableName}", tableName);
        mapperXML = mapperXML.replace("${beanClass}", clazz.getName());
        mapperXML = mapperXML.replace("${columns}", StringUtils.join(columns, ",\n" + prefix));
        mapperXML = mapperXML.replace("${values}", StringUtils.join(columnPlaceHolders, ",\n" + prefix));
        mapperXML = mapperXML.replace("${updateColumns}", StringUtils.join(updateColumns, "\n" + prefix));
        mapperXML = mapperXML.replace("${{whereCond}}", whereSQL);

        if (table != null && table.updateOnDup()) {
            List list = new ArrayList();
            for (int i = 0; i < updateColumns.size(); i++) {
                String c = updateColumns.get(i);
                Pattern pattern = Pattern.compile("\\#\\{(.+)\\}");
                Matcher matcher = pattern.matcher(c);
                if (matcher.find()) {
                    String f = matcher.group(1);
                    list.add(String.format("<if test=\"%s != null\">,%s</if>", f, toUnderscore(f) + "=" + "#{" + f + "}"));
                } else {
                    throw new RuntimeException("");
                }

            }
            mapperXML = mapperXML.replace("${updateOnDuplicate}",
                    "ON DUPLICATE KEY UPDATE id=id " + "\n" + prefix + StringUtils.join(list, "\n" + prefix));

        } else {
            mapperXML = mapperXML.replace("${updateOnDuplicate}", "");
        }

        String xmlPath = QlhOperationSystem.getTempPath() + "/" + clazz.getSimpleName() + ".xml";
        String ddlPath = QlhOperationSystem.getTempPath() + "/" + tableName + ".sql";
        QlhIoUtils.writeToFile(mapperXML.getBytes(), new File(xmlPath));
        QlhIoUtils.writeToFile((createSQL.toString() + alterSQL).getBytes("UTF-8"), new File(ddlPath));

        if (QlhOperationSystem.isWindows()) {
            try {
                Runtime.getRuntime().exec("cmd /k " + xmlPath);
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
            int len = annotation == null ? 32 : annotation.len();
            if (len <= 4096) {
                columnDDL = " varchar(" + len + ") not null default '' ";
            } else if (len <= 65535) {
                columnDDL = " text ";
            } else if (len <= 16777215) {
                columnDDL = " MediumText ";
            } else {
                columnDDL = " LongText  ";
            }
        } else if (clazz == int.class || clazz == Integer.class) {
            columnDDL = " int(11) not null default 0 ";
        } else if (clazz == double.class || clazz == Double.class) {
            columnDDL = " decimal(20,10) not null default 0 ";
        } else if (clazz == long.class || clazz == Long.class) {
            columnDDL = " bigint(20) not null default 0 ";
        } else if (clazz == BigDecimal.class) {
            int scale = annotation == null ? 4 : annotation.scale();
            columnDDL = " decimal(20," + scale + ") not null default 0 ";
        } else if (clazz == Date.class) {
            columnDDL = " datetime  ";
        } else {
            throw new RuntimeException("unknown type " + field.getName() + ":" + clazz);
        }
        columnDDL = columnDDL + " comment '" + (annotation != null ? annotation.comment() : "") + "'";
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
