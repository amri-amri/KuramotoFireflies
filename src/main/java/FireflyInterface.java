import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FireflyInterface extends Remote {
    void registerNeighbor(FireflyInterface neighbor) throws RemoteException;
    void broadcastFlash() throws RemoteException;
    void receiveFlash(FireflyInterface neighbor) throws RemoteException;
}
