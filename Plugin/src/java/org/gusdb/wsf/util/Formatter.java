/**
 * 
 */
package org.gusdb.wsf.util;

/**
 * @author xingao
 *
 */
public class Formatter {

    public static String printArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (String s : array) {
            if (sb.length() > 1) sb.append(", ");
            sb.append("\"" + s + "\"");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String printArray(String[][] array) {
        String newline = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        for (String[] parts : array) {
            sb.append(printArray(parts));
            sb.append(newline);
        }
        return sb.toString();
    }
}
