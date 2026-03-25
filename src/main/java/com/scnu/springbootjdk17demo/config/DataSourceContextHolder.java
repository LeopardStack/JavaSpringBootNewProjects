package com.scnu.springbootjdk17demo.config;

public class DataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String ds)  { CONTEXT.set(ds); }
    public static String get()         { return CONTEXT.get(); }
    public static void clear()         { CONTEXT.remove(); }
}