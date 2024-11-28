import java.util.Random;

/**
 * This class models the flashing behavior of a firefly.
 */
public class Firefly extends Thread {

    /**
     * Creates a firefly.
     * <p>
     * The fireflies phase shift is a random value between 0 (inclusive) and 2PI (exclusive).
     * @param frequency flashing frequency of the fireflies
     * @param couplingConstant the coupling constant that determines the strength of synchronisation
     * @param adjustmentFrequency the frequency of phase shift adjustments per second
     * @param world the world the firefly lives in
     * @param x the fireflies x-coordinate in the world
     * @param y the fireflies y-coordinate in the world
     */
    Firefly(double frequency, double couplingConstant, double adjustmentFrequency, World world, int x, int y) {
        this.frequency = frequency;
        this.couplingConstant = couplingConstant;
        this.adjustmentFrequency = adjustmentFrequency;
        this.world = world;
        this.x = x;
        this.y = y;
        
        // each firefly has a random phase shift from the beginning
        phaseShift = new Random().nextDouble() * 2 * Math.PI;
    }

    double phaseShift; // the fireflies phase shift in [0, 2PI)
    final double frequency; // amount of times a second that the firefly is active
    final double couplingConstant; // factor used to determine how string the coupling is
    final double adjustmentFrequency; // the amount of times per second a firefly updates its phase shift
    final World world; // The world containing all fireflies
    final int x, y; // the fireflies own coordinates in the world

    /**
     * Computes and returns activity value between -1 and 1
     * <p>
     * The activity of a firefly is calculated as the sine function of its frequency and time, as well as its phase shift.
     * An activity of at least 0 means, that the firefly is active.
     *
     * @return activity value between -1 and 1
     */
    private double getActivity() {
        // get current time
        double time = System.currentTimeMillis() / 1E3;
        // compute and return value
        return Math.sin(frequency * time * 2 * Math.PI + phaseShift);
    }

    /**
     * Retrieves the phase shifts of the fireflies neighbors in the world.
     *
     * @return array containing phase shifts
     */
    private double[] getNeighborPhases() {
        Firefly[][] world = this.world.getGrid();
        double[] neighborPhases = new double[4];

        // left
        int x_ = x - 1;
        if (x_ < 0) x_ = world[0].length - 1;
        neighborPhases[0] = world[y][x_].getActivity();

        // right
        x_ = x + 1;
        if (x_ > world[0].length - 1) x_ = 0;
        neighborPhases[1] = world[y][x_].getActivity();

        // above
        int y_ = y - 1;
        if (y_ < 0) y_ = world[0].length - 1;
        neighborPhases[2] = world[y_][x].getActivity();

        // below
        y_ = y + 1;
        if (y_ > world[0].length - 1) y_ = 0;
        neighborPhases[3] = world[y_][x].getActivity();

        return neighborPhases;
    }

    /**
     * Adjusts phase shift according to the Kuramoto model.
     */
    private void adjustPhaseShift() {
        // only the neighbors are considered
        double[] otherPhaseShifts = getNeighborPhases();
        int N = otherPhaseShifts.length;

        // compute change in phase
        double shift = 0;
        for (double otherShift : otherPhaseShifts) {
            shift += Math.sin(otherShift - phaseShift);
        }
        shift *= couplingConstant / N;

        // apply change
        phaseShift += shift;
        phaseShift %= 2 * Math.PI;
    }

    /**
     * Returns flashing state of firefly according to activity value.
     *
     * @return true, if activity is at least 0, false otherwise
     */
    public boolean isFlashing() {
        return getActivity() >= 0;
    }


    public void run() {
        long lastTimeOfAdjustment = System.currentTimeMillis();
        while (true) {
            try {

                Thread.sleep(50);
                long now = System.currentTimeMillis();

                // If not enough time has passed, go back to beginning of while-loop
                if ((now - lastTimeOfAdjustment) * 1E-3 * adjustmentFrequency < 1) continue;

                // adjust phase shift and memorize last time of adjustment
                adjustPhaseShift();
                lastTimeOfAdjustment = now;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
