package org.gusdb.wsf.plugin;

import java.util.Map;

/**
 * 
 */

/**
 * @author Jerric
 * @created Feb 10, 2006
 */
public interface Plugin {

    /**
     * @param params
     * @param cols
     *            specify the columns of the result, and the order of them
     * @return
     * @throws WsfServiceException
     */
    public WsfResponse execute(WsfRequest request) throws WsfServiceException;

    /**
     * The Plugin needs to provide a list of required parameter names; the base
     * class will use this template method in the input validation process.
     * 
     * @return returns an array the names of the required parameters
     */
    public String[] getRequiredParameterNames();

    /**
     * The Plugin needs to provides a list of the columns expected in the
     * result; the base class will use this template method in the input
     * validation process.
     * 
     * @return returns an array the columns expected in the result
     */
    public String[] getColumns();

    public String[] getContextKeys();

    public void initialize(Map<String, Object> context) throws WsfServiceException;

    public void validateParameters(WsfRequest request)
            throws WsfServiceException;

}
