/*
 * Copyright (C) 2025 Glavo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.info;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Glavo
 */
public final class Main {

    private static final StringBuilder builder = new StringBuilder();

    private static void appendString(StringBuilder builder, String str) {
        builder.append('"');
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= ' ' && c <= '~') {
                if (c == '"' || c == '\\') {
                    builder.append('\\');
                }
                builder.append(c);
            } else {
                switch (c) {
                    case '\b':
                        builder.append("\\b");
                        break;
                    case '\t':
                        builder.append("\\t");
                        break;
                    case '\n':
                        builder.append("\\n");
                        break;
                    case '\f':
                        builder.append("\\f");
                        break;
                    case '\r':
                        builder.append("\\r");
                        break;
                    default:
                        builder.append("\\u").append(String.format("%04x", (int) c));
                }
            }
        }

        builder.append('"');
    }

    private static void printSystemProperty(String key) {
        printInfo(key, System.getProperty(key));
    }

    private static void printInfo(String key, Object value) {
        printInfo(key, value, false);
    }

    private static void printInfo(String key, Object value, boolean last) {
        builder.setLength(0);
        builder.append("  \"").append(key).append("\": ");
        if (value instanceof String) {
            appendString(builder, (String) value);
        } else if (value instanceof Integer) {
            builder.append(((Integer) value).intValue());
        } else if (value instanceof String[]) {
            String[] array = (String[]) value;
            if (array.length == 0) {
                builder.append("[]");
            } else {
                builder.append('[');
                for (int i = 0; i < array.length; i++) {
                    String item = array[i];
                    appendString(builder, item);
                    if (i < array.length - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(']');
            }
        } else {
            builder.append(value);
        }

        if (!last) {
            builder.append(",");
        }

        System.out.println(builder);
    }

    private static boolean checkClass(String className) {
        try {
            Class.forName(className, false, Main.class.getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static String[] getModules() {
        try {
            Method getModuleName = Class.forName("java.lang.Module").getMethod("getName");

            Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
            Object bootLayer = moduleLayerClass.getMethod("boot").invoke(null);

            Set<?> modules = (Set<?>) moduleLayerClass.getMethod("modules").invoke(bootLayer);

            TreeSet<String> names = new TreeSet<String>();
            for (Object module : modules) {
                String name = (String) getModuleName.invoke(module);
                names.add(name);
            }
            return names.toArray(new String[0]);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println('{');

        printSystemProperty("os.name");
        printSystemProperty("os.arch");
        printSystemProperty("java.version");
        printSystemProperty("java.fullversion"); // IBM J9
        printSystemProperty("java.vendor");
        printSystemProperty("java.home");
        printSystemProperty("java.vm.version");
        printSystemProperty("java.vm.vendor");
        printSystemProperty("java.vm.name");
        printSystemProperty("java.vm.info");
        printSystemProperty("java.class.version");
        printSystemProperty("java.compiler");

        printSystemProperty("sun.management.compiler");
        printSystemProperty("jvmci.Compiler");

        printInfo("java.modules", getModules(), true);
        System.out.println('}');
    }
}
