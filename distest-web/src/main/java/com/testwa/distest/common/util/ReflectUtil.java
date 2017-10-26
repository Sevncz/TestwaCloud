package com.testwa.distest.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 通用反射工具类
 *
 * @author Cat73
 */
public class ReflectUtil {
    // - 搜索
    /**
     * 搜索属性
     *
     * @param class_ 属性所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param fieldName 属性名
     * @return 找到的属性，如果没找到则返回 null
     */
    public static Field searchField(Class<?> class_, final String fieldName) throws Exception {
        Field field = null;
        while (true) {
            try {
                field = class_.getDeclaredField(fieldName);
                return field;
            } catch (final NoSuchFieldException e) {
                class_ = class_.getSuperclass();
                if (class_ == Object.class || class_ == null) {
                    throw e;
                }
            }
        }
    }

    /**
     * 搜索方法
     *
     * @param class_ 方法所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param methodName 方法名
     * @param argTypes 参数的类型列表
     * @return 找到的方法，如果没找到则返回 null
     * @throws Exception
     */
    public static Method searchMethod(Class<?> class_, final String methodName, final Class<?>[] argTypes) throws Exception {
        // 查找方法
        Method method = null;
        while (true) {
            try {
                method = class_.getDeclaredMethod(methodName, argTypes);
                return method;
            } catch (final NoSuchMethodException e) {
                class_ = class_.getSuperclass();
                if (class_ == Object.class || class_ == null) {
                    throw e;
                }
            }
        }
    }

    // - 属性
    /**
     * 获取一个属性的值
     *
     * @param class_ 属性所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param object 属性所在的对象, 如果是静态属性, 则忽略此参数
     * @param fieldName 属性名
     * @return 该属性的值
     * @throws Exception
     */
    public static Object getFieldValue(final Class<?> class_, final Object object, final String fieldName) throws Exception {
        // 查找属性
        final Field field = ReflectUtil.searchField(class_, fieldName);

        // 保存原访问权限
        final boolean accessible = field.isAccessible();
        // 设置允许通过反射访问
        if (!accessible) {
            field.setAccessible(true);
        }
        // 获取值
        final Object result = field.get(object);
        // 恢复原访问权限
        if (!accessible) {
            field.setAccessible(false);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取一个属性的值
     *
     * @param object 属性所在的对象
     * @param fieldName 属性名
     * @return 该属性的值
     * @throws Exception
     */
    public static Object getFieldValue(final Object object, final String fieldName) throws Exception {
        return ReflectUtil.getFieldValue(object.getClass(), object, fieldName);
    }

    /**
     * 设置一个属性的值
     *
     * @param class_ 属性所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param object 属性所在的对象, 如果是静态属性, 则忽略此参数
     * @param fieldName 属性名
     * @param value 目标值
     * @throws Exception
     */
    public static void setFieldValue(final Class<?> class_, final Object object, final String fieldName, final Object value) throws Exception {
        // 查找属性
        final Field field = ReflectUtil.searchField(class_, fieldName);
        // 保存原访问权限
        final boolean accessible = field.isAccessible();
        // 设置允许通过反射访问
        if (!accessible) {
            field.setAccessible(true);
        }
        // 设置值
        field.set(object, value);
        // 恢复原访问权限
        if (!accessible) {
            field.setAccessible(false);
        }
    }

    /**
     * 设置一个属性的值
     *
     * @param object 属性所在的对象
     * @param fieldName 属性名
     * @param value 目标值
     * @throws Exception
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value) throws Exception {
        ReflectUtil.setFieldValue(object.getClass(), object, fieldName, value);
    }

    // - 方法
    /**
     * 调用一个方法
     *
     * @param class_ 方法所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param object 方法所在的对象, 如果是静态方法, 则忽略此参数
     * @param methodName 方法名
     * @param args 参数列表
     * @param argTypes 参数的类型列表
     * @return 方法的返回值
     * @throws Exception
     */
    public static Object invokeMethodLimitArgsTypes(final Class<?> class_, final Object object, final String methodName, final Object[] args, final Class<?>[] argTypes) throws Exception {
        // 查找方法
        final Method method = ReflectUtil.searchMethod(class_, methodName, argTypes);
        // 保存原访问权限
        final boolean accessible = method.isAccessible();
        // 设置允许通过反射访问
        if (!accessible) {
            method.setAccessible(true);
        }
        // 调用方法
        final Object result = method.invoke(object, args);
        // 恢复原访问权限
        if (!accessible) {
            method.setAccessible(false);
        }
        // 返回结果
        return result;
    }

    /**
     * 调用一个方法
     *
     * @param class_ 方法所在的 Class，会从这个 Class 一级一级往上查，直到 Object(不包含)
     * @param object 方法所在的对象, 如果是静态方法, 则忽略此参数
     * @param methodName 方法名
     * @param args 参数列表
     * @return 方法的返回值
     * @throws Exception
     */
    public static Object invokeMethod(final Class<?> class_, final Object object, final String methodName, final Object... args) throws Exception {
        final Class<?>[] parameterTypes = new Class<?>[args.length];
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
        }
        return ReflectUtil.invokeMethodLimitArgsTypes(class_, object, methodName, args, parameterTypes);
    }

    /**
     * 调用一个方法
     *
     * @param object 方法所在的对象
     * @param methodName 方法名
     * @param args 参数列表
     * @return 方法的返回值
     * @throws Exception
     */
    public static Object invokeMethod(final Object object, final String methodName, final Object... args) throws Exception {
        return ReflectUtil.invokeMethod(object.getClass(), object, methodName, args);
    }

    // - 构造方法
    /**
     * 调用一个构造函数来实例化一个对象
     *
     * @param class_ 要被实例化的 Class
     * @param args 参数列表
     * @param argTypes 参数的类型列表
     * @return 实例化后的对象
     * @throws Exception
     */
    public static Object invokeConstructorLimitArgsTypes(final Class<?> class_, final Object[] args, final Class<?>[] argTypes) throws Exception {
        // 查找构造函数
        final Constructor<?> constructor = class_.getDeclaredConstructor(argTypes);
        // 保存原访问权限
        final boolean accessible = constructor.isAccessible();
        // 设置允许通过反射访问
        if (!accessible) {
            constructor.setAccessible(true);
        }
        // 调用构造函数
        final Object result = constructor.newInstance(args);
        // 恢复原访问权限
        if (!accessible) {
            constructor.setAccessible(false);
        }
        // 返回结果
        return result;
    }

    /**
     * 调用一个构造函数来实例化一个对象
     *
     * @param class_ 要被实例化的 Class
     * @param args 参数列表
     * @return 实例化后的对象
     * @throws Exception
     */
    public static Object invokeConstructor(final Class<?> class_, final Object... args) throws Exception {
        final Class<?>[] parameterTypes = new Class<?>[args.length];
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
        }
        return ReflectUtil.invokeConstructorLimitArgsTypes(class_, args, parameterTypes);
    }
}