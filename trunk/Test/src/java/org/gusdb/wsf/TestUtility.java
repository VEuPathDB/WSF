/**
 * 
 */
package org.gusdb.wsf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xingao
 *
 */
public final class TestUtility {

    public static String printArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"");
        for (String s : array) {
            sb.append(s);
            sb.append("\", \"");
        }
        sb.delete(sb.length() - 3, sb.length());
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

    public static String[] tokenize(String line) {
        Pattern pattern = Pattern.compile("\\b[\\w\\.]+\\b");
        Matcher match = pattern.matcher(line);
        List<String> tokens = new ArrayList<String>();
        while (match.find()) {
            String token = line.substring(match.start(), match.end());
            tokens.add(token);
        }
        String[] sArray = new String[tokens.size()];
        tokens.toArray(sArray);
        return sArray;
    }
}
