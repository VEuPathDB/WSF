package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class EnumItem extends WdkModelBase {

    private String display;
    private String term;
    private String internal;
    private String parentTerm;
    private boolean isDefault = false;

    /**
     * default constructor called by digester
     */
    public EnumItem() {}

    /**
     * Copy constructor
     * 
     * @param enumItem
     */
    public EnumItem(EnumItem enumItem) {
        this.display = enumItem.display;
        this.term = enumItem.term;
        this.internal = enumItem.internal;
        this.isDefault = enumItem.isDefault;
        this.parentTerm = enumItem.parentTerm;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return (display == null) ? term : display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    String getTerm() {
        return term;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    String getInternal() {
        return internal;
    }

    /**
     * @return the isDefault
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * @param isDefault
     *            the isDefault to set
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // do nothing.
    }

    /**
     * @return the parentTerm
     */
    public String getParentTerm() {
        return parentTerm;
    }

    /**
     * @param parentTerm
     *            the parentTerm to set
     */
    public void setParentTerm(String parentTerm) {
        this.parentTerm = parentTerm;
    }
}