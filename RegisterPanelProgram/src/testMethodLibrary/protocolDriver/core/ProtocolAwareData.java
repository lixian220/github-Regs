/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/

package testMethodLibrary.protocolDriver.core;

import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.resultaccess.datatypes.MultiSiteBitSequence;

/**
 * ProtocolAwareData class is utility class, which is used to store Protocol Aware Capture raw data <p>
 * <p>
 * It contains two private members, which is used depends on Protocol Aware INOUT or OUT data type: <p>
 * &lt;MultiSiteLong&gt; - Store Protocol Aware MultiSiteLong data type <p>
 * &lt;MultiSiteBitSequence&gt; - Store Protocol Aware MultiSiteBitSequence data type<p>
 * <p>
 * These 2 data types are mutually exclude in Protocol Aware capture.
 */
public class ProtocolAwareData {
    private MultiSiteLong lData;
    private MultiSiteBitSequence bitSeq;

    /**
     * Return MultiSiteLong type protocol aware capture data<p>
     * @return MultiSiteLong type
     */
    public MultiSiteLong getData()
    {
        return lData;
    }

    /**
     * Return MultiSiteBitSequence type protocol aware capture data<p>
     * @return MultiSiteBitSequence type
     */
    public MultiSiteBitSequence getBitSequence()
    {
        return bitSeq;
    }

    /**
     * Set MultiSiteLong data to private field in this class, and set
     * MultiSiteBitSequence data to null<p>
     * @param _val MultiSiteLong type
     */
    public void setData(MultiSiteLong _val)
    {
        lData=_val;
        bitSeq=null;
    }

    /**
     * Set MultiSiteBitSequence data to private field in this class, and
     * set MultiSiteLong data to null<p>
     * @param _bit MultiSiteBitSequence type
     */
    public void setBitSequence(MultiSiteBitSequence _bit)
    {
        bitSeq=_bit;
        lData=null;
    }
}
