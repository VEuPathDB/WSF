/**
 * 
 */
package org.gusdb.wsf.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xingao
 * 
 * This time plugin runs 'date' system command, and parses out the result, and
 * returns the parts chosen by the params. The signal contains the return code
 * of the command, and the message contains the raw output of 'time'
 */
public class TimePlugin extends WsfPlugin {

    public static final String[] REQUIRED_PARAMS = { "hasDate", "hasTime" };
    public static final String[] OPTIONAL_PARAMS = { "hasWeekDay" };
    public static final String[] COLUMNS = { "field", "value" };

    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DAY = "Day";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String SECOND = "Second";
    public static final String TIME_ZONE = "TimeZone";
    public static final String WEEK_DAY = "WeekDay";

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.plugin.WsfPlugin#execute(java.lang.String,
     *      java.util.Map, java.lang.String[])
     * 
     * 
     */
    @Override
    protected WsfResult execute(String projectId, Map<String, String> params,
            String[] orderedColumns) throws WsfServiceException {
        // decide the param values;
        boolean hasDate = Boolean.parseBoolean(params.get(REQUIRED_PARAMS[0]));
        boolean hasTime = Boolean.parseBoolean(params.get(REQUIRED_PARAMS[1]));
        boolean hasWeekDay = false;
        if (params.containsKey(OPTIONAL_PARAMS[0]))
            hasWeekDay = Boolean.parseBoolean(params.get(OPTIONAL_PARAMS[0]));

        String[] command = buildCommand(params);
        try {
            StringBuffer result = new StringBuffer();
            int signal = invokeCommand(command, result, 60);
            String date = result.toString();
            String message = date;
            // for example: "Thu Feb 28 11:00:54 EST 2008"
            String[] parts = date.trim().split("\\s+");

            // get result
            Map<String, String> results = new HashMap<String, String>();
            if (hasDate) {
                results.put(YEAR, parts[5]);
                results.put(MONTH, mapMonth(parts[1]));
                results.put(DAY, parts[2]);
            }
            if (hasTime) {
                String[] time = parts[3].split(":");
                results.put(HOUR, time[0]);
                results.put(MINUTE, time[1]);
                results.put(SECOND, time[2]);
                results.put(TIME_ZONE, parts[4]);
            }
            if (hasWeekDay) results.put(WEEK_DAY, mapWeekDay(parts[0]));

            String[][] buffer = new String[results.size()][2];
            int row = 0;
            for (String field : results.keySet()) {
                String value = results.get(field);
                for (int i = 0; i < orderedColumns.length; i++) {
                    if (orderedColumns[i].equals(COLUMNS[0])) {
                        buffer[row][i] = field;
                    } else if (orderedColumns[i].equals(COLUMNS[1])) {
                        buffer[row][i] = value;
                    }
                }
                row++;
            }
            WsfResult wsfResult = new WsfResult();
            wsfResult.setMessage(message);
            wsfResult.setSignal(signal);
            wsfResult.setResult(buffer);
            return wsfResult;
        } catch (IOException ex) {
            throw new WsfServiceException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.plugin.WsfPlugin#getColumns()
     */
    @Override
    protected String[] getColumns() {
        return COLUMNS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.plugin.WsfPlugin#getRequiredParameterNames()
     */
    @Override
    protected String[] getRequiredParameterNames() {
        return REQUIRED_PARAMS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.plugin.WsfPlugin#validateParameters(java.util.Map)
     */
    @Override
    protected void validateParameters(Map<String, String> params)
            throws WsfServiceException {
        // the known params should all have boolean values
        for (String param : REQUIRED_PARAMS) {
            String value = params.get(param).trim().toLowerCase();
            if (!value.equals("true") && !value.equals("false"))
                throw new WsfServiceException("The param " + param
                        + " has invalid value: '" + params.get(param) + "'");
        }
        for (String param : OPTIONAL_PARAMS) {
            if (!params.containsKey(param)) continue;
            String value = params.get(param).trim().toLowerCase();
            if (!value.equals("true") && !value.equals("false"))
                throw new WsfServiceException("The param " + param
                        + " has invalid value: '" + params.get(param) + "'");
        }
    }

    private String[] buildCommand(Map<String, String> params) {
        List<String> command = new ArrayList<String>();
        command.add("date");

        // filter out the known params
        Map<String, String> knownParams = new HashMap<String, String>();
        for (String param : REQUIRED_PARAMS)
            knownParams.put(param, null);
        for (String param : OPTIONAL_PARAMS)
            knownParams.put(param, null);

        for (String param : params.keySet()) {
            if (knownParams.containsKey(param)) continue;

            command.add(param);
            String value = params.get(param);
            if (value != null && value.length() != 0) command.add(value);
        }

        // convert to array
        String[] array = new String[command.size()];
        command.toArray(array);
        return array;
    }

    private String mapMonth(String month) {
        if (month.equalsIgnoreCase("jan")) return "1";
        else if (month.equalsIgnoreCase("feb")) return "2";
        else if (month.equalsIgnoreCase("mar")) return "3";
        else if (month.equalsIgnoreCase("apr")) return "4";
        else if (month.equalsIgnoreCase("may")) return "5";
        else if (month.equalsIgnoreCase("jun")) return "6";
        else if (month.equalsIgnoreCase("jul")) return "7";
        else if (month.equalsIgnoreCase("aug")) return "8";
        else if (month.equalsIgnoreCase("sep")) return "9";
        else if (month.equalsIgnoreCase("oct")) return "10";
        else if (month.equalsIgnoreCase("nov")) return "11";
        else if (month.equalsIgnoreCase("dec")) return "12";
        else return "-1";
    }

    private String mapWeekDay(String weekDay) {
        if (weekDay.equalsIgnoreCase("mon")) return "1";
        else if (weekDay.equalsIgnoreCase("tue")) return "2";
        else if (weekDay.equalsIgnoreCase("wed")) return "3";
        else if (weekDay.equalsIgnoreCase("thu")) return "4";
        else if (weekDay.equalsIgnoreCase("fri")) return "5";
        else if (weekDay.equalsIgnoreCase("sat")) return "6";
        else if (weekDay.equalsIgnoreCase("sun")) return "0";
        else return "-1";
    }
}
