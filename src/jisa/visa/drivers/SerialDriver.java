package jisa.visa.drivers;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.SerialAddress;
import jisa.visa.VISAException;
import jisa.visa.connections.Connection;
import jisa.visa.connections.SerialConnection;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SerialDriver implements Driver {

    public SerialDriver() {

    }

    @Override
    public List<SerialAddress> search() {
        return Arrays.stream(SerialPortList.getPortNames()).map(SerialAddress::new).collect(Collectors.toList());
    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.SERIAL;
    }

    @Override
    public Connection open(Address address) throws VISAException {

        SerialAddress addr = address.toSerialAddress();

        if (addr == null) {
            throw new VISAException("Can only open serial connections with the native serial driver!");
        }

        String   device    = addr.getPort();
        String[] portNames = SerialPortList.getPortNames();
        String   found     = null;

        for (String name : portNames) {

            if (name.trim().equals(device.trim())) {
                found = name;
                break;
            }

        }

        if (found == null) {
            throw new VISAException("No serial port \"%s\" was found.", device.trim());
        }

        SerialPort port = new SerialPort(found);
        boolean    result;

        try {
            result = port.openPort();
        } catch (SerialPortException e) {
            throw new VISAException(e.getMessage());
        }

        if (!result) {
            throw new VISAException("Error opening port \"%s\".", device.trim());
        }

        return new NSConnection(port);

    }

    public static class NSConnection implements SerialConnection {

        private final SerialPort port;
        private       int        tmo;
        private       String     terms;
        private       byte[]     terminationSequence = {0x0A};
        private       Charset    charset             = Charset.defaultCharset();

        public NSConnection(SerialPort comPort) throws VISAException {
            port = comPort;
            setSerialParameters(9600, 8);
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            boolean result;

            try {
                result = port.writeBytes(bytes);
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

            if (!result) {
                throw new VISAException("Error writing to port!");
            }

        }

        @Override
        public void clear() throws VISAException {

            try {
                port.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void setEncoding(Charset charset) {
            this.charset = charset;
        }

        @Override
        public Charset getEncoding() {
            return charset;
        }

        @Override
        public void write(String toWrite) throws VISAException {

            boolean result = false;

            try {
                result = port.writeBytes(toWrite.getBytes(charset));
            } catch (SerialPortException e) {
                e.printStackTrace();
            }

            if (!result) {
                throw new VISAException("Error writing to port!");
            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer buffer    = ByteBuffer.allocate(bufferSize);
            byte[]     lastBytes = new byte[terminationSequence.length];
            byte[]     single;

            try {

                for (int i = 0; i < bufferSize; i++) {

                    single = port.readBytes(1, tmo);

                    if (single.length != 1) {
                        throw new VISAException("Error reading from input stream!");
                    }

                    buffer.put(single[0]);

                    if (terminationSequence.length > 0) {

                        System.arraycopy(lastBytes, 1, lastBytes, 0, lastBytes.length - 1);

                        lastBytes[lastBytes.length - 1] = single[0];

                        if (Arrays.equals(lastBytes, terminationSequence)) {
                            break;
                        }

                    }

                }

                return Util.trimArray(buffer.array());

            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void setReadTerminator(long character) {

            if (character == 0) {
                terminationSequence = new byte[0];
            }

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(character);

            buffer.rewind();

            byte value = 0;

            while (value == 0) {
                value = buffer.get();
            }

            buffer.position(buffer.position() - 1);

            terminationSequence = buffer.slice().array();

        }

        @Override
        public void setReadTerminator(String character) {
            terminationSequence = character.getBytes(charset);
        }

        @Override
        public void setTimeout(int duration) throws VISAException {
            tmo = duration;
        }

        @Override
        public void setSerialParameters(int baud, int data, SerialConnection.Parity parity, SerialConnection.Stop stop, SerialConnection.FlowControl... flows) throws VISAException {

            int stopBits;

            switch (stop) {

                case BITS_20:
                    stopBits = SerialPort.STOPBITS_2;
                    break;

                case BITS_15:
                    stopBits = SerialPort.STOPBITS_1_5;
                    break;

                default:
                case BITS_10:
                    stopBits = SerialPort.STOPBITS_1;
                    break;

            }

            try {

                port.setParams(baud, data, stopBits, parity.toInt());

                int flowControl = SerialPort.FLOWCONTROL_NONE;

                for (FlowControl flow : flows) {

                    switch (flow) {

                        case RTS_CTS:
                            flowControl |= SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT;
                            break;

                        case XON_XOFF:
                            flowControl |= SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
                            break;

                    }

                }

                port.setFlowControlMode(flowControl);

            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void close() throws VISAException {

            boolean result;

            try {
                port.purgePort(1);
                port.purgePort(2);
                result = port.closePort();
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

            if (!result) {
                throw new VISAException("Error closing port!");
            }

        }

    }

}
