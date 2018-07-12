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
import xoc.dsa.SetupVariableType;
import xoc.dta.measurement.IMeasurement;

/**
 *<pre>PARALLEL_LOOPBACK class contains methods, which are invoked through Java
 *Reflection. It contains all the customized actions related to PARALLEL_LOOPBACK
 *protocol, including:
 *
 * - setTransactionDef()   : TranSeqDef is Java class created to hold data structure
 *                           of Protocol. It is resided in protocolDriver/core/TranSeqDef.java.
 *                           This method will store reference of TranSeqDef created in
 *                           MainProtocol.java, to local private variable.
 * - updateSpecVariables() : Retrieved updated ProtocolCommand input parameters,
 *                           and update it to spec variables, when necessary.
 * - write()               : This method triggers necessary DS-API generation
 *                           action, to call customizedWrite transaction in
 *                           PARALLEL_LOOPBACK.prot
 * - read()                : This method triggers necessary DS-API generation
 *                           action, to call customizedRed transaction in
 *                           PARALLEL_LOOPBACK.prot
 * </pre>
 */
public class PARALLEL_LOOPBACK {
    private final Byte CAPTURE = 1;
    private final Byte NOCAPTURE = 0;
    private TransactionSequenceDefinitionData transactionSeqDefObj;
    private ISetupTransactionSeqArrayArgument captArrayVar = null;
    private ISetupTransactionSeqDef iSetupTransactionSeqDef;

    public PARALLEL_LOOPBACK() {}

    /**
     * This method is called from MainProtocol.java, through Java Reflection. This method
     * is used to setup private variables to be used in PARALLEL_LOOPBACK class.
     *
     * @param tsdef - TranSeqDef reference created in MainProtocol.java.
     */
    public void setTransactionDef(TransactionSequenceDefinitionData tsdef)
    {
        transactionSeqDefObj    = tsdef;
        iSetupTransactionSeqDef = transactionSeqDefObj.getTransactionSequenceDefinition();
    }

   /**
    * <pre>updateSpecVariable() is called from mainProtocol.java, through Java Reflection.</pre>
    *
    * @param debugFlag - Debug Flag from executeProtocolAware class, to enable debug printing
    * @param meas     - IMeasurement run object, which is needed to update value of spec
    *             variable.
    * @param param    - Parameters to be updated prior IMeasurement.execute().
    * <p>
    * <b>Note:</b> Some formatting checks had been implemented to ensure correct parameters are passed into this method call.
    * RuntimeException will be raised if the input parameters are passed in, with incorrect length or types.
    * <p><p>
    * Based on Register Panel GUI PARALLEL_LOOPBACK configuration file, input parameter
    * protocolAwareCommand will be constructed with structure below:<p>
    * &lt;write&gt;,&lt;protocol&gt;,&lt;RegisterName&gt;,&lt;BitName&gt;,&lt;BitLoc&gt;,&lt;Data&gt;,&lt;ReadBackData&gt;,&lt;Address&gt;,
    * &lt;Mask&gt;,&lt;Read-Write&gt;,&lt;Reset&gt;
    * <p><p>
    * For example:
    * write,PARALLEL_LOOPBACK,CR,,,0x7,,0x40010000,0x1337F7F,read_write,0x00000007
    * <p><p>
    * In this case, param[7] (address) and param[5] (data) will be extracted and populated into variable update commands,
    * depends on read or write transaction
    */
    public void updateSpecVariable(Boolean debugFlag, IMeasurement meas, String... param)
    {
        //perform check prior spec variable update:
        if (param[0].equals("write"))
        {
            if (param.length < 8)
            {
                System.out.println("Error: Incorrect number of parameters when calling PARALLEL_LOOPBACK customizedWrite method()! Fail to update Spec Variables");
                throw new RuntimeException("Incorrect number of parameters, it should be => write,PARALLEL_LOOPBACK,<RegisterName>,<BitName>,<BitLoc>,<Data>,<ReadBackData>,<Address>,<Mask>,<Read-Write>,<Reset>");
            }

            if (!param[7].isEmpty())
            {
                try
                {
                    meas.spec().setVariable("address", Long.decode(param[7]));
                    if (debugFlag)
                    {
                        System.out.println("Updating PARALLEL_LOOPBACK write transaction variable [address] to ... " + param[7]);
                    }
                }
                catch (RuntimeException e)
                {
                    throw new RuntimeException("8th parameter " + param[7] + " is not a Long type data. Please check your code...");
                }
            }
            else
            {
                throw new RuntimeException("8th parameter is empty, user must enter <Address> data when setting up write transaction");
            }

            if (!param[5].isEmpty())
            {
                try
                {
                    meas.spec().setVariable("data", Long.decode(param[5]));
                    if (debugFlag)
                    {
                       System.out.println("Updating PARALLEL_LOOPBACK write transaction variable [data]    to ... " + param[5]);
                    }
                }
                catch (RuntimeException e)
                {
                    throw new RuntimeException("6th parameter " + param[5] + " is not a Long type data. Please check your code...");
                }
            }
            else
            {
                throw new RuntimeException("6th parameter is empty, user must enter <Data> when setting up write transaction");
            }

        }
        else if (param[0].equals("read"))
        {
            //Protocol Aware Read Capture
            if (param.length < 8)
            {
                System.out.println("Error: Incorrect number of parameters when calling PARALLEL_LOOPBACK customizedWrite method()! Fail to update Spec Variables");
                throw new RuntimeException("Incorrect number of parameters, it should be => read,PARALLEL_LOOPBACK,<RegisterName>,<BitName>,<BitLoc>,<Data>,<ReadBackData>,<Address>,<Mask>,<Read-Write>,<Reset>");
            }

            if (!param[7].isEmpty())
            {
                try
                {
                    meas.spec().setVariable("address", Long.decode(param[7]));
                    if (debugFlag)
                    {
                        System.out.println("Updating PARALLEL_LOOPBACK read transaction variable [address] to ... " + param[7]);
                    }
                }
                catch (RuntimeException e)
                {
                    throw new RuntimeException("8th parameter " + param[7] + " is not a Long type data. Please check your code...");
                }
            }
            else
            {
                throw new RuntimeException("8th parameter is empty, user must enter <address> data when setting up read transaction");
            }
        }
        else
        {
            throw new RuntimeException("Unrecognized transaction type " + param[0] + ", which is not defined in updateSpecVariable() method!");
        }
    }

