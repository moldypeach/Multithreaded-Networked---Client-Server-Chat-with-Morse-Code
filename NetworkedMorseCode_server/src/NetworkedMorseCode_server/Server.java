package NetworkedMorseCode_server;

// Fig. 27.5: Server.java
// Server portion of a client/server stream-socket connection. 
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Server extends JFrame 
{
   private JTextArea displayArea; // display information to user
   private ServerSocket server; // server socket  
   private ExecutorService runChat; // will run chat
   private List<chatClient> clients;
   private int removeClientNum;

   // set chat server and GUI that displays server exchanges
   public Server()
   {
      super( "Aurora Server" );

       // create ExecutorService with a thread for each player
      runChat = Executors.newCachedThreadPool();
      clients = new ArrayList<>();
      removeClientNum = -1;
      
      try // set up server to receive connections; process connections
      {
         server = new ServerSocket( 12345, 100 ); // create ServerSocket
      } // end try
      catch ( IOException ioException ) 
      {
         ioException.printStackTrace();
      } // end catch      
      
      displayArea = new JTextArea(); // create displayArea
      displayArea.setEditable(false);
      displayArea.setBackground(Color.BLACK);
      displayArea.setForeground(Color.GREEN);
      add( new JScrollPane( displayArea ), BorderLayout.CENTER );

      setSize( 600, 300 ); // set size of window
      setVisible( true ); // show window
      displayMessage("Morse Code server started");
   } // end Server constructor

   // set up and run server 
   public void execute()
   {
       
       while ( clients.size() < 100 ) 
       {
          try 
          {
             clients.add( new chatClient( server.accept(), clients.size()) );
             displayMessage( "Connection " + clients.size() + " received from: "
                             + clients.get(clients.size() -1 ).connection.getInetAddress().getHostName() );             
             runChat.execute(clients.get(clients.size() -1 )); // execute chatClient runnable
          } // end try
          catch ( IOException ioException ) 
          {
             ioException.printStackTrace();
          } // end catch
       } // end while       
   } // end method execute

   // wait for connection to arrive, then display connection info
   private void displayMessage( final String messageToDisplay)
   {
       SwingUtilities.invokeLater(
               new Runnable()
               {
                   public void run() // updates display Area
                   {
                       displayArea.append( messageToDisplay + "\n"); // append message
                   } // end run method
               } // end anonymous inner class
       ); // end call to SwingUtilities.invokeLate
   } // end displayMessage method
   
      // send message to client all connected clients
   private void sendData( String message )
   {
      try // send object to client
      {
          if(removeClientNum >= 0)
          {
              removeClient();
          }

          for (chatClient client : clients)
          {
              client.output.writeObject( message );
              client.output.flush(); // flush output to client
          }            
      } // end try
      catch ( IOException ioException ) 
      {
         displayMessage( "Error writing object" );
      } // end catch
   } // end method sendData
   
    private void removeClient()
    {
          displayMessage("Removing client " + removeClientNum);
          clients.remove(removeClientNum);
          // if there is more than one client, and it isn't the last one in list
          if ( (clients.size() > 0) && (removeClientNum < clients.size() -1) )
          {   
              // reorder clientNumbers
              for (chatClient client : clients)
              {
                  if( client.clientNumber != 0)
                    client.clientNumber -= 1;
              }
          }
          removeClientNum = -1;
          displayMessage("Clients renumbered");
    } // end removeClient() method
   
   // private inner class Player manages each Player as a runnable
   private class chatClient implements Runnable 
   {
       private ObjectOutputStream output; // output stream to client 
       private ObjectInputStream input; // input stream from client       
       private Socket connection; // connection to client
       private int clientNumber; // track client number
       private String clientName;
       private Boolean openConnection;

      // set up chatClient thread
      public chatClient( Socket socket, int number )
      {
         connection = socket; // store socket for client
         // get input & output streams
         try
         {
             getStreams();
         }
         catch( IOException ioException )
         {
             ioException.printStackTrace();
         }
         clientNumber = number;
         openConnection = true;
      } // end Player constructor

   // get streams to send and receive data
   private void getStreams() throws IOException
   {
      // set up output stream for object
      output = new ObjectOutputStream( connection.getOutputStream() );
      output.flush(); // flush output buffer to send header information

      // set up input stream for object
      input = new ObjectInputStream( connection.getInputStream() );
      try
      {
          clientName = (String) input.readObject();
      }
      catch ( ClassNotFoundException classNotFoundException )
      {}
      displayMessage("Got I/O streams for client " + clientName + "(" + clientNumber + ")");
   } // end method getStreams
   
   // process connection with client
   private void processConnection() throws IOException
   {
      String message = "Client connection " + clientNumber + " successful";
//      sendData( clients ); // send connection successful message

      while( openConnection ) // process messages sent from client
      { 
         try // read message and display it
         {
            message = ( String ) input.readObject(); // read new message
            if( message.equalsIgnoreCase("\\quit") )
                openConnection = false;
            else
                sendData( clientName + "\n" + message );
         } // end try
         catch ( ClassNotFoundException classNotFoundException ) 
         {
            sendData( "Unknown object type received" );
         } // end catch

      }
   } // end method processConnection

   // close streams and socket
   private void closeConnection() 
   {
       displayMessage("\nTerminating connection for client " + clientNumber + "\n");
//       sendData( "\nTerminating connection for client " + clientNumber + "\n" );
      
      try 
      {
//          clients.get(clientNumber).output.writeObject( "false" );
//          clients.get(clientNumber).output.flush();
          output.close(); // close output stream
          input.close(); // close input stream
          connection.close(); // close socket
      } // end try
      catch ( IOException ioException ) 
      {
         ioException.printStackTrace();
      } // end catch
   } // end method closeConnection   
   
      // control thread's execution
      public void run()
      {
          
         while ( openConnection ) 
         {
            try 
            {
               processConnection(); // process connection
            } // end try
            catch ( EOFException eofException ) 
            {
               sendData( "\nServer terminated connection for client " + clientNumber + "\n" );
            } // end catch
            catch ( IOException ioException ) 
            {
               ioException.printStackTrace();
            } // end catch            
            finally 
            {
               closeConnection(); //  close connection
               removeClientNum = clientNumber;
               Thread.currentThread().interrupt();
            } // end finally
         } // end while
         
      } // end method run
   } // end class Player   
   
   
} // end class Server