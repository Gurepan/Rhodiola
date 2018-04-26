package info.ilambda.rhodiola.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ClassUtils {
    private ClassUtils() {

    }

    private static final Logger logger = Logger.getLogger(ClassUtils.class.getName());
    private static String subClassPath = "/".equals(File.separator) ? getDefaultClassLoader().getResource("").getPath().substring(1) : getDefaultClassLoader().getResource("").getPath().substring(1).replace("/", File.separator);

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = ClassUtils.class.getClassLoader();
        }

        return cl;
    }

    public static List<Class> scanPackage(String packageName) {
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        packageName = packageName.lastIndexOf(".") != packageName.length() - 1 ? packageName + "" : packageName;
        List<Class> classList = new ArrayList<>();
        for (String path : paths) {
            if (path.indexOf("\\jre\\lib\\") > -1) {
                continue;
            }
            findFiles(new File(path), packageName, a -> true, classList);
        }
        return classList;
    }

    public static <T extends Annotation> List<Class> scanPackage(Class<T> anno) {
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        List<Class> classList = new LinkedList<>();
        for (String path : paths) {
            if (path.indexOf("\\jre\\lib\\") > -1) {
                continue;
            }
            findFiles(new File(path), "", a -> a.isAnnotationPresent(anno), classList);
        }
        return classList;
    }

    private static void findFiles(File file, String packageName, Function<Class, Boolean> classFilter, List<Class> classes) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                findFiles(f, packageName, classFilter, classes);
            }
        } else if (file.getName().endsWith(".class")) {
            processClassFile(file, packageName, classFilter, classes);
        } else if (file.getName().endsWith(".jar")) {
            processJarFile(file, packageName, classFilter, classes);
        }
    }

    private static void processClassFile(File file, String packageName, Function<Class, Boolean> classFilter, List<Class> classes) {
        String filePathWithDot;
        if (packageName.isEmpty()) {
            int i = file.getAbsolutePath().indexOf(subClassPath);
            if (i > -1) {
                filePathWithDot = file.getAbsolutePath().substring(i + subClassPath.length()).replace(".class", "").replace(File.separator, ".");
            } else {
                filePathWithDot = file.getAbsolutePath().replace(".class", "").replace(File.separator, ".");
            }
        } else {
            filePathWithDot = file.getAbsolutePath().replace(".class", "").replace(File.separator, ".");
        }
        int subIndex;
        if ((subIndex = filePathWithDot.indexOf(packageName)) != -1) {
            final String className = filePathWithDot.substring(subIndex);
            addClass(classFilter, className, classes);
        }
    }

    private static void processJarFile(File file, String packageName, Function<Class, Boolean> classFilter, List<Class> classes) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.endsWith(".class")) {
                    final String className = jarEntry.getName().replace(".class", "").replace("/", ".");
                    if (className.indexOf(packageName) == 0) {
                        addClass(classFilter, className, classes);
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("解压" + file + "出错");
        }
    }

    private static void addClass(Function<Class, Boolean> classFilter, String className, List<Class> classes) {
        if (className.indexOf("$") > 0) {
            return;
        }
        Class aClass;
        try {
            aClass = getDefaultClassLoader().loadClass(className);
            if (classFilter.apply(aClass)) {
                classes.add(aClass);
            }
        } catch (ClassNotFoundException e) {
//            logger.warning("实例化类" + className + "失败");
            return;
        } catch (NoClassDefFoundError e) {
            return;
        } catch (ArrayStoreException e) {
            return;
        }
    }

    public static List<Class> getClass(Class a) {
        List<Class> classList = new LinkedList<>();
        classList.add(a);
        Class[] is = a.getInterfaces();
        for (Class aClass : is) {
            classList.add(aClass);
        }
        Class c = a.getSuperclass();
        while (c != null) {
            classList.add(c);
            c = c.getSuperclass();
        }
        return classList;
    }

    public static <T extends Annotation> List<Method> getAnnotationedMethod(Class aClass, Class<T> anno) {
        List<Method> methodList = new LinkedList<>();
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            T t = method.getAnnotation(anno);
            if (t != null) {
                methodList.add(method);
            }
        }
        return methodList;
    }

    public static <T extends Annotation> T getMethodAnnotation(Method method, Class<T> anno) {
        return method.getAnnotation(anno);
    }

    public static <T extends Annotation> T getClassAnnotation(Class aClass, Class<T> anno) {
        return (T) aClass.getAnnotation(anno);
    }
}
