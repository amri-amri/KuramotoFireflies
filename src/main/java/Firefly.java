import java.util.Random;

public class Firefly extends Thread {


    Firefly(double frequency, double couplingConstant, double adjustmentFrequency, World world, int x, int y) {
        this.frequency = frequency;
        this.couplingConstant = couplingConstant;
        phaseShift = new Random().nextDouble() * 2 * Math.PI;
        this.world = world;
        this.x = x;
        this.y = y;
        this.adjustmentFrequency = adjustmentFrequency;
    }

    double frequency;
    double couplingConstant;
    double phaseShift;
    World world;
    int x, y;
    double adjustmentFrequency;

    /*
    between -1 and 1
     */
    private double getActivity() {
        double time = System.currentTimeMillis() / 1E3;
        return Math.sin(frequency * time * 2 * Math.PI + phaseShift);
    }

    private double[] getNeighborPhases() {
        Firefly[][] world = this.world.getGrid();
        double[] neighborPhases = new double[4];

        int x_ = x - 1;
        if (x_ < 0) x_ = world[0].length - 1;
        neighborPhases[0] = world[y][x_].getActivity();

        x_ = x + 1;
        if (x_ > world[0].length - 1) x_ = 0;
        neighborPhases[1] = world[y][x_].getActivity();

        int y_ = y - 1;
        if (y_ < 0) y_ = world[0].length - 1;
        neighborPhases[2] = world[y_][x].getActivity();

        y_ = y + 1;
        if (y_ > world[0].length - 1) y_ = 0;
        neighborPhases[3] = world[y_][x].getActivity();

        return neighborPhases;
    }

    private void adjustPhaseShift() {
        double[] otherPhaseShifts = getNeighborPhases();
        int N = otherPhaseShifts.length;
        double shift = 0;
        for (double otherShift : otherPhaseShifts) {
            shift += Math.sin(otherShift - phaseShift);
        }
        shift *= couplingConstant / N;
        phaseShift += shift;
        phaseShift %= 2 * Math.PI;
    }

    public boolean isFlashing() {
        return getActivity() >= 0;
    }

    public void run() {
        long lastTimeOfAdjustment = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(50);
                long now = System.currentTimeMillis();
                if ((now - lastTimeOfAdjustment) * 1E-3 * adjustmentFrequency < 1) continue;
                adjustPhaseShift();
                lastTimeOfAdjustment = now;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
