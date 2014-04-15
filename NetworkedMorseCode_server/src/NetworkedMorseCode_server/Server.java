/* Filename:        Server.java
 * Last Modified:   15 April 2014
 * Author:          Todd Parker
 * Email:           todd.i.parker@maine.edu
 * Course:          CIS314 - Advanced Java
 * 
 * Server.java creates a GUI window for logging server messages, creates a
 * server socket and then calls server.accept() to block until a client connect-
 * ion is detected and created. Method execute() is called from main to initiate
 * and execute client connections, and runs until the server application is 
 * either terminated or reaches 100 connected clients. Each connected client
 * is added to a clients List as a Runnable in its own thread. Each client's 
 * constructor initializes its connection and gets the I/O streams, and then
 * method run() calls method processConnection(); which, loops indefinitely
 * until the server receives a "\\quit" message from a client. Upon detection
 * of a quit message, the client calls method removeClient() to delete itself
 * from the clients list, and then the thread is interrupted.
 *
 * NOTE: Some code of this class was modified from the course textbook
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and Pearson Education, 
 * Inc. All Rights Reserved.   
 */
package NetworkedMorseCode_server;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

public class Server extends JFrame 
{
   private JTextArea displayArea; // Display information to user
   private ServerSocket server; // Server socket  
   private ExecutorService runChat; // Runs chat
   private List<chatClient> clients; // List of connected clients
//   private int removeClientNum; // Flag to remove a client from clients

   // Constructor sets up server and GUI
   public Server()
   {
      super( "Aurora Server" );

       // create ExecutorService with a thread for each player
      runChat = Executors.newCachedThreadPool();
      clients = new ArrayList<>();
//      removeClientNum = -1;
      
      try // Set up server to receive connections; process connections
      {
         server = new ServerSocket( 12345, 100 ); // create ServerSocket
      } // end try
      catch ( IOException ioException ) 
      {
         ioException.printStackTrace();
      } // end catch      
      
      displayArea = new JTextArea(); // Create display area
      displayArea.setEditable(false); // Disallow input to display area
      displayArea.setBackground(Color.BLACK);
      displayArea.setForeground(Color.GREEN);
      // Add displayArea to JFrame
      add( new JScrollPane( displayArea ), BorderLayout.CENTER );

      setSize( 600, 300 ); // Set size of server window
      setVisible( true ); // Show window
      displayMessage("Morse Code server started");
   } // end Server constructor

   // Run server 
   public void execute()
   {
       
       while ( clients.size() < 100 ) // While less than 100 connected clients
       {
          try 
          {
              // Add a new client, passing current list size as client number
             clients.add( new chatClient( server.accept(), clients.size()) );
             displayMessage( "Connection " + clients.size() + " received from: "
                             + clients.get(clients.size() -1 ).connection.getInetAddress().getHostName() );             
             // Execute chatClient as runnable (i.e. in its own thread)
             runChat.execute(clients.get(clients.size() -1 ));
          } // End try
          catch ( IOException ioException ) 
          {
             ioException.printStackTrace();
          } // End catch
       } // End while       
   } // End execute() method

   // Display a server message
   private void displayMessage( final String messageToDisplay)
   {
       SwingUtilities.invokeLater(
               new Runnable()
               {
                   public void run() // Updates display Area
                   {
                       // Append message
                       displayArea.append( messageToDisplay + "\n");
                   } // End run method
               } // End anonymous inner class
       ); // End SwingUtilities.invokeLate
   } // End displayMessage() method
   
   // Send message to all connected clients
   private void sendData( String message )
   {
      try // Send object to client
      {
          for (chatClient client : clients) // For each connected client
          {
              client.output.writeObject( message );
              client.output.flush(); // flush output to client
          } // End for each connected client    
      } // end try
      catch ( IOException ioException ) 
      {
         displayMessage( "Error writing object" );
      } // End catch
   } // End sendData() method
   
   // Remove the passed client number from clients list and reorder client numbers
    private void removeClient( int removeClientNum)
    {
          displayMessage("Removing client " + removeClientNum);
          clients.remove(removeClientNum); // Remove client from list
          // If there was more than one client, and the client removed was not
          // the last client in the list
          if ( (clients.size() > 0) && (removeClientNum < clients.size() ) )
          {   
              // Reorder client numbers
              for (chatClient client : clients)
              {
                  if( client.clientNumber != 0) // Ignore client number 0
                    client.clientNumber -= 1;
              } // End for reorder client numbers
          }
          displayMessage("Clients renumbered");
    } // end removeClient() method
   
   // private inner class chatClient manages each chatClient as a runnable
   private class chatClient implements Runnable 
   {
       private ObjectOutputStream output; // Output stream to client 
       private ObjectInputStream input; // Input stream from client       
       private Socket connection; // Connection to client
       private int clientNumber; // Track client number
       private String clientName; // Track client name
       private Boolean openConnection; // State of connection (i.e. open/closed)

      // Constructor sets up chatClient thread
      public chatClient( Socket socket, int number )
      {
         connection = socket; // Store socket for client
         clientNumber = number; // Store client number 
         // Get I/O streams
         try
         {
             getStreams();
         }
         catch( IOException ioException )
         {
             ioException.printStackTrace();
         }
         openConnection = true;
      } // End Player constructor

   // Get I/O streams
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
   } // End getStreams() methods
   
   // Process connection with client
   private void processConnection() throws IOException
   {
      String message = "Client connection " + clientNumber + " successful";

      // While client has an open connection to server, process messages
      while( openConnection )
      { 
         try // Read message and display it
         {
            message = ( String ) input.readObject(); // Read new message
            if( message.equalsIgnoreCase("\\quit") ) // If client is closed
                openConnection = false;
            else // Else send message to all connected clients
                sendData( clientName + "\n" + message );
         } // End try
         catch ( ClassNotFoundException classNotFoundException ) 
         {
            sendData( "Unknown object type received" );
         } // End catch

      } // End while client has an open connection to server
   } // End method processConnection

   // Close I/O streams and socket
   private void closeConnection() 
   {
       displayMessage("\nTerminating connection for client " + clientNumber + "\n");      
      try 
      {
          output.close(); // Close output stream
          input.close(); // Close input stream
          connection.close(); // Close socket
      } // end try
      catch ( IOException ioException ) 
      {
         ioException.printStackTrace();
      } // End catch
   } // End closeConnection() method
   
      // Control thread's execution
      public void run()
      {
            try 
            {
                processConnection(); // Process connection
            } // End try
            catch ( EOFException eofException ) 
            {
                sendData( "\nServer terminated connection for client " + clientNumber + "\n" );
            } // End catch
            catch ( IOException ioException ) 
            {
                ioException.printStackTrace();
            } // End catch            
            finally 
            {
                closeConnection(); //  Close connection
                removeClient(clientNumber); // Remove client from clients
                Thread.currentThread().interrupt(); // End thread
            } // End finally
      } // End run() method
   } // End Player class   
} // End Server class