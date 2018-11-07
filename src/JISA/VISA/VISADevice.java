package JISA.VISA;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;

/**
 * Generic instrument encapsulation via VISA
 */
public class VISADevice {

    private Connection        connection;
    private InstrumentAddress address;
    private String            terminator     = "";
    private String            lastCommand    = null;
    private String            lastRead       = null;
    private int               readBufferSize = 1024;
    private int               retryCount     = 3;
    private long              timeout        = 2000;

    public final static int    DEFAULT_TIMEOUT = 13;
    public final static int    DEFAULT_EOI     = 1;
    public final static int    DEFAULT_EOS     = 0;
    public final static int    EOS_RETURN      = 5130;
    public final static int    LF_TERMINATOR   = 0x0A;
    public final static int    CR_TERMINATOR   = 0x0D;
    public final static int    CRLF_TERMINATOR = 0x0D0A;
    public final static String C_IDN           = "*IDN?";

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public VISADevice(InstrumentAddress address) throws IOException {

        if (address == null) {
            return;
        }

        try {
            this.connection = VISA.openInstrument(address);
            this.address = address;
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }


    public void clearRead() throws IOException {

        try {
            connection.setTMO(50);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

        while (true) {
            try {
                read();
            } catch (IOException e) {
                break;
            }
        }

        try {
            connection.setTMO(timeout);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * What default number of bytes should we expect to get when reading from the device?
     *
     * @param bytes The number of bytes, yikes.
     */
    public void setReadBufferSize(int bytes) {
        readBufferSize = bytes;
    }

    public void setSerialParameters(int baudRate, int dataBits, Connection.Parity parity, Connection.StopBits stopBits, Connection.Flow flowControl) throws IOException {

        try {
            connection.setSerial(baudRate, dataBits, parity, stopBits, flowControl);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Should we send an EOI signal at the end of writing to the device? Generally, this should be true and is by default
     * however older devices from more anarchic times (such as the 70s) may needs this disabling.
     *
     * @param flag Do we, don't we?
     *
     * @throws IOException Upon communications error
     */
    public void setEOI(boolean flag) throws IOException {

        try {
            connection.setEOI(flag);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Sets which character we should expect to read from the device to indicate that it's done talking to us.
     *
     * @param character The character code
     *
     * @throws IOException Upon communications error
     */
    public void setReadTerminationCharacter(long character) throws IOException {

        try {
            connection.setEOS(character);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Sets the timeout, in milliseconds, for operations with the device
     *
     * @param timeoutMSec Timeout, milliseconds
     *
     * @throws IOException Upon communications error
     */
    public void setTimeout(long timeoutMSec) throws IOException {
        try {
            connection.setTMO(timeoutMSec);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
        timeout = timeoutMSec;
    }

    public void setRetryCount(int count) {
        retryCount = count;
    }

    /**
     * Returns the address used to connect to the device
     *
     * @return Address object
     */
    public InstrumentAddress getAddress() {
        return address;
    }

    /**
     * Set a termination character to tell the device when we've stopped talking to it
     *
     * @param term The character to use (eg "\n" or "\r")
     */
    public void setTerminator(String term) {
        terminator = term;
    }

    /**
     * Write the given string to the device
     *
     * @param command The string to write
     * @param args    Any formatting arguments
     *
     * @throws IOException Upon communications error
     */
    public synchronized void write(String command, Object... args) throws IOException {
        String commandParsed = String.format(command, args).concat(terminator);
        lastCommand = commandParsed;
        try {
            connection.write(commandParsed);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Read a string from the device
     *
     * @return The string returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String read() throws IOException {
        return read(retryCount);
    }

    /**
     * Read a string from the device
     *
     * @return The string returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String read(int attempts) throws IOException {

        int count = 0;

        // Try n times
        while (true) {
            try {
                lastRead = connection.read(readBufferSize);
                break;
            } catch (VISAException e) {
                count++;
                if (count >= attempts) {
                    throw new IOException(e.getMessage());
                }
            }
        }
        return lastRead;
    }

    /**
     * Read a double from the device
     *
     * @return The number returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized double readDouble() throws IOException {
        return Double.parseDouble(read());
    }

    /**
     * Read an integer from the device
     *
     * @return Integer read from the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized int readInt() throws IOException {
        return Integer.parseInt(read());
    }

    /**
     * Write the given string, then immediately read the response as a double
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return Numerical response
     *
     * @throws IOException Upon communications error
     */
    public synchronized double queryDouble(String command, Object... args) throws IOException {
        write(command, args);
        return readDouble();
    }

    /**
     * Write the given string, then immediately read the response as an integer
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return Numerical response
     *
     * @throws IOException Upon communications error
     */
    public synchronized int queryInt(String command, Object... args) throws IOException {
        write(command, args);
        return readInt();
    }

    /**
     * Write the given string, then immediately read the response
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return String response
     *
     * @throws IOException Upon communications error
     */
    public synchronized String query(String command, Object... args) throws IOException {
        write(command, args);
        return read();
    }

    /**
     * Sends the standard identifications query to the device (*IDN?)
     *
     * @return The resposne of the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String getIDN() throws IOException {
        return query(C_IDN);
    }

    /**
     * Close the connection to the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized void close() throws IOException {
        try {
            connection.close();
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
    }

}
