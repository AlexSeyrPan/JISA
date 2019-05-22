package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Enums.AMode;
import JISA.Enums.Source;
import JISA.Enums.TType;
import JISA.Enums.Terminals;

import java.io.IOException;
import java.util.Random;

public class DummyMCSMU implements MCSMU {

    private Random    random  = new Random();
    private Double[]  current = {null, null, null, null};
    private Double[]  voltage = {null, null, null, null};
    private Source[]  mode    = {Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE};
    private boolean[] probes  = {true, true, true, true};
    private double[]  R       = {random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500};

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {
        return voltage[channel] == null ? getCurrent(channel) * (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : voltage[channel];
    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {
        return current[channel] == null ? getVoltage(channel) / (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : current[channel];
    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {
        this.voltage[channel] = voltage;
        this.current[channel] = null;
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        this.current[channel] = current;
        this.voltage[channel] = null;
    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {
        return true;
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {
        mode[channel] = source;
    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        return mode[channel];
    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltage(channel, level);
                break;

            case CURRENT:
                setCurrent(channel, level);
                break;

        }

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                getVoltage(channel);
                break;

            case CURRENT:
                getCurrent(channel);
                break;

        }

        return getVoltage(channel);
    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {
        switch (getSource(channel)) {

            case VOLTAGE:
                getCurrent(channel);
                break;

            case CURRENT:
                getVoltage(channel);
                break;

        }

        return getVoltage(channel);
    }

    @Override
    public int getNumChannels() {
        return 4;
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {
        probes[channel] = fourProbes;
    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {
        return probes[channel];
    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {

    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {

    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setSourceRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getSourceRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingSource(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        return 100e-6;
    }

    @Override
    public TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException {
        return TType.TRIAX;
    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {

    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        return Terminals.FRONT;
    }

    @Override
    public void setOffMode(int channel, OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public String getIDN() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }
}
