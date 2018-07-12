/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package Panel.DCPanel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI;
import xoc.dsa.ISetupDcVI.IDisconnect;
import xoc.dsa.ISetupDcVI.IIforceVmeas;
import xoc.dsa.ISetupDcVI.IIforceVmeas.SetupKeepVclamp;
import xoc.dsa.ISetupDcVI.IImeas;
import xoc.dsa.ISetupDcVI.IImeas.SetupUngangMode;
import xoc.dsa.ISetupDcVI.IVforceImeas;
import xoc.dsa.ISetupDcVI.IVmeas;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.alarm.IAlarm;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteDoubleArray;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDcVIResults;

/**
 * This test method performs a dc test for dcVI instrument by using the setup data
 * specified in the DC panel view. It supports four types measure action:
 * <ol>
 * <li>Current force voltage measure</li>
 * <li>Voltage force current measure</li>
 * <li>Voltage measure</li>
 * <li>Current measure</li>
 * </ol>
 *
 * <b>Note:</b> The test method is only used to cooperate with DC panel during test flow debugging.
 *
 * @since 8.0.8
 * @see "DCPanel detailed descriptions in TDC (Topic 343200)"
 */
public class DCPanel extends TestMethod {

    /**
     * (Mandatory)<br>
     * Declares the field that is used to do current force voltage measure setting.
     * In this test, you specify the following parameters: <br>
     * <ol>
     * <li>signals: the DUT signals to test.</li>
     * <li>forceValue: the value of the current that will be forced on the DUT signals.</li>
     * <li>irange: The current range value.</li>
     * <li>averages: The number of measurements to execute for an averaged result value.</li>
     * <li>averagingTime: The time of how long the averaging should be performed.</li>
     * <li>highAccuracy: Specifies whether to enable or disable the high accuracy mode.</li>
     * <li>waitTime: Specifies the settling time between forcing the current and measuring the
     * voltage.</li>
     * <li>vrange: The voltage range value.</li>
     * <li>vexpected: The expected voltage at the DUT at the time when the action starts.</li>
     * <li>vclampLow: The low clamp voltage value.</li>
     * <li>vclampHigh: The high clamp voltage value.</li>
     * <li>keepVclamp: Control if voltage clamps are active after connect.</li>
     * <li>vDiffRange: The expected voltage at the DUT at the time when the action starts.</li>
     * </ol>
     *
     * @since 8.0.8
     */
    public ParameterGroupCollection<IfvmActionInfo> ifvmSignalGroup = new ParameterGroupCollection<>();

    /**
     * (Mandatory)<br>
     * Declares the field that is used to do voltage force current measure setting.
     * In this test, you specify the following parameters: <br>
     * <ol>
     * <li>signals: the DUT signals to test.</li>
     * <li>forceValue: The value of the voltage that will be forced on the DUT signals.</li>
     * <li>irange: The current range value.</li>
     * <li>vrange: The voltage range value.</li>
     * <li>averages: The number of measurements to execute for an averaged result value.</li>
     * <li>averagingTime: The time of how long the averaging should be performed.</li>
     * <li>restoreIrange: Restore the current range to the level set defined default.</li>
     * <li>downRanging: Enables autoranging by decreasing the current range until the
     * measurement is in range or the target range is reached.</li>
     * <li>highAccuracy: Specifies whether to enable or disable the high accuracy mode.</li>
     * <li>waitTime: Specifies the settling time between forcing the current and measuring the
     * voltage.</li>
     * <li>fastPrecharge: Sets the current clamps to the maximum value for the specified time,
     * before they are set to the value programmed by the iclamp property.</li>
     * </ol>
     *
     * @since 8.0.8
     */
    public ParameterGroupCollection<VfimActionInfo> vfimSignalGroup = new ParameterGroupCollection<>();

