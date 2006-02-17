package org.gusdb.wsf.plugin;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 */

/**
 * @author Jerric
 * @created Feb 10, 2006
 */
public interface IWsfPlugin {

    /**
     * @param params
     * @param cols specify the columns of the result, and the order of them
     * @return
     * @throws WsfServiceException
     */
    public String[][] invoke(Map<String, String> params, String[] orderedColumns)
            throws WsfServiceException;

    void setLogger(Logger logger);
}
