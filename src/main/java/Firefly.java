import java.util.HashMap;
import java.util.Random;

/**
 * This class models a single Firefly, while also containing static fields for all Fireflies.
 */
public class Firefly extends Thread {
    private static final int SLEEP_TIME = 10; // amount of time thread is sleeping
    private static final double frequency = 0.1; // frequency of flashing
    private static final double threshold = 0.99; // threshold value for flashing
    private static final double couplingConst = 1; // coupling constant from Kuramoto model
    private static final int N = 4; // amount of neighbors

    private double phase; // phase of the firefly in [0, 2Pi)
    private final HashMap<Firefly, Double> neighborFlashTimes; // Map storing the last broadcasted time a neighbor flashed
    private boolean doAdjustPhase; // flag to control when the phase should be adjusted

    public Firefly() {
        this.neighborFlashTimes = new HashMap<>();
    }

    /**
     * Initialises the rest of the Firefly objects fields.
     * <p>
     * The actual construction of a Firefly object has to be delayed because a Firefly has to be referenced as neighbor
     * before it is constructed.
     *
     * @param neighbors array containing neighboring Firefly objects
     */
    public void init(Firefly[] neighbors) {
        // random phase
        phase = new Random().nextDouble() * 2 * Math.PI;

        // initialise the neighbor map
        // use own flashing time as default value
        double t = (Math.asin(threshold) + phase) / (frequency * 2 * Math.PI);
        for (Firefly neighbor : neighbors) {
            neighborFlashTimes.put(neighbor, t);
        }

        // set adjust flag to false, there is nothing to adjust, yet
        doAdjustPhase = false;
    }

    /**
     * @return activity value in [-1, 1]
     */
    private double getActivity() {
        double t = System.nanoTime() * 1E-9;
        return Math.sin(t * frequency * 2 * Math.PI - phase);
    }

    /**
     * @return true, iff activity is above threshold
     */
    boolean isFlashing() {
        return getActivity() > threshold;
    }

    /**
     * Makes every neighbor receive a flash.
     * The receiveFlash(Firefly) method is called for every neighbor.
     */
    private void broadcastFlash() {
        for (Firefly neighbor : neighborFlashTimes.keySet()) {
            neighbor.receiveFlash(this);
        }
    }

    /**
     * Handles a flash broadcast.
     *
     * @param neighbor the neighbor broadcasting a flash
     */
    private synchronized void receiveFlash(Firefly neighbor) {
        // get current time
        double t = System.nanoTime() * 1E-9;

        // save current time as flash time of given neighbor
        neighborFlashTimes.put(neighbor, t);

        // next time -> adjust phase
        doAdjustPhase = true;
    }

    /**
     * Adjusts the phase according to the given flash times in the neighborFlashTimes map and the Kuramoto model.
     */
    private void adjustPhase() {

        // copy the flash times to avoid race conditions
        double[] flashTimes = new double[N];
        synchronized (neighborFlashTimes) {
            int i = 0;
            for (Firefly neighbor : neighborFlashTimes.keySet()) {
                flashTimes[i++] = neighborFlashTimes.get(neighbor);
            }

            // the flag is set to false at the beginning of the method for the case that, while this method is being
            // executed another broadcast happened, requiring another adjustment
            doAdjustPhase = false;
        }

        // Kuramoto...
        double phaseDelta = 0;
        for (double t : flashTimes) {
            phaseDelta += Math.sin(t * frequency * 2 * Math.PI - Math.asin(threshold) - phase);
        }
        phaseDelta *= couplingConst / N;
        phase += phaseDelta;
    }

    @Override
    public void run() {
        boolean flashed = false;
        boolean isFlashing;
        while (true) {
            // flash only for the first time in a period, when the activity is above the threshold
            isFlashing = isFlashing();
            if (isFlashing && !flashed) {
                broadcastFlash();
            }
            flashed = isFlashing;

            // adjust own phase if necessary
            if (doAdjustPhase) {
                adjustPhase();
            }

            // zzZ...
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
