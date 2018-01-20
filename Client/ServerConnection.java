import java.net.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 *
 * @author brom
 */
public class ServerConnection {

	// Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0.3;

	private DatagramSocket m_socket = null;
	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;

	public ServerConnection(String hostName, int port) {
		m_serverPort = port;

		try {
			m_serverAddress = InetAddress.getByName(hostName);	
			m_socket = new DatagramSocket();
		} catch (Exception e) {
			System.out.println("Exception:"  + e);
		}
	}

	public boolean handshake(String name) {
		String msg = "New " + name;
		DatagramPacket packet = marshallMessage(msg);

		try {
			m_socket.send(packet);	
		} catch (Exception e){
			System.out.println("IOException: " + e);
		}

		String response = receiveChatMessage();

		if (response.equals("Success")) {
			return true;
		} else if(response.equals("UsernameTaken")) {
			System.out.println("Username Taken.");
			return false;	
		} 

		return false;
	}

	public String receiveChatMessage() {
		byte[] buffer = new byte[1048];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		do {
			try {
				m_socket.receive(packet);	
			} catch (Exception e) {
				System.out.println("Error while user getting reponse from server.");
			}
			String response = unmarshallMessage(packet);
			System.out.println(response);
			return response;
		} while(true);
	}

	public void sendChatMessage(String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();

		if (failure > TRANSMISSION_FAILURE_RATE) {
			DatagramPacket packet = marshallMessage(message);

			try {
				m_socket.send(packet);
			} catch (Exception e){
				System.out.println("IOException: " + e);
			}
		} else {
			// Message got lost resend message
			sendChatMessage(message);
		}
	}

	public DatagramPacket marshallMessage(String message) {
		byte[] buffer = new byte[2048];
		buffer = message.getBytes();

		return new DatagramPacket(buffer, buffer.length, m_serverAddress, m_serverPort);
	}

	public String unmarshallMessage(DatagramPacket packet) {
		return new String(packet.getData(), 0, packet.getLength());
	}

}
