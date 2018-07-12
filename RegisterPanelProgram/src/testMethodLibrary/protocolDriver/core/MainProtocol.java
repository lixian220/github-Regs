/*******************************************************************************
 * Copyright (c) 2017 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/

package testMethodLibrary.protocolDriver.core;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.DeviceSetupUncheckedException;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.ITestContext;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.MultiSiteLongArray;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IProtocolInterfaceResults;
import xoc.dta.resultaccess.ITransactionSequenceResults;
import xoc.dta.resultaccess.datatypes.MultiSiteBitSequence;
import xoc.dta.resultaccess.datatypes.MultiSiteBitSequenceArray;
import xoc.dta.workspace.IWorkspace;

/**
 * MainProtocol is java class utility class created to allow ease of extension for Protocol Aware related test.<p><p>
 * This utility class calls two data utilities class (ProtocolAwareData.java and TransactionSequenceDefinitionData.java)
 * for protocol aware setup and data handling.
 * <p><p>
 * Information needed for Java Reflection is stored in nested class, &lt;Protocol&gt;
 * <p>,<p>
 */
public class MainProtocol {
    private Class<?>[] emptyParam = {};
    private Class<?>[] protocolParam      = new Class[] { IDeviceSetup.class, String[].class };
    private Class<?>[] seqTranParam       = new Class[] { TransactionSequenceDefinitionData.class };
    private Class<?>[] updateSpecParam    = new Class[] { Boolean.class, IMeasurement.class, String[].class };

    private String sPrevProt = "";
    private boolean bDebug = false;
    private Protocol protObj = null;
    private String sTestSuiteName;
    private String sProtocolName;
    private String sDevPath;
    private boolean bHasCapture = false;
    private IMeasurement meas;
    private IDeviceSetup devSetup;
    private ISetupTransactionSeqDef tsDef;
    private TransactionSequenceDefinitionData tranSeqDef;
    private List<ProtocolAwareData> lPAData = null;
    private Map<String, Protocol> mProt_ProtInterface = new HashMap<String, Protocol>();
    private Map<String, ISetupTransactionSeqDef> mProt_TranSeqDef = new HashMap<String, ISetupTransactionSeqDef>();

    private static final String PathToCustomizedProtocolAwareTestMethod = "testMethodLibrary.protocolDriver.custom.";
    private static final String PathToDotProtocolAwareSetupFiles = "/src/common/protocols/";

    /**
     * Constructor for MainProtocol, which is instantiated from Test Method ExecuteProtocolAware.java
     * @param _meas reference to IMeasurement object
     * @param _context reference to TestMethod ITestContext
     * @param _ds reference to IDeviceSetup, which is used for DS-API generation
     */
    public MainProtocol(IMeasurement _meas, ITestContext _context, IDeviceSetup _ds)
    {
        sTestSuiteName = _context.getTestSuiteName();
        meas = _meas;
        sDevPath = IWorkspace.getActiveProjectPath();
        if (_ds == null) {
            devSetup = DeviceSetupFactory.createInstance();
        } else {
            devSetup = _ds;
        }
        if (bDebug) {
            println("DevPath        = " + sDevPath);
            println("TestSuite Name = " + sTestSuiteName);
        }
    }

    /**
     * This method is used to retrieve protocolAwareCommand from ExecuteProtocolAware.java<p>
     * The String will be used to generate protocol aware spec file in customized &lt;Protocol&gt;.java file,
     * through Java Reflection
     * @param _sProtCmd Protocol Aware Command String from Test Method input parameter protocolAwareCommand
     */
    public void PASequencer(String[] _sProtCmdArray)
    {
        devSetup.importSpec(meas.getSpecificationName());
        reflectionPA(_sProtCmdArray);
    }

    /**
     * Return whether current protocol aware is setup with capture<p>
     *
     * @return boolean type. <b>true</b>= setup with PA capture, <b>false</b> setup without PA capture
     */
    public boolean hasCapture()
    {
        return bHasCapture;
    }

