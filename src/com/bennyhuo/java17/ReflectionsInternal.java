package com.bennyhuo.java17;


/**
 * Created by benny.
 */
public class ReflectionsInternal {

    public static void main(String[] args) {
        useWeakCache();
    }

    public static void useWeakCache() {
        try {
            var weakCacheClass = Class.forName("com.sun.beans.WeakCache");
            var weakCache = weakCacheClass.getDeclaredConstructor().newInstance();
            var putMethod = weakCacheClass.getDeclaredMethod("put", Object.class, Object.class);
            var getMethod = weakCacheClass.getDeclaredMethod("get", Object.class);
            putMethod.invoke(weakCache, "name", "bennyhuo");
            System.out.println(getMethod.invoke(weakCache, "name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
