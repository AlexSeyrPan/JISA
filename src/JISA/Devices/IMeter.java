package JISA.Devices;

import JISA.Enums.AMode;

import java.io.IOException;

public interface IMeter extends Instrument {
    
    /**
     * Takes a current measurement and returns the value.
     *
     * @return Current measurement value, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrent() throws IOException, DeviceException;

    /**
     * Sets the integration time for each measurement.
     *
     * @param time Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Returns the integration time used for each measurement.
     *
     * @return Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the range used for current measurements. A value of n indicates the range -n to +n. If only discrete options
     * are available, the smallest range that contains the given value is chosen.
     *
     * @param range The range to use, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrentRange(double range) throws IOException, DeviceException;

    /**
     * Returns the range used for current measurements. A value of n indicates the range -n to +n.
     *
     * @return The range being used, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrentRange() throws IOException, DeviceException;

    /**
     * Instruments the ammeter to automatically select which range to use for current measurements.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoCurrentRange() throws IOException, DeviceException;

    /**
     * Returns whether the ammeter is currently using auto-ranging for current measurements.
     *
     * @return Is auto-ranging being used?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isCurrentRangeAuto() throws IOException, DeviceException;

    /**
     * Sets the type of averaging to use for each measurement.
     *
     * @param mode Averaging mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageMode(AMode mode) throws IOException, DeviceException;

    /**
     * Sets the number of measurements to use for each average. Only applies if the averaging mode is not "NONE".
     *
     * @param count Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageCount(int count) throws IOException, DeviceException;

    /**
     * Returns the type of averaging being used for each measurement.
     *
     * @return Averaging type
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    AMode getAverageMode() throws IOException, DeviceException;

    /**
     * Returns the number of measurements being used for each average. Only applies if the averaging mode is not "NONE".
     *
     * @return Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    int getAverageCount() throws IOException, DeviceException;

    /**
     * Turns the ammeter measurement channel on.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Turns the ammeter measurement channel off.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOff() throws IOException, DeviceException;

    /**
     * Returns whether the ammeter measurement channel is on or off.
     *
     * @return Is it on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isOn() throws IOException, DeviceException;

}
