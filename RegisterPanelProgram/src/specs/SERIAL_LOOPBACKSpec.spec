import common.groups;

/**
 * Specification File for SERIAL_LOOPBACK Protocol. This file contains timing/level/wavetable properties for
 * SERIAL_LOOPBACK protocol
 */
spec SERIAL_LOOPBACKSpec {

    set exampleLev;
    set exampleTiming;

    var Voltage someVoltage = 1.5 V;
    var Time somePeriod = 50 ns;

    setup digInOut SerialIO  {
        result.capture.enabled = true;
        wavetable wvt1 {
          xModes = 1;

          0 : d1:0;
          1 : d1:1;
          L : d1:Z r1:L;
          H : d1:Z r1:H;
          C : d1:Z r1:C;
          X : d1:Z;
        }

        set level exampleLev {
            vih = someVoltage * 2;
            vil = 0 V;
            voh = someVoltage+0.1 V;
            vol = someVoltage-0.1 V;
            term= R50Ohm;
            vt = someVoltage;
        }

        set timing exampleTiming {
            period = somePeriod;
            d1 = 0 ns;
            r1 = 0.75 * somePeriod;
        }
    }
    setup digInOut SerialAD {
        result.capture.enabled = true;
        wavetable wvt1 {
          xModes = 1;

          0 : d1:0;
          1 : d1:1;
          L : d1:Z r1:L;
          H : d1:Z r1:H;
          P : d1:0 d2:1;
          X : d1:Z;
        }

        set level exampleLev {
            vih = someVoltage * 2;
            vil = 0 V;
            voh = someVoltage+0.1 V;
            vol = someVoltage-0.1 V;
            term= R50Ohm;
            vt = someVoltage;
        }

        set timing exampleTiming {
            period = somePeriod;
            d1 = 0 ns;
            d2 = 0.5 *  somePeriod;
            r1 = 0.75 * somePeriod;
        }
    }
}