    /**
    * This method is invoked through Java Reflection from MainProtocol.java. The method name
    * <b>must</b> match transaction name to be used in &lt;Protocol&gt;.prot file.
    * <p><p>
    * In this example, this method is closely tracking syntax of PARALLEL_LOOPBACK transaction:<p>
    *       transaction write(UnsignedLong IN address, UnsignedLong IN data)
    * <p>
    * @param ds        - IDeviceSetup object, passing in through Java Reflection, which will be used for
    *              DS-API Protocol Aware Setup.
    * @param param     - Parameters to be used to call customizedWrite transaction, which are
    *              address and data.
    * @return 0, indicating there is no read capture in this method.
    *
    */
   public Byte write(IDeviceSetup ds, String... param)
   {

       ds.addVariable(SetupVariableType.UnsignedLong, "address", "0");
       ds.addVariable(SetupVariableType.UnsignedLong, "data", "0");

       iSetupTransactionSeqDef.addParameter(ISetupTransactionSeqDef.Type.UnsignedLong, ISetupTransactionSeqDef.Direction.IN, "address");
       iSetupTransactionSeqDef.addParameter(ISetupTransactionSeqDef.Type.UnsignedLong, ISetupTransactionSeqDef.Direction.IN, "data");

       iSetupTransactionSeqDef.addTransaction("write", "address", "data");

       ds.parallelBegin();
           ds.transactionSequenceCall(iSetupTransactionSeqDef, "address = address", "data = data");
       ds.parallelEnd();

       return NOCAPTURE;
   }

     /**
     * This method is invoked through Java Reflection from MainProtocol.java. The method name
     * <b>must</b> match transaction name to be used in &lt;Protocol&gt;.prot file.
     * <p><p>
     * In this example, this method is closely tracking syntax of PARALLEL_LOOPBACK transaction:<p>
     *      transaction read(UnsignedLong IN address, UnsignedLong INOUT data) masks (UnsignedLong maskData)
     * <p><p>
     * Mask is defined in &lt;Protocol&gt;.prot file, but not implemented in this method.
     * @param ds       - IDeviceSetup object, passing in through Java Reflection, which will be used for
     *             DS-API Protocol Aware Setup.
     * @param param    - Parameters to be used to call customizedWrite transaction, which are
     *             address and data. For read capture protocol aware, data input parameter is omitted.
     * @return 1, which indicates that this is read capture method.
     */
    public Byte read(IDeviceSetup ds, String... param) {

        ds.addVariable(SetupVariableType.UnsignedLong, "address", "0");

        iSetupTransactionSeqDef.addParameter(ISetupTransactionSeqDef.Type.UnsignedLong, ISetupTransactionSeqDef.Direction.IN, "address");

        captArrayVar = iSetupTransactionSeqDef.addArrayParameter(transactionSeqDefObj.getProtocolName() + "output",
                ISetupTransactionSeqDef.Type.UnsignedLongArray,
                ISetupTransactionSeqDef.Direction.OUT);

        transactionSeqDefObj.getTransactionSequenceDefinition().updateArrayParameterSizes(transactionSeqDefObj.getProtocolName() + "output", 1);

        iSetupTransactionSeqDef.addTransaction("read", "address", captArrayVar.getReference(transactionSeqDefObj.getCaptIndex()));
        transactionSeqDefObj.incrCaptIndex();

        ds.parallelBegin();
            String tranSeqCallName = ds.transactionSequenceCall(iSetupTransactionSeqDef, "address = address");
        ds.parallelEnd();

        transactionSeqDefObj.setTransSeqCallName(tranSeqCallName);

        return CAPTURE;
    }
}
