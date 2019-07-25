package jisa.devices;

import java.io.IOException;

/**
 * Extension of the LockIn class for lock-in amplifiers with dual-phase capabilities
 */
public interface DPLockIn extends LockIn {

    /**
     * Returns the amplitude of the component of the signal in-phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedX() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the component of the signal 90 degrees out of phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedY() throws IOException, DeviceException;

    /**
     * Returns of the phase of the locked-on signal (relative to the reference)
     *
     * @return Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedPhase() throws IOException, DeviceException;

}