import common.groups;

/**
 * Specification file for PARALLEL_LOOPBACK Protocol. This file contains timing/level/wavetable properties for
 * PARALLEL_LOOPBACK protocol
 */
spec PARALLEL_LOOPBACKSpec {

    set exampleLev;
    set exampleTiming;

    var Voltage someVoltage = 1.5 V;
    var Time somePeriod = 50 ns;

    setup digInOut allio  {
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
            r1 = somePeriod/2;
        }
    }
    setup digInOut allad {
        result.capture.enabled = true;
        wavetable wvt1 {
          xModes = 1;

          0 : d1:0;
          1 : d1:1;
          L : d1:Z r1:L;
          H : d1:Z r1:H;
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
            r1 = somePeriod/2;
        }
    }
}
