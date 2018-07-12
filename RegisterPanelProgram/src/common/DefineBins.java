package common;

import xoc.dta.TestMethod;
import xoc.dta.binning.IBinTable;
import xoc.dta.binning.IBinning.Color;
import xoc.dta.binning.IBinning.ResultType;

/**
 * An example test method using test descriptor
 */
public class DefineBins extends TestMethod {

    @Override
    public void execute() {
        IBinTable binTable = context.binning().binTable();

        binTable.addHardBin(1, "passed", ResultType.PASS);
        binTable.addSoftBin(1, 1, "passed", ResultType.PASS, 0, Color.GREEN);

        binTable.addHardBin(2, "failed", ResultType.FAIL);
        binTable.addSoftBin(2, 2, "failed", ResultType.FAIL, 0, Color.RED);
    }
}