    /**
     * (Mandatory)<br>
     * Declares the field that is used to do voltage measure setting.
     * In this test, you specify the following parameters: <br>
     * <ol>
     * <li>signals: the DUT signals to test.</li>
     * <li>averages: The number of measurements to execute for an averaged result value.</li>
     * <li>averagingTime: The time of how long the averaging should be performed.</li>
     * <li>waitTime: Specifies the settling time between forcing the current and measuring the
     * voltage.</li>
     * <li>vrange: The voltage range value.</li>
     * <li>highAccuracy: Specifies whether to enable or disable the high accuracy mode.</li>
     * </ol>
     *
     * @since 8.0.8
     */
    public ParameterGroupCollection<VmActionInfo> vmSignalGroup = new ParameterGroupCollection<>();

    /**
     * (Mandatory)<br>
     * Declares the field that is used to do current measure setting.
     * In this test, you specify the following parameters: <br>
     * <ol>
     * <li>signals: the DUT signals to test.</li>
     * <li>irange: The current range value.</li>
     * <li>averages: The number of measurements to execute for an averaged result value.</li>
     * <li>averagingTime: The time of how long the averaging should be performed.</li>
     * <li>restoreIrange: Restore the current range to the level set defined default.</li>
     * <li>downRanging: Enables autoranging by decreasing the current range until the
     * measurement is in range or the target range is reached.</li>
     * <li>ungangMode: Specifies ungang mode.</li>
     * <li>highAccuracy: Specifies whether to enable or disable the high accuracy mode.</li>
     * <li>waitTime: Specifies the settling time between forcing the current and measuring the
     * voltage.</li>
     * </ol>
     *
     * @since 8.0.8
     */
    public ParameterGroupCollection<ImActionInfo> imSignalGroup = new ParameterGroupCollection<>();

    /**
     * (Mandatory)<br>
     * Dummy signals used to create default specification file and operating sequence file when
     * running debug as test program.
     *
     * @since 8.0.8
     */
    public String dummySignals = "";

    /**
     * The measured results info return to DC panel.
     *
     * @since 8.0.8
     */
    @Out
    public Map<String, Map<String, String[]>> measuredResults = new HashMap<>();

    /**
     * Defines the measurement object associated with this test method.
     *
     * @since 8.0.8
     */
    private IMeasurement measurement ;

    /**
     * Store all actions setup info.
     */
    private final List<ActionInfo> allActionGroup = new LinkedList<>();

    /**
     * Whether all actions' signal empty.
     */
    private boolean isAllActionsSignalEmpty = true;

    private static final String SITE_INFO = "SiteInfo";

    private static final String ALARM_INFO = "AlarmInfo";

    @Override
    public void setup() {
        // 1. Clear the data to support to multi-calls, e.g. change parameters or switch action.
        allActionGroup.clear();
        allActionGroup.addAll(ifvmSignalGroup.values());
        allActionGroup.addAll(vfimSignalGroup.values());
        allActionGroup.addAll(vmSignalGroup.values());
        allActionGroup.addAll(imSignalGroup.values());

        for (ActionInfo actionInfo : allActionGroup) {
            if (!actionInfo.signals.isEmpty()){
                isAllActionsSignalEmpty = false;
                break;
            }
        }

        // 2. All actions signal is empty, it means pause at test flow break point when running debug as test program
        //or no valid data.
        if (isAllActionsSignalEmpty) {
            //Temporary codes begin, will be removed once supports empty measurement.
            if (!dummySignals.isEmpty()) {
                IDeviceSetup dummpyDeviceSetup = DeviceSetupFactory.createInstance();
                ISetupDcVI dcVI = dummpyDeviceSetup.addDcVI(dummySignals);
                dcVI.level().setVforce(0);
                IDisconnect disconnectActoion = dcVI.disconnect("disconnect");
                dummpyDeviceSetup.actionCall(disconnectActoion);
                measurement.setSetups(dummpyDeviceSetup);
            }
            //Temporary codes end.
            return;
        }

        // 3. Create action for each signal group.
        IDeviceSetup deviceSetup = DeviceSetupFactory.createInstance();
        deviceSetup.parallelBegin();
        {
            for (ActionInfo actionInfo : allActionGroup) {
                if (!actionInfo.signals.isEmpty()){
                    actionInfo.setup(deviceSetup);
                }
            }
        }
        deviceSetup.parallelEnd();

        // 4. Link setup info with the measurement.
        measurement.setSetups(deviceSetup);
    }

