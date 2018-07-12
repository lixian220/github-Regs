/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package testMethodLibrary.protocolDriver.core;

import java.io.Serializable;

import xoc.dsa.ISetupTransactionSeqDef;

/**
 * <pre>
 * class TransactionSequenceDefinitionData {
 *     String                  sProtName;   //protocol name
 *     String                  sTransSeqCallName; // TransactionSequence call Name, needed for Protocol Aware result retrieval
 *     int                     iCaptIndex;  //record number of variables used for capture data
 *     int                     iInDataIndex;//record number of variables used to input the data
 *     ISetupTransactionSeqDef trSeqDef;    //ISetupTransactionSeqDef Object, used in DS-API generation
 * }
 * </pre>
 */
public class TransactionSequenceDefinitionData implements Serializable
{
    private String                              sProtName;
    private String                              sTransSeqCallName;
    private int                                 iCaptIndex;
    private int                                 iInDataIndex;
    private transient ISetupTransactionSeqDef   trSeqDef;
    private static final long                   serialVersionUID = 1L;

    //package private constructor
    TransactionSequenceDefinitionData(String __sProt, ISetupTransactionSeqDef __tranSeqDefObj)
    {
        sProtName=__sProt;
        trSeqDef=__tranSeqDefObj;
        iCaptIndex=0;
        iInDataIndex=0;
    }

    /**
     * Increase iCaptIndex by 1 <p>
     * iCaptIndex is used to track number of variables used for capture data
     */
    public void incrCaptIndex()
    {
        iCaptIndex++;
    }

    /**
     * set iCaptIndex through input parameter _val <p>
     * iCaptIndex is used to track number of variables used for capture data
     * @param _val int type, overwrite iCaptIndex with this input parameter
     */
    public void setCaptIndex(int _val)
    {
        iCaptIndex=_val;
    }

    /**
     * Increase iInDataIndex by 1 <p>
     * iInDataIndex is used to track number of variables used to input the data
     */
    public void incrInDataIndex()
    {
        iInDataIndex++;
    }

    /**
     * set iInDataIndex through input parameter _val<p>
     * iInDataIndex is used to track number of variables used to input the data
     * @param _val int type, overwrite iInDataIndex with this input parameter
     */
    public void setInDataIndex(int _val)
    {
        iInDataIndex=_val;
    }

    /**
     * return iCaptIndex value<p>
     * iCaptIndex is used to track number of variables used for capture data
     * @return int type
     */
    public int getCaptIndex()
    {
        return iCaptIndex;
    }

    /**
     * return iInDataIndex value<p>
     * iInDataIndex is used to track number of variables used to input the data
     * @return int type
     */
    public int getInDataIndex()
    {
        return iInDataIndex;
    }

    /**
     * retun trSeqDef, which defines the ISetupTransactionSeqDef Object, used in DS-API generation
     * @return ISetupTransactionSeqDef
     */
    public ISetupTransactionSeqDef getTransactionSequenceDefinition()
    {
        return trSeqDef;
    }

    /**
     * return sProtName, which is used to record protocol aware name
     * @return String type
     */
    public String getProtocolName()
    {
        return sProtName;
    }

    /**
     * return sTransSeqCallName, which is needed for Protocol Aware result retrieval
     * @return String type, TransactionSequence Call Name
     */
    public String getTransSeqCallName()
    {
        return sTransSeqCallName;
    }

    /**
     * set sTransSeqCallName, which will be used for Protocol Aware result retrieval
     * @param _transSeqCallName
     */
    public void setTransSeqCallName(String _transSeqCallName)
    {
        sTransSeqCallName = _transSeqCallName;
    }
}
