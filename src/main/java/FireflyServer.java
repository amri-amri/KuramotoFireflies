import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FireflyServer extends Remote {
    void registerNeighbor(FireflyClient neighbor) throws RemoteException;
    void broadcastFlash() throws RemoteException;
}
