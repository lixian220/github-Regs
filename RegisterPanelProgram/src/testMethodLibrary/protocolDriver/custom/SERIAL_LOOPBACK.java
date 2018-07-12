/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package testMethodLibrary.protocolDriver.custom;

import testMethodLibrary.protocolDriver.core.TransactionSequenceDefinitionData;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupTransactionSeqArrayArgument;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.measurement.IMeasurement;

/**
 *<pre>SERIAL_LOOPBACK class contains methods, which are invoked through Java
 *Reflection. It contains all the customized actions related to SERIAL_LOOPBACK
 *protocol, including:
 *
 * - setTransactionDef()   : TranSeqDef is Java class created to hold data structure
 *                           of Protocol. It is resided in protocolDriver/core/TranSeqDef.java.
 *                           This method will store reference of TranSeqDef created in
 *                           MainProtocol.java, to local private variable.
 * - updateSpecVariables() : Retrieved updated ProtocolCommand input parameters,
 *                           and update it to spec variables, when necessary.
 * - customizedWrite()     : This method triggers necessary DS-API generation
 *                           action, to call customizedWrite transaction in
 *                           SERIAL_LOOPBACK.prot
 * - costomizedRead()      : This method triggers necessary DS-API generation
 *                           action, to call customizedRed transaction in
 *                           SERIAL_LOOPBACK.prot
 * </pre>
 */
public class SERIAL_LOOPBACK {
    private final Byte ERROR = 2;
    private final Byte CAPTURE = 1;
    private final Byte NOCAPTURE = 0;
    private TransactionSequenceDefinitionData transactionSeqDefObj;
    private ISetupTransactionSeqArrayArgument captArrayVar = null;
    private ISetupTransactionSeqDef iSetupTransactionSeqDef;

    public SERIAL_LOOPBACK() {}

    /**
     * This method is called from MainProtocol.java, through Java Reflection. This method
     * is used to setup private variables to be used in SERIAL_LOOPBACK class.
     *
     * @param tsdef - TranSeqDef reference created in MainProtocol.java.
     */
    public void setTransactionDef(TransactionSequenceDefinitionData tsdef) {
        transactionSeqDefObj    =tsdef;
        iSetupTransactionSeqDef = transactionSeqDefObj.getTransactionSequenceDefinition();
    }

    /**
     * <pre>updateSpecVariable() is called from mainProtocol.java, through Java Reflection.</pre>
     *
     * @param debugFlag - Debug Flag from executeProtocolAware class, to enable debug printing
     * @param meas     - IMeasurement run object, which is needed to update value of spec
     *             variable.
     * @param param    - Parameters to be updated prior IMeasurement.execute().
     *
     * <p><p>
     * <b>Note:</b> SERIAL_LOOPBACK protocol is defined with variable length. Due to this, updating spec approach
     * is not supported in SERIAL_LOOPBACK Protocol. Instead of using this method to update Spec Variable in
     * SERIAL_LOOPBACK protocol, user must ensure setup()/compile() blocks are called in GUI code.<p>
     * <b>Note:</b> This method is only used to perform System.out.println() to UI report in SERIAL_LOOPBACK.java class
     */
    public void updateSpecVariable(Boolean debugFlag, IMeasurement meas, String... param)
    {
        //perform printout, no spec variable to be updated:
        if (debugFlag)
        {
            if (param[0].equals("write"))
            {
                System.out.println("Runing SERIAL_LOOPBACK.customizedWrite with IR = <" + param[7] + ">");
                System.out.println("Runing SERIAL_LOOPBACK.customizedWrite with DR = <" + param[5] + ">");
            }
            else if (param[0].equals("read"))
            {
                System.out.println("Runing SERIAL_LOOPBACK.customizedRead with IR = <" + param[7] + ">");
                System.out.println("Runing SERIAL_LOOPBACK.customizedRead with DR = <" + param[5] + ">");
            }
        }
    }

