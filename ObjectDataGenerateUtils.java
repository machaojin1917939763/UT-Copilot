package com.maezia.vwasp.ordercenter.be.core.basic.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @author: Ma Chaojin(C | TT - 33)
 * @description: Mock 对象，使用这个方法，传入一个类的字节码文件，返回一个该类的对象，返回的对象所有的属性都赋值
 * 目前所支持的有：
 * 1、传入的类如果有父类，连父类的属性都会赋值
 * 2、支持常见的基本属性
 * 3、支持包装类
 * 4、支持自定义类
 * 5、支持基本类型集合
 * 6、支持包装类集合
 * 7、支持自定义类型集合
 * 8、支持基本类型数组
 * 9、支持包装类数组
 * 10、支持自定义类型数组
 * @date: 2023/7/13 18:16
 * @version: 1.0
 */
@Slf4j
public class ObjectDataGenerateUtils {


    public static <T> List<T> generateDataList(Class<T> generateData,int listSize){
        List<T> r = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            T data = generateData(generateData);
            r.add(data);
        }
        return r;
    }

    public static <T> List<T> generateDataList(Class<T> generateData,int listSize,Object ...value){
        List<T> r = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            T data = generateData(generateData,value);
            r.add(data);
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] generateDataArray(Class<T> generateData,int arraySize){
        T[] r = (T[]) new Object[arraySize];
        for (int i = 0; i < arraySize; i++) {
            T data = generateData(generateData);
            r[i] = data;
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] generateDataArray(Class<T> generateData,int arraySize,Object ...value){
        T[] r = (T[]) new Object[arraySize];
        for (int i = 0; i < arraySize; i++) {
            T data = generateData(generateData,value);
            r[i] = data;
        }
        return r;
    }

    /**
     * 不可以自定义参数值的，使用方法
     * ObjectDataGenerateUtils.generateData(PaymentQrCodeRequest.class);
     *
     * @param object
     * @param <T>
     * @return
     */
    public static <T> T generateData(Class<T> object) {
        try {
            T instance = object.getDeclaredConstructor().newInstance();
            generateDataForObject(instance, object, null);
            return instance;
        } catch (Exception e) {
            if (!e.getClass().equals(IllegalArgumentException.class)){
                e.printStackTrace();
                throw new RuntimeException("创建对象失败了");
            }
        }
        return (T) new Object();
    }

    /**
     * 可以自定义参数值的自动生成对象方法，使用方法
     * ObjectDataGenerateUtils.generateData(PaymentQrCodeRequest.class,"extInfo","{'name':'machaojin'}");
     *
     * @param object 类的字节码文件
     * @param values 参数名和参数值，参数值在参数名的后面
     * @param <T>
     * @return
     */
    public static <T> T generateData(Class<T> object, Object... values) {
        if (values.length % 2 != 0) {
            throw new RuntimeException("参数名和参数值需要一一对应哦");
        }
        try {
            T instance = object.getDeclaredConstructor().newInstance();
            HashMap<String, Object> fieldForValues = new HashMap<>();
            for (int i = 0; i < values.length; i += 2) {
                fieldForValues.put(String.valueOf(values[i]), values[i + 1]);
            }
            generateDataForObject(instance, object, fieldForValues);
            return instance;
        } catch (Exception e) {
            log.error("参数未成功赋值");
        }
        return (T) new Object();
    }

    /**
     * 创建一个对象的实例
     *
     * @param instance 对象实例
     * @param object   创建好的对象
     */
    private static void generateDataForObject(Object instance, Class<?> object, HashMap<String, Object> fieldForValues) {
        for (Class<?> clazz = object; clazz != null; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                try {
                    //自定义的方法属性
                    if (fieldForValues != null && fieldForValues.containsKey(field.getName())) {
                        field.set(instance, fieldForValues.get(field.getName()));
                        continue;
                    }
                    if (type.equals(Integer.class)) {
                        field.set(instance, 1);
                    } else if (type.equals(Short.class)) {
                        field.set(instance, (short) 1);
                    } else if (type.equals(Byte.class)) {
                        field.set(instance, (byte) 1);
                    } else if (type.equals(Double.class)) {
                        field.set(instance, 0.0);
                    } else if (type.equals(Float.class)) {
                        field.set(instance, 0f);
                    }else if (type.equals(Boolean.class)) {
                        field.set(instance, true);
                    }else if (type.equals(Character.class)) {
                        field.set(instance, 'v');
                    }else if (type.equals(String.class)) {
                        if (field.getName().toLowerCase().contains("id")) {
                            field.set(instance, "1");
                        } else if (field.getName().toLowerCase().contains("time")) {
                            field.set(instance, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                        } else if (field.getName().toLowerCase().contains("money") || field.getName().toLowerCase().contains("amount")) {
                            field.set(instance, "1.00");
                        }else {
                            field.set(instance, field.getName());
                        }
                    } else if (type.equals(Long.class)) {
                        field.set(instance, 1L);

                    } else if (type.equals(BigDecimal.class)) {

                        field.set(instance, BigDecimal.ZERO.setScale(2));

                    } else if (type.equals(LocalDateTime.class)) {

                        field.set(instance, LocalDateTime.now());
                        //如果是一个集合，并且集合的属性是其他类
                    }else if (type.equals(int.class)) {
                        field.set(instance, 1);
                    } else if (type.equals(short.class)) {
                        field.set(instance, (short) 1);
                    } else if (type.equals(byte.class)) {
                        field.set(instance, (byte) 1);
                    } else if (type.equals(double.class)) {
                        field.set(instance, 0.0);
                    } else if (type.equals(float.class)) {
                        field.set(instance, 0f);
                    }else if (type.equals(boolean.class)) {
                        field.set(instance, true);
                    }else if (type.equals(char.class)) {
                        field.set(instance, 'v');
                    } else if (type.equals(long.class)) {
                        field.set(instance, 1L);
                    } else if (Collection.class.isAssignableFrom(type)) {
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        Class<?> elementType = (Class<?>) genericType.getActualTypeArguments()[0];

                        field.set(instance, isCollection(elementType, fieldForValues));
                        //如果是一个数组
                    } else if (type.isArray()) {
                        Class<?> elementType = type.getComponentType();
                        field.set(instance, isArray(elementType, fieldForValues));
                        //普通自定义属性
                    } else if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
                        Object propertyInstance = type.getDeclaredConstructor().newInstance();
                        generateDataForObject(propertyInstance, type, fieldForValues);
                        field.set(instance, propertyInstance);
                    }
                } catch (Exception e) {
                    log.error("参数未成功赋值");
                }

            }
        }
    }

    /**
     * 生成一个数组
     *
     * @param elementType 数组元素类型
     * @param <T>
     * @return
     * @throws Exception
     */
    private static <T> Object isArray(Class<?> elementType, HashMap<String, Object> fieldForValues) throws Exception {
        Object array = Array.newInstance(elementType);
            if (elementType.isPrimitive()) {
                if (elementType.equals(int.class)) {
                    Array.set(array, 0,1);
                } else if (elementType.equals(boolean.class)) {
                    Array.set(array, 0, true);
                } else if (elementType.equals(long.class)) {
                    Array.set(array, 0, 1L);
                } else if (elementType.equals(byte.class)) {
                    Array.set(array, 0, (byte)1);
                } else if (elementType.equals(short.class)) {
                    Array.set(array, 0, (short)1);
                } else if (elementType.equals(float.class)) {
                    Array.set(array, 0, 1F);
                }else if (elementType.equals(double.class)) {
                    Array.set(array, 0, 0.0);
                }else if (elementType.equals(char.class)) {
                    Array.set(array, 0, 'v');
                }
                // ... 处理其他基本类型
            } else if (elementType.equals(Integer.class)) {
                Array.set(array, 0, 1);
            } else if (elementType.equals(Boolean.class)) {
                Array.set(array, 0, true);
            } else if (elementType.equals(Long.class)) {
                Array.set(array, 0, 1L);
            } else if (elementType.equals(String.class)) {
                Array.set(array, 0, "success");
            } else if (elementType.equals(Byte.class)) {
                Array.set(array, 0, (byte)1);
            } else if (elementType.equals(Short.class)) {
                Array.set(array, 0, (short)1);
            } else if (elementType.equals(Float.class)) {
                Array.set(array, 0, 1F);
            } else if (elementType.equals(Double.class)) {
                Array.set(array, 0, 0.0);
            } else if (elementType.equals(Character.class)) {
                Array.set(array, 0, 'v');
            } else {
                Object element = elementType.getDeclaredConstructor().newInstance();
                generateDataForObject(element, elementType, fieldForValues);
                Array.set(array, 0, element);
            }
        return array;
    }

    /**
     * 集合类型对象生成
     *
     * @param elementType 集合元素
     * @return
     * @throws Exception
     */
    private static Collection<Object> isCollection(Class<?> elementType, HashMap<String, Object> fieldForValues) throws Exception {
        Collection<Object> collection = new ArrayList<>();
            if (elementType.isPrimitive()) {
                if (elementType.equals(int.class)) {
                    collection.add(1);
                } else if (elementType.equals(boolean.class)) {
                    collection.add(true);
                } else if (elementType.equals(long.class)) {
                    collection.add(1L);
                } else if (elementType.equals(byte.class)) {
                    collection.add((byte)1);
                } else if (elementType.equals(short.class)) {
                    collection.add((short)1);
                } else if (elementType.equals(float.class)) {
                    collection.add(1F);
                } else if (elementType.equals(double.class)) {
                    collection.add(0.0);
                } else if (elementType.equals(char.class)) {
                    collection.add(0.0);
                }
                // ... 处理其他基本类型
            } else if (elementType.equals(Integer.class)) {
                collection.add(1);
            } else if (elementType.equals(Boolean.class)) {
                collection.add(true);
            } else if (elementType.equals(Long.class)) {
                collection.add(1L);
            } else if (elementType.equals(String.class)) {
                collection.add("List success");
            } else if (elementType.equals(Byte.class)) {
                collection.add(1);
            } else if (elementType.equals(Short.class)) {
                collection.add(1);
            } else if (elementType.equals(Float.class)) {
                collection.add(1);
            } else if (elementType.equals(Double.class)) {
                collection.add(0.0);
            } else if (elementType.equals(Character.class)) {
                collection.add('v');
            } else {
                Object element = elementType.getDeclaredConstructor().newInstance();
                generateDataForObject(element, elementType, fieldForValues);
                collection.add(element);
            }
        return collection;
    }
}

