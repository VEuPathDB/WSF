package org.gusdb.wdk.model;

import java.util.HashMap;

public class EnumParam extends AbstractEnumParam {
    
    public EnumParam () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void addItem(EnumItem item) {
	
	if (vocabMap == null) vocabMap = new HashMap();
	orderedKeySet.add(item.getTerm());
	vocabMap.put(item.getTerm(), item.getInternal());
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {
    }

    protected void initVocabMap() throws WdkModelException {
	
    }
}