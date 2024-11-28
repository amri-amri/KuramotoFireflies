import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FireflyClient extends Remote {
    void receiveFlash(FireflyServer neighbor) throws RemoteException;
}
