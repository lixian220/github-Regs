spec SERIAL_LOOPBACKSignalRoleMapping {
    /**
    * Signal Role Mapping for SERIAL_PROTOCOL
    * All the input pins are mapped to AD0-AD3
    * The only output pin, TDO are mapped to IO1 (pair with AD1/TDI through cross-connect board)
    */
    protocolInterface common.protocols.SERIAL_LOOPBACK SERIAL_LOOPBACK {
     TCK      = AD0;
     TDI      = AD1;
     TDO      = IO1;
     TMS      = AD2;
     TRSTN    = AD3;
    }
}
