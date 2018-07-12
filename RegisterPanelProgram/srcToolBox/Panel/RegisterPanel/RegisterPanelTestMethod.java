/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package Panel.RegisterPanel;

import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteString;

public class RegisterPanelTestMethod extends TestMethod {

    /**
     * This field is used to send in command(s) from Register Panel GUI <br>
     * <br>
     * User can process this string for DS-API generation in setup() block, or
     * use it to update variables in execute() block, if Protocol Aware is
     * implemented with spec variables.
     */
    public String protocolAwareCommand;

    /**
     * This field is used to return result from Test Method back to Register Panel GUI <br>
     * <br>
     * Register Panel GUI will retrieve result through protocolAwareResult parameter,
     * which is MultiSiteString type, containing Hex String result with 0x prefix.
     * Register Panel GUI will process this per site result, and populate data onto Register Panel GUI
     */
    public MultiSiteString protocolAwareResult;

    @Override
    public void setup() {

        super.setup();

        println("\n*******************************************************");
        println("Executing setup() block of Testsuite " + context.getTestSuiteName());
        println("Value of input parameter protocolAwareCommand in setup() block = ");
        println(protocolAwareCommand);
        //Enable this block, to split concatenated string from multiple transactions, pass in from GUI code
        if (protocolAwareCommand != null)
        {
            //String[] stringArray = protocolAwareCommand.split(";");
        }
        println("*******************************************************\n");
    }

    @Override
    public void execute() {

        //Enable this block, to split concatenated string from multiple transactions, pass in from GUI code
        //String[] stringArray = protocolAwareCommand.split(";");

        //print the value of input parameter protocolAwareCommand
        println("\n=======================================================");
        println("Executing execute() block of Testsuite " + context.getTestSuiteName());
        println("Value of input parameter protocolAwareCommand in execute() block = ");
        println(protocolAwareCommand);
        println("=======================================================");

        //Implement measurement execution here:


        //Return result as 0x Hex String through protocolAwareData
        //In the sample implementation, site string data = site number
        //Edit codes below to return actual protocol aware capture result in Hex String back to GUI
        protocolAwareResult = new MultiSiteString();
        for (int site : context.getActiveSites())
        {
            protocolAwareResult.set(site, "0x"+Long.toHexString(site));
        }

        println("Hex String return back to Register Panel GUI (through protocolAwareResult) = ");
        println(protocolAwareResult);
        println("=======================================================\n");
    }
}
