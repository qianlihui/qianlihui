package com.qlh.base;

import lombok.Data;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class QlhClass {

    private static Map<Class, ClassMetadata> classMetaDataMap = new ConcurrentHashMap<>();

    public static ClassMetadata getMetadata(Class clazz) {
        ClassMetadata classMetadata = classMetaDataMap.get(clazz);
        if (classMetadata == null) {
            classMetadata = new ClassMetadata();
            for (Method method : clazz.getMethods()) {
                classMetadata.methods.add(method);
            }
            for (Field field : clazz.getDeclaredFields()) {
                classMetadata.fields.add(field);
            }
            // 读取父类字段信息
            Class tc = clazz.getSuperclass();
            while (tc != null && tc != Object.class) {
                for (Field field : tc.getDeclaredFields()) {
                    classMetadata.parentFields.add(field);
                }
                tc = tc.getSuperclass();
            }
            classMetaDataMap.putIfAbsent(clazz, classMetadata);
        }
        return classMetadata;
    }

    public static List<Class<?>> scanPackage(String pack) {
        return QlhException.runtime(() -> {
            List<Class<?>> classes = new ArrayList<>();
            String packageName = pack;
            String packageDirName = packageName.replace('.', '/');
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.charAt(0) == '/') {
                            name = name.substring(1);
                        }
                        if (name.startsWith(packageDirName)) {
                            int idx = name.lastIndexOf('/');
                            if (idx != -1) {
                                packageName = name.substring(0, idx).replace('/', '.');
                            }
                            if ((idx != -1)) {
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(
                                            packageName.length() + 1, name.length() - 6);
                                    classes.add(Class.forName(packageName + '.' + className));
                                }
                            }
                        }
                    }
                }
            }
            return classes;
        });
    }

    public static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath,
                                                        List<Class<?>> classes) {
        QlhException.runtime(() -> {
            File dir = new File(packagePath);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            File[] dirFiles = dir.listFiles(file -> file.isDirectory() || (file.getName().endsWith(".class")));
            for (File file : dirFiles) {
                if (file.isDirectory()) {
                    findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                }
            }
        });
    }

    public static ParameterizedType getParameterizedType(Class clazz) {
        while (clazz != Object.class) {
            Type type = clazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                return (ParameterizedType) type;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static String toGetter(String property) {
        return "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    public static String toSetter(String property) {
        return "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    public static Object invokeGetter(Object target, String property) {
        return QlhException.runtime(() -> {
            Method m = getMetadata(target.getClass()).getMethod(toGetter(property));
            return m == null ? null : m.invoke(target);
        });
    }

    public static Object invokeSetter(Object target, String property, Object value) {
        return QlhException.runtime(() ->
                getMetadata(target.getClass()).getMethod(toSetter(property), value.getClass())
                        .invoke(target, value));
    }

    public static boolean isPrimaryType(Object obj) {
        return obj instanceof Number
                || obj instanceof String
                || obj instanceof Character
                ;
    }

    public static boolean isPrimaryTypeClass(Class clazz) {
        return Number.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz)
                ;
    }

    public static <T> T autoTransfer(String origin, Class target) {
        if (target == Double.class || target == double.class) {
            return (T) new Double(origin);
        } else if (target == Integer.class || target == int.class) {
            return (T) new Integer(origin);
        } else if (target == BigDecimal.class) {
            return (T) new BigDecimal(origin);
        } else if (target == Long.class || target == long.class) {
            return (T) new Long(origin);
        } else if (target == String.class) {
            return (T) origin;
        }
        return null;
    }

    @Data
    public static class ClassMetadata {
        List<Method> methods = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Field> parentFields = new ArrayList<>();

        public Field getFieldByName(String name) {
            return getFieldByName(name, false);
        }

        public Field getFieldByName(String name, boolean isIgnoreCase) {
            for (Field field : fields) {
                if (isIgnoreCase) {
                    if (field.getName().equalsIgnoreCase(name))
                        return field;
                } else {
                    if (field.getName().equals(name))
                        return field;
                }
            }
            return null;
        }

        public Method getMethod(String nameIgnoreCase, Class... parameterTypes) {
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(nameIgnoreCase) && method.getParameterCount() == parameterTypes.length) {
                    boolean isMatch = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (parameterTypes[i] == method.getParameterTypes()[i] || method.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) {

                        } else {
                            isMatch = false;
                        }
                    }
                    if (isMatch) {
                        return method;
                    }
                }
            }
            return null;
        }

        public List<Field> getFields(boolean includeParent) {
            if (includeParent) {
                List<Field> list = new ArrayList<>(fields.size() + parentFields.size());
                list.addAll(fields);
                list.addAll(parentFields);
                return list;
            }
            return fields;
        }
    }

}
