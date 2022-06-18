import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;

public class Main {
    public static void main(String[] args) {
        EchoClient echoClient = new EchoClient();
        echoClient.startListener();
        /*
        try {
            Socket socket = new Socket("localhost", 2049);
            ///while (true){
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            char[] out = new char[9000];
            reader.read(out);
            System.out.println(out);

            writer.write(out);
            reader.read(out);
            System.out.println(out);

            writer.write("/RS.ChannelList,ssn.FRemoteSessionImageChannelWrite /RS.ChannelList,ssi.FRemoteSessionInputChannelRead /RS.ChannelList,ssi.FRemoteSessionLiveLinkChannelRead");
            out = new char[9000];
            reader.read(out);
            System.out.println(out);

            out = new char[9000];
            reader.read(out);
            System.out.println(out);

            out = new char[9000];
            reader.read(out);
            System.out.println(out);
            /*


            while(true){
                out = new char[9000];
                writer.write(" ");
                reader.read(out);
                System.out.println(out);
            }
            /// }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
