import java.util.Random;

public class Firefly extends Thread {

    final static int CONTROL_RATE = 200;

    Firefly(double frequency, double couplingConstant, World world, int x, int y) {
        this.frequency = frequency;
        this.couplingConstant = couplingConstant;
        phaseShift = new Random().nextDouble() * 2 * Math.PI;
        this.world = world;
        this.x = x;
        this.y = y;
    }

    double frequency;
    double couplingConstant;
    double phaseShift;
    World world;
    int x, y;
    //private final Object lock = new Object();

    private double getPhase() {
        double time = System.currentTimeMillis() / 1E3;
        return Math.sin(frequency * time * 2 * Math.PI + phaseShift);
    }

    private double[] getNeighborPhases() {
        Firefly[][] world = this.world.getGrid();
        double[] neighborPhases = new double[4];

        int x_ = x - 1;
        if (x_ < 0) x_ = world[0].length - 1;
        neighborPhases[0] = world[y][x_].getPhase();

        x_ = x + 1;
        if (x_ > world[0].length - 1) x_ = 0;
        neighborPhases[1] = world[y][x_].getPhase();

        int y_ = y - 1;
        if (y_ < 0) y_ = world[0].length - 1;
        neighborPhases[2] = world[y_][x].getPhase();

        y_ = y + 1;
        if (y_ > world[0].length - 1) y_ = 0;
        neighborPhases[3] = world[y][x].getPhase();

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
        return getPhase() >= 0;
    }

    public void run() {
        while (true) {
            try {
                adjustPhaseShift();
                Thread.sleep(CONTROL_RATE); // Kontrollrate
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