    @Override
    public void execute() {
        if(isAllActionsSignalEmpty) {
            return;
        }

        measurement.execute();

        // 1. Reserve measurement alarm info
        List<IAlarm> alarmList = measurement.getAlarms();

        // 2. Reserve each action's measure result.
        for (ActionInfo actionInfo : allActionGroup) {
            if (!actionInfo.signals.isEmpty()){
                actionInfo.reserveResult();
            }
        }

        // 3. Put the action measured result in map passed to DC panel.
        for (ActionInfo actionInfo : allActionGroup) {
            if (!actionInfo.signals.isEmpty()) {
                measuredResults.put(actionInfo.getId(), actionInfo.getPerSignalResult());
            }
        }

        // 4. Build the alarm info to the map for passing to DC panel.
        buildAlarmsInfo(alarmList);

        // 5. Build the site info to the map for passing to DC panel.
        buildSitesInfo();
    }

    /**
     * Build alarm info for passing to DC panel.
     * @param alarmList alarm list
     */
    private void buildAlarmsInfo(List<IAlarm> alarmList) {
        if (alarmList != null && !alarmList.isEmpty()) {

            Map<String, String[]> ret = new HashMap<>();
            StringBuffer allAlarmsSb = new StringBuffer("");

            for (int i = 0; i < alarmList.size(); i++) {
                IAlarm alarm = alarmList.get(i);
                StringBuffer alarmSb = new StringBuffer("");
                // alarm format: site,signal,alarmType
                // for example: 1,IO1,type1;1,IO2,type2
                alarmSb.append(alarm.getSite()).append(",").append(alarm.getSignal().getName()).append(",")
                        .append(alarm.getType().name());

                if (i == alarmList.size() - 1) {
                    allAlarmsSb.append(alarmSb);
                } else {
                    allAlarmsSb.append(alarmSb).append(";");
                }
            }

            ret.put(ALARM_INFO, new String[] { allAlarmsSb.toString() });

            measuredResults.put(ALARM_INFO, ret);
        }
    }

    /**
     * Build active sites info for passing to DC panel.
     */
    private void buildSitesInfo() {
        int[] sites = context.getActiveSites();
        String[] sitesStr = new String[sites.length];
        for (int i = 0; i < sites.length; i++) {
            sitesStr[i] = String.valueOf(sites[i]);
        }
        Map<String, String[]> sitesMap = new HashMap<>();
        sitesMap.put(SITE_INFO, sitesStr);

        measuredResults.put(SITE_INFO, sitesMap);
    }

    /**
     * Defines the class that represents the common parameter group info for dcVI instrument.
     *
     * @since 8.0.8
     */
    public abstract class ActionInfo extends ParameterGroup {

        /**
         * (Mandatory)<br>
         * Signal used to configure setup.
         *
         * @since 8.0.8
         */
        public String signals = "";

        /**
         * (Optional)<br>
         * The number of measurements to execute for an averaged result value.
         *
         *  @since 8.0.8
         */
        public String averages = "";

        /**
         * (Optional)<br>
         * The time of how long the averaging should be performed.
         *
         *  @since 8.0.8
         */
        public String averagingTime = "";

        /**
         * (Optional)<br>
         * Specifies whether to enable or disable the high accuracy mode.
         *
         * @since 8.0.8
         */
        public String highAccuracy = "";

        /**
         * (Optional)<br>
         * Specifies the settling time between forcing the current and measuring the
         * voltage.
         *
         * @since 8.0.8
         */
        public String waitTime = "";

        /**
         * Store the measured result.
         */
        IDcVIResults dcVIResult = null;

        /**
         * Create the concrete action setup info.
         *
         * @param deviceSetup
         */
        void setup(IDeviceSetup deviceSetup) {
            // empty
        }

        /**
         * Reserve the measured results.
         *
         * @since 8.0.8
         */
        void reserveResult() {
            dcVIResult = measurement.dcVI(signals).preserveResults();
        }