    /**
     * Return a list of ProtocolAwareData objects to the caller.<p>
     * Contents of the List will be processed, to retrieve Protocol Aware Capture data
     * @return List<ProtocolAwareData>
     */
    public List<ProtocolAwareData> getPAMeasurement()
    {
        ProtocolAwareData paData;
        MultiSiteLongArray msla = null;
        MultiSiteBitSequenceArray msba = null;
        if (tranSeqDef.getCaptIndex() ==0)
        {
            return null;
        }
        IProtocolInterfaceResults protocolIF = meas.protocolInterface(sProtocolName)
                .preserveResults();
        ITransactionSequenceResults[] tSeqResults = protocolIF.transactSeq(tranSeqDef.getTransSeqCallName());

        lPAData = new ArrayList<>();

        for (ITransactionSequenceResults res : tSeqResults)
        {
            msla = res.getValueAsLongArray(sProtocolName + "output");
            if (msla==null)
            {
                msba = res.getValueAsBitSequenceArray(sProtocolName + "output");
                if (msba==null)
                {
                    println("Read is wrong both UnsignedLong and BitSequence value is empty");
                }
                else
                {
                    MultiSiteBitSequence msb=null;
                    for (int i=0;i<msba.length();i++)
                    {
                        msb=msba.getElement(i);
                        paData=new ProtocolAwareData();
                        paData.setBitSequence(msb);
                        lPAData.add(paData);
                    }
                }
            }
            else
            {
                MultiSiteLong msl=null;
                int iSize=msla.length();
                for (int i=0;i<iSize;i++)
                {
                    msl=msla.getElement(i);
                    paData = new ProtocolAwareData();
                    paData.setData(msl);
                    lPAData.add(paData);
                }
            }
        }
        return lPAData;
    }

