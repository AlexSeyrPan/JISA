package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class VISA {

    private static       VISANativeInterface       lib;
    private static final String                    OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static       String                    libName;
    private static final String                    responseEncoding = "UTF8";
    private static final long                      VISA_ERROR       = 0x7FFFFFFF;
    private static final int                       _VI_ERROR        = -2147483648;
    private static final int                       VI_SUCCESS       = 0;
    private static final int                       VI_NULL          = 0;
    private static final int                       VI_TRUE          = 1;
    private static final int                       VI_FALSE         = 0;
    private static       NativeLong                visaResourceManagerHandle;
    private static       HashMap<Long, NativeLong> instruments      = new HashMap<>();

    static {

        if (OS_NAME.contains("win")) {

            try {
                libName = "tkVisa64";
                lib = (VISANativeInterface) Native.loadLibrary(libName, VISANativeInterface.class);
            } catch (UnsatisfiedLinkError e) {

            }
            if (lib == null) {
                libName = "nivisa64";
                lib = (VISANativeInterface) Native.loadLibrary(libName, VISANativeInterface.class);
            }
        } else if (OS_NAME.contains("linux")) {
            libName = "libvisa";
            lib = (VISANativeInterface) Native.loadLibrary(libName, VISANativeInterface.class);
        }

        if (lib == null) {
            System.err.println("Could not open VISA libaray!");
            System.exit(1);
        }

        try {
            visaResourceManagerHandle = getResourceManager();
        } catch (VISAException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static NativeLong getResourceManager() throws VISAException {

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong            visaStatus = lib.viOpenDefaultRM(pViSession);

        if (visaStatus.longValue() != VI_SUCCESS) {
            throw new VISAException("Error opening resource manager!");
        }
        return pViSession.getValue();

    }

    private static ByteBuffer stringToByteBuffer(String source) {
        try {
            ByteBuffer dest = ByteBuffer.allocate(source.length() + 1);
            dest.put(source.getBytes(responseEncoding));
            dest.position(0);
            return dest;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static InstrumentAddress[] getInstruments() throws VISAException {

        ByteBuffer            expr       = stringToByteBuffer("?*");
        ByteBuffer            desc       = ByteBuffer.allocate(1024);
        NativeLongByReference listHandle = new NativeLongByReference();
        NativeLongByReference listCount  = new NativeLongByReference();
        NativeLong status = lib.viFindRsrc(
                visaResourceManagerHandle,
                expr,
                listHandle,
                listCount,
                desc
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error searching devices");
        }

        int                 count     = listCount.getValue().intValue();
        InstrumentAddress[] addresses = new InstrumentAddress[count];
        NativeLong          handle    = listHandle.getValue();

        for (int i = 0; i < count; i++) {
            final String addr;
            try {
                addr = new String(desc.array(), 0, 1024, responseEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new VISAException("Unable to encode address!");
            }
            addresses[i] = () -> addr;
            status = lib.viFindNext(handle, desc);

            if (status.longValue() != VI_SUCCESS) {
                break;
            }

        }

        return addresses;

    }

    public static int openInstrument(String address) throws VISAException {

        NativeLong            visaStatus;
        NativeLong            visaInstrumentHandle;
        NativeLongByReference pViInstrument = new NativeLongByReference();

        ByteBuffer pViString = stringToByteBuffer(address);
        if (pViString == null) {
            throw new VISAException("Error encoding address to ByteBuffer.");
        }
        visaStatus = lib.viOpen(
                visaResourceManagerHandle,
                pViString,         // byte buffer for instrument string
                new NativeLong(0), // access mode (locking or not). 0:Use Visa default
                new NativeLong(0), // timeout, only when access mode equals locking
                pViInstrument      // pointer to instrument object
        );

        if (visaStatus.longValue() == VI_SUCCESS) {
            int handle = instruments.size();
            instruments.put(pViInstrument.getValue().longValue(), pViInstrument.getValue());
            return handle;
        } else {
            throw new VISAException("Could not open device: \"%s\"!", address);
        }


    }

    public static void write(long instrument, String toWrite) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        ByteBuffer pBuffer = stringToByteBuffer(toWrite);
        if (pBuffer == null) {
            throw new VISAException("Error converting command to ByteBuffer");
        }

        long writeLength = toWrite.length();

        NativeLongByReference returnCount = new NativeLongByReference();
        NativeLong            status      = lib.viWrite(instruments.get(instrument), pBuffer, new NativeLong(writeLength), returnCount);

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Could not write to instrument!");
        }

        if (returnCount.getValue().longValue() != writeLength) {
            throw new VISAException("Command was not fully sent!");
        }

    }

    public static String read(long instrument) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        ByteBuffer            response    = ByteBuffer.allocate(1024);
        NativeLongByReference returnCount = new NativeLongByReference();
        NativeLong status = lib.viRead(
                instruments.get(instrument), response, new NativeLong(1024), returnCount
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error reading from instrument!");
        }

        try {
            return new String(response.array(), 0, returnCount.getValue().intValue(), responseEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new VISAException("Could not encode returned string!");
        }

    }

    public static void closeInstrument(long instrument) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        NativeLong status = lib.viClose(instruments.get(instrument));

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error closing instrument!");
        }

        instruments.remove(instrument);

    }

    public static void setAttribute(long instrument, long attribute, long value) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        NativeLong status = lib.viSetAttribute(
                instruments.get(instrument),
                new NativeLong(attribute),
                new NativeLong(value)
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error setting EOI flag!");
        }

    }

    public static void setEOI(long instrument, boolean set) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_SEND_END_EN, set ? VI_TRUE : VI_FALSE);
    }

    public static void setTimeout(long instrument, long timeoutMSec) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TMO_VALUE, timeoutMSec);
    }

    public static void enableTerminationCharacter(long instrument, boolean set) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TERMCHAR_EN, set ? VI_TRUE : VI_FALSE);
    }

    public static void setTerminationCharacter(long instrument, long eos) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TERMCHAR, eos);
    }

}