        /**
         * Convert the DTA multisiteArray type to multisite type.
         *
         * @return per signal result.
         *
         * @since 8.0.8
         */
        Map<String, String[]> getPerSignalResult() {
            Map<String, MultiSiteDoubleArray> perSignalResult = getMeasureResults();

            Map<String, String[]> ret = new HashMap<>();
            for (Entry<String, MultiSiteDoubleArray> entry : perSignalResult.entrySet()) {
                String signalName = entry.getKey();
                MultiSiteDouble measuredValue = entry.getValue().getElement(0);// Only one action;
                Double[] values = measuredValue.getData();

                String[] valuesStr = new String[values.length];

                for (int i = 0; i < values.length; i++) {
                    valuesStr[i] = String.valueOf(values[i]);
                }

                ret.put(signalName, valuesStr);
            }

            return ret;
        }

        /**
         * Get action's measured result.
         *
         * @return measured result.
         *
         * @since 8.0.8
         */
        abstract Map<String, MultiSiteDoubleArray> getMeasureResults();

    }

    /**
     * Defines the class that represents the parameter group for current force voltage measure action for dcVI instrument.
     *
     * @since 8.0.8
     */
    public class IfvmActionInfo extends ActionInfo {

        /**
         * (Mandatory)<br>
         * The value of the current that will be forced on the DUT signals.
         *
         * @since 8.0.2
         */
        public String forceValue = "";

        /**
         * (Optional)<br>
         * The current range value.
         *
         * @since 8.0.8
         */
        public String irange = "";

        /**
         * (Optional)<br>
         * The voltage range value.
         *
         * @since 8.0.8
         */
        public String vrange = "";

        /**
         * (Optional)<br>
         * The expected voltage at the DUT at the time when the action starts.
         *
         * @since 8.0.8
         */
        public String vexpected = "";

        /**
         * (Optional)<br>
         * The low clamp voltage value.
         *
         * @since 8.0.8
         */
        public String vclampLow = "";

        /**
         * (Optional)<br>
         * The high clamp voltage value.
         *
         * @since 8.0.8
         */
        public String vclampHigh = "";

        /**
         * (Optional)<br>
         * Control if voltage clamps are active after connect.
         *
         * @since 8.0.8
         */
        public String keepVclamp = "";

        /**
         * (Optional)<br>
         * The expected voltage at the DUT at the time when the action starts.
         *
         * @since 8.0.8
         */
        public String vDiffRange = "";

        /**
         * Create the ifvm action.
         *
         * @since 8.0.8
         */
        @Override
        void setup(IDeviceSetup deviceSetup) {
            IIforceVmeas ifvmAction = deviceSetup.addDcVI(signals).iforceVmeas(getId());

            ifvmAction.setForceValue(forceValue);

            if (!averages.isEmpty()) {
                ifvmAction.setAverages(averages);
            }

            if (!averagingTime.isEmpty()) {
                ifvmAction.setAveragingTime(averagingTime);
            }

            if (!highAccuracy.isEmpty()) {
                ifvmAction.setHighAccuracy(highAccuracy);
            }

            if (!waitTime.isEmpty()) {
                ifvmAction.setWaitTime(waitTime);
            }

            if (!irange.isEmpty()) {
                ifvmAction.setIrange(irange);
            }

            if (!vrange.isEmpty()) {
                ifvmAction.setVrange(vrange);
            }

            if (!vexpected.isEmpty()) {
                ifvmAction.setVexpected(vexpected);
            }

            if (!vclampLow.isEmpty()) {
                ifvmAction.setVclampLow(vclampLow);
            }

            if (!vclampHigh.isEmpty()) {
                ifvmAction.setVclampHigh(vclampHigh);
            }

            if (!keepVclamp.isEmpty()) {
                if (keepVclamp.equals(SetupKeepVclamp.auto.name())) {
                    ifvmAction.setKeepVclamp(SetupKeepVclamp.auto);
                } else if (keepVclamp.equals(SetupKeepVclamp.disabled.name())) {
                    ifvmAction.setKeepVclamp(SetupKeepVclamp.disabled);
                } else if (keepVclamp.equals(SetupKeepVclamp.enabled.name())) {
                    ifvmAction.setKeepVclamp(SetupKeepVclamp.enabled);
                }
            }

            if (!vDiffRange.isEmpty()) {
                ifvmAction.setVdiffRange(vDiffRange);
            }

            deviceSetup.actionCall(ifvmAction);
        }

