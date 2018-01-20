import java.net.*;
import java.net.InetAddress;
import java.util.Random;

/**
 * 
 * @author brom
 */
public class ClientConnection {
	
	static double TRANSMISSION_FAILURE_RATE = 0.3;
	
	private final String m_name;
	private final InetAddress m_address;
	private final int m_port;

	public ClientConnection(String name, InetAddress address, int port) {
		m_name = name;
		m_address = address;
		m_port = port;
	}

	public void sendMessage(String message, DatagramSocket socket) {
		
		Random generator = new Random();
    	double failure = generator.nextDouble();
    	
    	if (failure > TRANSMISSION_FAILURE_RATE){
    		byte[] buffer = message.getBytes(); 
    		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, m_address, m_port);
    		
    		try {
    			socket.send(packet);
    		} catch (Exception e) {

    		}
    	} else {
    		// Message got lost
    	}
		
	}

	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}
}