   /**
    * This method is invoked through Java Reflection from MainProtocol.java. The method name
    * <b>must</b> match transaction name to be used in &lt;Protocol&gt;.prot file.
    * <p><p>
    * In this example, this method is closely tracking syntax of SERIAL_LOOPBACK transaction:<p>
    *   transaction customizedWrite (BitSequence IN IrDataIn, BitSequence IN DrDataIn)
    * <p><p>
    * Based on Register Panel configuration file for SERIAL_LOOPBACK, input parameter protocolAwareCommand
    * will be constructed with structure below:<p>
    * &lt;read&gt;,&lt;Protocol&gt;,&lt;RegisterName&gt;,&lt;DRBitName&gt;,&lt;DrBitLoc&gt;,&lt;DrBitData&gt;,&lt;ReadBackData&gt;,&lt;IrRegister&gt;,&lt;Reset&gt;
    * <p><p>
    * For example:<p>
    * write, SERIAL_LOOPBACK,BYPASS,,,0x0,,0xF,0x0
    * <p><p>
    * In this example implementation, param[5](DrDataIn) and param[7](IrDataIn) will be extracted
    * and populated into DS-API commands. <p>
    * <b>Note:</b> need to convert these value from HexString to BitSequence String Format, enclose with "&lt;" and "&gt;"
    * <p><p>
    * @param ds       - IDeviceSetup object, passing in through Java Reflection, which will be used for
    *             DS-API Protocol Aware Setup.
    * @param param    - Parameters to be used to call customizedWrite transaction, which are
    *             IrDataIn and DrDataIn
    * @return 0, indicating there is no read capture in this method.
    */
    public Byte write(IDeviceSetup ds, String[] param)
    {

        if (param.length < 8)
        {
            System.out.println("Error: Incorrect number of parameters when calling SERIAL_LOOPBACK customizedWrite method()! Fail to generate DS-API setup");
            throw new RuntimeException("Incorrect number of parameters, it should be => write,SERIAL_LOOPBACK,<RegisterName>,<DRBitName>,<DrBitLoc>,<DrBitData>,<ReadBackData>,<IrRegister>,<Reset>");
        }

        iSetupTransactionSeqDef.addTransaction("customizedWrite", convertToBitSequenceString(param[7]), convertToBitSequenceString(param[5]));

        ds.parallelBegin();
            ds.transactionSequenceCall(iSetupTransactionSeqDef);
        ds.parallelEnd();
        return NOCAPTURE;
    }

    /**
    * This method is invoked through Java Reflection from MainProtocol.java. The method name
    * <b>must</b> match transaction name to be used in &lt;Protocol&gt;.prot file.
    * <p><p>
    * In this example, this method is closely tracking syntax of SERIAL_LOOPBACK transaction:<p>
    *       transaction customizedRead (BitSequence IN IrDataIn, BitSequence IN DrDataIn, BitSequence INOUT DrDataOut)
    * <p><p>
    * Based on Register Panel configuration file for SERIAL_LOOPBACK, input parameter protocolAwareCommand
    * will be constructed with structure below:<p>
    * &lt;read&gt;,&lt;Protocol&gt;,&lt;RegisterName&gt;,&lt;DRBitName&gt;,&lt;DrBitLoc&gt;,&lt;DrBitData&gt;,&lt;ReadBackData&gt;,&lt;IrRegister&gt;,&lt;Reset&gt;
    * <p><p>
    * For example:
    * read, SERIAL_LOOPBACK,BYPASS,,,0x0,,0xF,0x0
    * <p><p>
    * In this case, param[5] (DrDataIn) and param[7] (IrDataIn) will be extracted and populated into DS-API commands<p>
    * <b>Note:</b> need to convert these value from hexString to BitSequence String format, enclose with "&lt;" and "&gt;"
    * <p><p>
    * In this SERIAL_LOOPBACK customizedRead method, it is designed to perform online validation
    * with E7010 cross-connect board. During online execution, data written into DrDataIn will
    * be source through TDI pin, loopback to TDO pin through cross-connect board. <br>
    * TDO pin is used in Protocol Aware Capture. <br>
    *
    * @param ds    - IDeviceSetup object, passing in through Java Reflection, which will be used for
    *          DS-API Protocol Aware Setup.
    * @param param - Parameters to be used to call customizedWrite transaction, which are
    *          IrDataIn, DrDataIn and DrDataOut. For read capture, DrDataOut parameter is omitted.
    * @return 1, which indicates that this is read capture method.
    */
    public Byte read(IDeviceSetup ds, String[] param)
    {
        if (param.length < 9)
        {
            System.out.println("Error: Incorrect number of parameters when calling SERIAL_LOOPBACK customizedRead method()! Fail to generate DS-API setup");
            return ERROR;
        }

        //Read Capture
        captArrayVar = iSetupTransactionSeqDef.addArrayParameter(transactionSeqDefObj.getProtocolName() + "output",
                ISetupTransactionSeqDef.Type.BitSequenceArray,
                ISetupTransactionSeqDef.Direction.OUT);

        transactionSeqDefObj.getTransactionSequenceDefinition().updateArrayParameterSizes(transactionSeqDefObj.getProtocolName() + "output", 1);

        iSetupTransactionSeqDef.addTransaction("customizedRead", convertToBitSequenceString(param[7]), convertToBitSequenceString(param[5]), captArrayVar.getReference(transactionSeqDefObj.getCaptIndex()));
        transactionSeqDefObj.incrCaptIndex();

        ds.parallelBegin();
            String tranSeqCallName = ds.transactionSequenceCall(iSetupTransactionSeqDef);
        ds.parallelEnd();
        transactionSeqDefObj.setTransSeqCallName(tranSeqCallName);
        return CAPTURE;
    }

    /**
     * @param data String data from GUI code
     * @return String data type, converted to SmarTest BitStream String format, enclose with "&lt;" and "&gt;"
     */
    private String convertToBitSequenceString(String data)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(data);
        sb.append(">");

        return sb.toString();
    }
}