        /**
         * Get the ifvm action measured results.
         *
         * @since 8.0.8
         */
        @Override
        Map<String, MultiSiteDoubleArray> getMeasureResults() {
            Map<String, MultiSiteDoubleArray> perSignalResult = dcVIResult.iforceVmeas(getId()).getVoltage();

            return perSignalResult;
        }

    }

    /**
     * Defines the class that represents the parameter group for voltage force current measure action for dcVI instrument.
     *
     * @since 8.0.8
     */
    public class VfimActionInfo extends ActionInfo {

        /**
         * (Mandatory)<br>
         * The value of the voltage that will be forced on the DUT signals.
         *
         * @since 8.0.8
         */
        public String forceValue = "";

        /**
         * (Mandatory)<br>
         * The current range value.
         *
         * @since 8.0.8
         */
        public String irange = "";

        /**
         * (Optional)<br>
         * The voltage range value.
         *
         * @since 8.0.8
         */
        public String vrange = "";

        /**
         * (Optional)<br>
         * Restore the current range to the level set defined default.
         *
         *  @since 8.0.8
         */
        public String restoreIrange = "";

        /**
         * (Optional)<br>
         * Enables autoranging by decreasing the current range until the measurement is in range
         * or the target range is reached.
         *
         *  @since 8.0.8
         */
        public String safeDownRanging = "";

        /**
         * (Optional)<br>
         *  Sets the current clamps to the maximum value for the specified time,
         *  before they are set to the value programmed by the iclamp property.
         *
         * @since 8.0.8
         */
        public String fastPrecharge = "";

        /**
         * Create the vfim action.
         *
         * @since 8.0.8
         */
        @Override
        void setup(IDeviceSetup deviceSetup) {
            IVforceImeas vfimAction = deviceSetup.addDcVI(signals).vforceImeas(getId());

            vfimAction.setForceValue(forceValue).setIrange(irange);

            if (!averages.isEmpty()) {
                vfimAction.setAverages(averages);
            }

            if (!averagingTime.isEmpty()) {
                vfimAction.setAveragingTime(averagingTime);
            }

            if (!highAccuracy.isEmpty()) {
                vfimAction.setHighAccuracy(highAccuracy);
            }

            if (!waitTime.isEmpty()) {
                vfimAction.setWaitTime(waitTime);
            }

            if (!vrange.isEmpty()) {
                vfimAction.setVrange(vrange);
            }

            if (!restoreIrange.isEmpty()) {
                vfimAction.setRestoreIrange(restoreIrange);
            }

            if (!safeDownRanging.isEmpty()) {
                if (safeDownRanging.equals(IVforceImeas.SetupSafeDownRanging.auto.name())) {
                    vfimAction.setSafeDownRanging(IVforceImeas.SetupSafeDownRanging.auto);
                } else if (safeDownRanging.equals(IVforceImeas.SetupSafeDownRanging.enabled.name())) {
                    vfimAction.setSafeDownRanging(IVforceImeas.SetupSafeDownRanging.enabled);
                } else if (safeDownRanging.equals(IVforceImeas.SetupSafeDownRanging.disabled.name())) {
                    vfimAction.setSafeDownRanging(IVforceImeas.SetupSafeDownRanging.disabled);
                }
            }

            if (!fastPrecharge.isEmpty()) {
                vfimAction.setFastPrecharge(fastPrecharge);
            }

            deviceSetup.actionCall(vfimAction);
        }

        /**
         * Get the vfim action measured results.
         *
         * @since 8.0.8
         */
        @Override
        Map<String, MultiSiteDoubleArray> getMeasureResults() {
            Map<String, MultiSiteDoubleArray> perSignalResult = dcVIResult.vforceImeas(getId()).getCurrent();

            return perSignalResult;
        }
    }

    /**
     * Defines the class that represents the parameter group for voltage measure action for dcVI instrument.
     *
     * @since 8.0.8
     */
    public class VmActionInfo extends ActionInfo {

        /**
         * (Optional)<br>
         * The voltage range value.
         *
         * @since 8.0.8
         */
        public String vrange = "";

