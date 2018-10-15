package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Static class for accessing the native VISA library in a more Java-friendly way
 */
public class VISA {

    private static ArrayList<Driver>     drivers      = new ArrayList<>();
    private static HashMap<Long, Driver> instrDrivers = new HashMap<>();
    private static HashMap<Long, Long>   instrIDs     = new HashMap<>();
    private static long                  counter      = 0;

    static {

        try {
            VISADriver.init();
            drivers.add(new VISADriver());
        } catch (VISAException ignored) { }

        try {
            GPIBDriver.init();
            drivers.add(new GPIBDriver());
        } catch (VISAException ignored) { }

        if (drivers.size() == 0) {
            System.err.println("Could not load any drivers!");
            System.exit(1);
        }

    }

    /**
     * Returns an array of all instrument addressed detected by VISA
     *
     * @return Array of instrument addresses
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static StrAddress[] getInstruments() throws VISAException {

        HashMap<String, StrAddress> addresses = new HashMap<>();

        for (Driver driver : drivers) {
            for (StrAddress a : driver.search()) {
                boolean found = false;
                for (String s : addresses.keySet()) {
                    if (s.trim().equals(a.getVISAAddress().trim())) {
                        found = true;
                    }
                }
                if (!found) {
                    addresses.put(a.getVISAAddress(), a);
                }
            }
        }

        return addresses.values().toArray(new StrAddress[0]);

    }

    /**
     * Open the instrument with the given VISA resource address
     *
     * @param address Resource address
     *
     * @return Instrument handle
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static long openInstrument(InstrumentAddress address) throws VISAException {

        long id    = -1;
        long index = -1;
        for (Driver d : drivers) {

            try {
                id = d.open(address);
                index = counter++;
                instrDrivers.put(index, d);
                instrIDs.put(index, id);
                break;
            } catch (VISAException e) {
                id = -1;
            }

        }

        if (id == -1) {
            throw new VISAException("Could not open %s using any driver!", address.getVISAAddress());
        }

        return index;

    }

    /**
     * Writes to the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     * @param toWrite    String the write to the instrument
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void write(long instrument, String toWrite) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).write(
                instrIDs.get(instrument),
                toWrite
        );

    }

    /**
     * Read from the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     * @param bufferSize Number of bytes to allocate for response
     *
     * @return The read string from the device
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static String read(long instrument, int bufferSize) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        return instrDrivers.get(instrument).read(
                instrIDs.get(instrument),
                bufferSize
        );

    }

    /**
     * Read from the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     *
     * @return The read string from the device
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static String read(long instrument) throws VISAException {
        return read(instrument, 1024);
    }

    /**
     * Closes the connection to the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void closeInstrument(long instrument) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).close(instrIDs.get(instrument));

    }

    /**
     * Sets whether to send EOI at the end of talking (mostly for GPIB)
     *
     * @param instrument Instrument handle from openInstrument()
     * @param set        Should it send?
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void setEOI(long instrument, boolean set) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setEOI(instrIDs.get(instrument), set);
    }

    /**
     * Sets the timeout for read/write to/from instrument
     *
     * @param instrument  Instrument handle from openInstrument()
     * @param timeoutMSec Timeout in milliseconds
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void setTimeout(long instrument, long timeoutMSec) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setTMO(instrIDs.get(instrument), timeoutMSec);
    }

    public static void setTerminationCharacter(long instrument, long eos) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setEOS(instrIDs.get(instrument), eos);
    }

}