    /**
     * This method is called in execute() block in calling Test Method.<p><p>
     * For implementation with spec variable in specification file, user do not need to rerun
     * setup() block. User should program updateSpecVariable method in &lt;Protocol&gt;.java file,
     * so that correct spec variable is populated prior IMeasurement.execute()<p><p>
     * @param _sProtCmd - String Array type, full protocol commands to be passed into customized &lt;Protocol&gt;.java,
     * through Java Reflection
     */
    public void updateProtocolAwareCommand(boolean debugFlag, String[] protCommandArray)
    {
        Method methodObj;
        try {
            methodObj = protObj.cls.getDeclaredMethod("updateSpecVariable", updateSpecParam);
            methodObj.invoke(protObj.obj, new Object[] { Boolean.valueOf(debugFlag), meas, protCommandArray });
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void reflectionPA(String[] _m)
    {
        sProtocolName       = _m[1];
        String methodName   = _m[0];
        try {
            if (sProtocolName.equals(sPrevProt))
            {
                protObj = mProt_ProtInterface.get(sProtocolName);
            }
            else
            {
                protObj = createNewProtocol();
                tsDef = createTranSeqDef(sProtocolName, protObj.getPAInterface());
                tranSeqDef = new TransactionSequenceDefinitionData(sProtocolName, tsDef);

                // declare and invoke setTransactionDef()
                Method methodObj = protObj.cls.getDeclaredMethod("setTransactionDef", seqTranParam);
                methodObj.invoke(protObj.obj, tranSeqDef);
                sPrevProt = sProtocolName;
            }
            // declare and invoke methods in <Protocol>.java through Java Reflection
            Method methodObj = protObj.cls.getDeclaredMethod(methodName, protocolParam);
            Byte bCapt = (Byte) methodObj.invoke(protObj.obj, new Object[] { devSetup, _m });
            if (bCapt == 1)
            {
                bHasCapture = true;
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    private ISetupTransactionSeqDef createTranSeqDef(String _protName,
            ISetupProtocolInterface _paInterface)
    {
        ISetupTransactionSeqDef tsdef;
        String sTranSeqDefName = replaceDotWithUnderScore(sTestSuiteName + _protName);
        tsdef = mProt_TranSeqDef.get(sTranSeqDefName);
        if (tsdef == null)
        {
            tsdef = _paInterface.addTransactionSequenceDef(sTranSeqDefName);
            mProt_TranSeqDef.put(sTranSeqDefName, tsdef);
        }
        return tsdef;
    }

    private Protocol createNewProtocol()
    {
        ISetupProtocolInterface paInterface = null;
        protObj = new Protocol();
        try
        {
            paInterface = createProtocolInterface(protObj, true);
            Object obj;
            Class<?> cls = Class.forName(PathToCustomizedProtocolAwareTestMethod + sProtocolName);
            Constructor<?> constructor = cls.getConstructor(emptyParam);
            obj = constructor.newInstance(new Object[] {});

            protObj.setPaInterface(paInterface);
            mProt_ProtInterface.put(sProtocolName, protObj);
            protObj.setPAClass(cls);
            protObj.setPAObject(obj);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return protObj;
    }

    private String replaceDotWithUnderScore(String _str)
    {
        String tmpStr = _str.replace('.', '_');
        return tmpStr;
    }

    private ISetupProtocolInterface createProtocolInterface(Protocol _prot,
            boolean _bThrowException) throws FileNotFoundException, DeviceSetupUncheckedException
    {
        ISetupProtocolInterface paInterface = null;
        String sProtFileLoc = sDevPath + PathToDotProtocolAwareSetupFiles + sProtocolName;
        String sProtFile = sProtFileLoc + ".prot";
        String sProtSignalRoleFile = sProtFileLoc + "SignalRoleMapping.spec";
        String sProt = "common.protocols." + sProtocolName;

        if (mProt_ProtInterface.containsKey(sProtocolName))
        {
            return mProt_ProtInterface.get(sProtocolName).getPAInterface();
        }

        if (isFileExists(sProtFile) && isFileExists(sProtSignalRoleFile))
        {
            paInterface = devSetup.addProtocolInterfaceWithSignalMappingSpec(sProtocolName, sProt,
                    sProt + "SignalRoleMapping");
        }

        if (bDebug)
        {
            println("ds.addProtocolInterfaceWithSignalMappingSpec(" + sProtocolName + ", " + sProt
                    + "," + sProt + "SignalRoleMapping)");
        }
        return paInterface;
    }

    private void println(String s)
    {
        System.out.println(s);
    }

    private static final boolean isFileExists(String _filename) throws FileNotFoundException
    {
        Path tmpPath = null;
        try
        {
            tmpPath = Paths.get(IWorkspace.getActiveProjectPath()).resolve(_filename);
        }
        catch(Exception e)
        {
            throw new FileNotFoundException("File " + _filename + " not found!!");
        }

        if (null == tmpPath)
        {
            return false;
        }
        return tmpPath.toFile().exists();
    }
}
/**
 * Protocol nested class is used to store information on Protocol Aware under setup<p>
 * <p>
 * This class contains class name, reference to customized protocol aware object and method map,
 * which are used for Java Reflection. <p>
 * <p>
 * All the customized protocol aware related tasks are invoked store in &lt;Protocol&gt;.java customized
 * utility class, and invoke through Java Reflection
 */
class Protocol {
    private ISetupProtocolInterface paInterface;
    Class<?> cls;
    Object obj;
//    Map<String, Method> mMethod;

    /*
     * Setter for class to customize Protocol Aware Utility class.
     * This method is only called in MainProtocol.createNewProtocol()
     */
    void setPAClass(Class<?> _cls)
    {
        cls = _cls;
    }

    /*
     * Setter for class to customize Protocol Aware Utility Object.
     * This method is only called in MainProtocol.createNewProtocol()
     */
    void setPAObject(Object _obj)
    {
        obj = _obj;
    }

    /**
     * Setter for ISetupProtocolInterface type field, which is needed for DS-API setup generation
     * @param _paInterface ISetupProtocolInterface type
     */
    void setPaInterface(ISetupProtocolInterface _paInterface)
    {
        paInterface = _paInterface;
    }

    /**
     * Getter for ISetupProtocolInterface type field, which is needed for DS-API setup generation
     */
    ISetupProtocolInterface getPAInterface()
    {
        return paInterface;
    }
}

