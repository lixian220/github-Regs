/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package testMethodLibrary.registerPanelTML;

import java.util.List;

import testMethodLibrary.protocolDriver.core.MainProtocol;
import testMethodLibrary.protocolDriver.core.ProtocolAwareData;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteString;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.datatypes.BitSequence;

/**
 * <pre>
 * ExecuteProtocolAware class extends SmarTest TestMethod class
 *
 * Two compulsory parameters to work with Register Panel GUI:
 * - protocolAwareCommand &lt;String Type&gt;        - Contains all the parameters needed to
 *                                                setup/run Protocol Aware from GUI
 * - protocolAwareResult &lt;MultiSiteString Type&gt; - Contains for result from Protocol Aware execution
 *
 * This testmethod used DS-API to generate Protocol Aware Setup.
 *
 * In this testmethod, there are two "extension" points, allowing user
 * to customize their Protocol Aware. It is realized through Java Reflection,
 * and these 2 extension points are:
 * - mainProtocol.PASequencer() =>  Generate Spec/OSEQ setup files through DS-API
 * - mainProtocol.updateProtocolAwareCommand() => update protocolAwareCommand input Parameter
 *
 * <pre>Customized methods are programmed in protocolDrive/custom/&ltProtocol>.java file.
 * </pre>
 */
public class ExecuteProtocolAware extends TestMethod {

    /** Protocol Aware Command String, which is used to setup Protocol Aware parameters */
    @In
    public String protocolAwareCommand = "";

    /** IMeasurement Object */
    @In
    public IMeasurement measurement;

    /**
     * Specification file contains level/timing/wavetable setup properties for respective Protocol
     * Aware Setup
     */
    @In
    public String specificationFile;

    /** Debug flag - to print out execution information in setup() and execute() blocks */
    @In
    public boolean debugFlag = false;

    /**
     * This is result handle, which will be used by Register Panel GUI. Result will be formatted in
     * String/Hex. Register Panel GUI will retrieve this variable, process it, and populate data
     * into GUI.
     */
    @Out
    public MultiSiteString protocolAwareResult = new MultiSiteString();

    private IDeviceSetup devSetup;
    private MainProtocol mainProtocol;

    @Override
    public void setup() {
        if (debugFlag) {
            System.out.println("\n*******************************************************");
            System.out.println(
                    "Executing setup(), setting up TestSuite: " + context.getTestSuiteName());
            System.out.println("With protocolAwareCommand : " + protocolAwareCommand);
        }

        super.setup();

        measurement.setSpecificationName(specificationFile);

        devSetup = DeviceSetupFactory.createInstance();
        mainProtocol = new MainProtocol(measurement, context, devSetup);
        mainProtocol.PASequencer(stringToStringArray(protocolAwareCommand));
        measurement.setSetups(devSetup);

        if (debugFlag) {
            System.out.println("*******************************************************");
        }
    }

    @Override
    public void execute() {
        int[] activeSites = context.getActiveSites();

        if (debugFlag) {
            System.out.println("\n=======================================================");
            System.out.println("Executing execute() block, executing TestSuite: "
                    + context.getTestSuiteName());
            System.out.println("With protocolAwareCommand : " + protocolAwareCommand);
        }

        //Check if the information is concatenated with ";"
        String[] protocolAwareCommandArrays = protocolAwareCommand.split(";");

        int index = 0;

        for (String tmpProtocolAwareCommand : protocolAwareCommandArrays)
        {

            mainProtocol.updateProtocolAwareCommand(debugFlag, stringToStringArray(tmpProtocolAwareCommand));

            List<ProtocolAwareData> lResult = null;

            measurement.execute();
            lResult = mainProtocol.getPAMeasurement();

            if (lResult == null) {
                for (int site : activeSites) {
                    if (index == 0)
                    {
                        protocolAwareResult.set(site, String.valueOf(measurement.hasPassed(site)));
                    }
                    else
                    {
                        protocolAwareResult.set(site, protocolAwareResult.get(site) + ";" + String.valueOf(measurement.hasPassed(site)));
                    }                }
            } else {
                for (ProtocolAwareData p : lResult) {
                    if (p.getData() == null) {
                        if (p.getBitSequence() == null) {
                            System.out.println("Something is wrong, Protocol Aware result equal to null");
                        } else {
                            for (int site : activeSites) {
                                if (index == 0)
                                {
                                    protocolAwareResult.set(site,
                                            convertBitSequenceToBinaryString(p.getBitSequence().get(site)));
                                }
                                else
                                {
                                    protocolAwareResult.set(site, protocolAwareResult.get(site) + ";" + convertBitSequenceToBinaryString(p.getBitSequence().get(site)));
                                }
                            }
                        }
                    } else {
                        for (int site : activeSites) {
                            if (index == 0)
                            {
                                protocolAwareResult.set(site, convertLongToHexString(p.getData().get(site)));
                            }
                            else
                            {
                                protocolAwareResult.set(site, protocolAwareResult.get(site) + ";" + convertLongToHexString(p.getData().get(site)));
                            }
                        }
                    }
                }
            }
            //index will be used to insert ";" into return string;
            index++;
        }
        if (debugFlag) {
            println("From ExecuteProtocolAware.protocolAwareResult = " + protocolAwareResult);
            System.out.println("=======================================================");
            System.out.println();
        }

    }

    /**
     * @param _long
     *            - Input parameter in Long datatype, which should be called when the result from
     *            Protocol Aware is UnsignedLong type.
     * @return return String type data in Hex format. Prefix "0x" will be added.
     */
    private static String convertLongToHexString(Long _long) {
        return "0x" + Long.toHexString(_long);
    }

    /**
     * @param _bs
     *            - Input parameter in BitSequence, which should be called when the result from
     *            Protocol Aware is BitSequence type.
     *            - In this method, BitSequence data will be extracted 4 bits at a time to construct
     *            equivalent hex string.
     * @return return String type data in Hex format. Prefix "0x" will be added.
     */
    private static String convertBitSequenceToBinaryString(BitSequence _bs)
    {
        StringBuilder sb = new StringBuilder("0x");
        long bitSequenceLength = _bs.length();
        long numberOfFullHexString = bitSequenceLength/4; //Need to divide by 4, as 4 bits construct 1 hex string character
        long remainingBits = bitSequenceLength % 4;

        // processing remaining bits from the left
        if (remainingBits != 0) {
            int tmpChar =0;
            for (int j=0; j < remainingBits; j++) {
                if (_bs.get(j)) {
                    tmpChar += (1L << (remainingBits -j - 1));
                }
            }
        sb.append(Long.toHexString(tmpChar));
        }

        //processing the rest of the bits (left = MSB, right = LSB)
        for (int i = 0; i < numberOfFullHexString; i++) {
            int tmpChar = 0;
            for (int j=0; j<4; j++) {
                if (_bs.get((int) remainingBits + i*4 + j)) {
                    tmpChar += (1L << 3-j); //shift in reverse order
                }
            }
            sb.append(Long.toHexString(tmpChar));
        }
        return sb.toString();
    }

    /**
     * @param _commandString
     *            - Protocol Aware command String from GUI/test suite input parameters
     * @return String[] type, where all the spaces are trimmed.
     */
    private static String[] stringToStringArray(String _commandString)
    {
        String[] trimmedArray = _commandString.trim().split("\\s*,\\s*");
        return trimmedArray;
    }
}