        /**
         * Create the vm action.
         *
         * @since 8.0.8
         */
        @Override
        void setup(IDeviceSetup deviceSetup) {
            IVmeas vmAction = deviceSetup.addDcVI(signals).vmeas(getId());

            if (!averages.isEmpty()) {
                vmAction.setAverages(averages);
            }

            if (!averagingTime.isEmpty()) {
                vmAction.setAveragingTime(averagingTime);
            }

            if (!highAccuracy.isEmpty()) {
                vmAction.setHighAccuracy(highAccuracy);
            }

            if (!waitTime.isEmpty()) {
                vmAction.setWaitTime(waitTime);
            }

            if (!vrange.isEmpty()) {
                vmAction.setVrange(vrange);
            }

            deviceSetup.actionCall(vmAction);
        }

        /**
         * Get the vm action measured results.
         *
         * @since 8.0.8
         */
        @Override
        Map<String, MultiSiteDoubleArray> getMeasureResults() {
            Map<String, MultiSiteDoubleArray> perSignalResult = dcVIResult.vmeas(getId()).getVoltage();

            return perSignalResult;
        }

    }

    /**
     * Defines the class that represents the parameter group for current measure action for dcVI instrument.
     *
     * @since 8.0.8
     */
    public class ImActionInfo extends ActionInfo {
        /**
         * (Mandatory)<br>
         * The current range value.
         *
         * @since 8.0.8
         */
        public String irange = "";

        /**
         * (Optional)<br>
         * Restore the current range to the level set defined default.
         *
         *  @since 8.0.8
         */
        public String restoreIrange = "";

        /**
         * (Optional)<br>
         * Enables autoranging by decreasing the current range until the measurement is in range
         * or the target range is reached.
         *
         *  @since 8.0.8
         */
        public String safeDownRanging = "";

        /**
         * (Optional)<br>
         * Ungange Mode.
         */
        public String ungangMode = "";

        /**
         * Create the im action.
         *
         * @since 8.0.8
         */
        @Override
        void setup(IDeviceSetup deviceSetup) {
            IImeas imAction = deviceSetup.addDcVI(signals).imeas(getId());

            imAction.setIrange(irange);

            if (!averages.isEmpty()) {
                imAction.setAverages(averages);
            }

            if (!averagingTime.isEmpty()) {
                imAction.setAveragingTime(averagingTime);
            }

            if (!highAccuracy.isEmpty()) {
                imAction.setHighAccuracy(highAccuracy);
            }

            if (!waitTime.isEmpty()) {
                imAction.setWaitTime(waitTime);
            }

            if (!restoreIrange.isEmpty()) {
                imAction.setRestoreIrange(restoreIrange);
            }

            if (!safeDownRanging.isEmpty()) {
                if(safeDownRanging.equals(IImeas.SetupSafeDownRanging.auto.name())) {
                    imAction.setSafeDownRanging(IImeas.SetupSafeDownRanging.auto);
                } else if (safeDownRanging.equals(IImeas.SetupSafeDownRanging.enabled.name())) {
                    imAction.setSafeDownRanging(IImeas.SetupSafeDownRanging.enabled);
                } else if (safeDownRanging.equals(IImeas.SetupSafeDownRanging.disabled.name())) {
                    imAction.setSafeDownRanging(IImeas.SetupSafeDownRanging.disabled);
                }
            }

            if (!ungangMode.isEmpty()) {
                if (ungangMode.equals(SetupUngangMode.fast.name())) {
                    imAction.setUngangMode(SetupUngangMode.fast);
                } else if (ungangMode.equals(SetupUngangMode.never.name())) {
                    imAction.setUngangMode(SetupUngangMode.never);
                }
            }

            deviceSetup.actionCall(imAction);
        }

        /**
         * Get the im action measured results.
         *
         * @since 8.0.8
         */
        @Override
        Map<String, MultiSiteDoubleArray> getMeasureResults() {
            Map<String, MultiSiteDoubleArray> perSignalResult = dcVIResult.imeas(getId()).getCurrent();

            return perSignalResult;
        }
    }

}
