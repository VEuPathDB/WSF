package org.gusdb.wsf;

/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public interface IWsfPlugin {

    public String[][] invoke(String[] params, String[] values, String[] cols)
            throws WsfServiceException;
}
