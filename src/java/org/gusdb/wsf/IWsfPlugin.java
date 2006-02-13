package org.gusdb.wsf;

import java.util.Map;

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
    public String[][] invoke(Map<String, String> params, String[] cols)
            throws WsfServiceException;
}
