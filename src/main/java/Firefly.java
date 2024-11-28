import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;

/**
 * This class models a single Firefly, while also containing static fields for all Fireflies.
 */
public class Firefly extends JFrame implements FireflyInterface {
    private static final int SLEEP_TIME = 10; // amount of time thread is sleeping
    private static final double frequency = 0.05; // frequency of flashing
    private static final double threshold = 0.8; // threshold value for flashing
    private static final double couplingConst = 1; // coupling constant from Kuramoto model

    private double phase; // phase of the firefly in [0, 2Pi)
    private final HashMap<FireflyInterface, Double> neighborFlashTimes; // Map storing the last broadcasted time a neighbor flashed
    private boolean doAdjustPhase; // flag to control when the phase should be adjusted

    public Firefly(String name, int registryPort, int s, int x, int y) {
        // random phase
        phase = new Random().nextDouble() * 2 * Math.PI;

        // initialise the neighbor map
        this.neighborFlashTimes = new HashMap<>();

        // set adjust flag to false, there is nothing to adjust, yet
        doAdjustPhase = false;

        // view
        setTitle(String.format("%s at port %d", name, registryPort));
        setSize(s, s);
        setLocation(x,y);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Registers a neighbor to be broadcasted to.
     *
     * @param neighbor the neighbor to receive the flash via broadcast
     * @throws RemoteException
     */
    @Override
    public void registerNeighbor(FireflyInterface neighbor) throws RemoteException {
        // use own flashing time as default value
        double t = (Math.asin(threshold) + phase) / (frequency * 2 * Math.PI);
        neighborFlashTimes.put(neighbor, t);
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
    public void broadcastFlash() throws RemoteException {
        for (FireflyInterface neighbor : neighborFlashTimes.keySet()) {
            neighbor.receiveFlash(this);
        }
    }

    /**
     * Handles a flash broadcast.
     *
     * @param neighbor the neighbor broadcasting a flash
     */
    @Override
    public synchronized void receiveFlash(FireflyInterface neighbor) {
        // get current time
        double t = System.nanoTime() * 1E-9;

        // save current time as flash time of given neighbor
        neighborFlashTimes.put( neighbor, t);

        // next time -> adjust phase
        doAdjustPhase = true;
    }

    /**
     * Adjusts the phase according to the given flash times in the neighborFlashTimes map and the Kuramoto model.
     */
    private void adjustPhase() {

        // copy the flash times to avoid race conditions
        int N = neighborFlashTimes.size();
        double[] flashTimes = new double[N];
        synchronized (neighborFlashTimes) {
            int i = 0;
            for (FireflyInterface neighbor : neighborFlashTimes.keySet()) {
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

    public static void main(String[] args) throws InterruptedException {
        // name registryPort s x y otherPorts...
        final String NAME = args[0];
        final int s = Integer.parseInt(args[1]);
        final int x = Integer.parseInt(args[2]);
        final int y = Integer.parseInt(args[3]);
        final int REGISTRY_PORT = Integer.parseInt(args[4]);

        Firefly firefly = new Firefly(NAME, REGISTRY_PORT, s, x, y);
        // Starte Server
        // Create and export server instance
        FireflyInterface stub;
        try {
            stub = (FireflyInterface) UnicastRemoteObject.exportObject(firefly, 0);
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            registry.rebind("BroadcastService", stub);
            String str = String.format("Server of %s started with registry port %d", NAME, REGISTRY_PORT);
            System.out.println(str);
        } catch (RemoteException e) {
            System.err.println(e);
            return;
        }

        // Verbinde mit anderen Servern
        for (int i = 5; i < args.length; i++) {
            int port = Integer.parseInt(args[i]);
            while (true)
                try {
                    Registry ngbRegistry = LocateRegistry.getRegistry("localhost", port);
                    FireflyInterface ngbServer = (FireflyInterface) ngbRegistry.lookup("BroadcastService");
                    ngbServer.registerNeighbor((FireflyInterface) stub);
                    break;
                } catch (NotBoundException | RemoteException ignored) {
                    //throw new RuntimeException(e);
                    Thread.sleep(1000);
                }
        }
        String str = String.format("%s connected to all neighbors", NAME);
        System.out.println(str);


        // Start Thread
        new Thread(() -> firefly.run()).start();
        str = String.format("%s started running", NAME);
        System.out.println(str);
    }

    public void run() {
        boolean flashed = false;
        boolean isFlashing;
        while (true) {
            // flash only for the first time in a period, when the activity is above the threshold
            isFlashing = isFlashing();
            if (isFlashing && !flashed) {
                try {
                    broadcastFlash();
                } catch (RemoteException e) {
                    System.err.println("Couldn't broadcast flash!");
                }
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

    @Override
    public void paint(Graphics g) {


        g.setColor(isFlashing() ? Color.YELLOW : Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());


        try {
            Thread.sleep(10); // Update-Rate
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // refresh the visualisation
        repaint();
    }
}
