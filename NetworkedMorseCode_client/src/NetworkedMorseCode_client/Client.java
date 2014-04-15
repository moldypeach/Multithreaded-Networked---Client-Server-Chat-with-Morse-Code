/* Filename:        Client.java
 * Last Modified:   15 April 2014
 * Author:          Todd Parker
 * Email:           todd.i.parker@maine.edu
 * Course:          CIS314 - Advanced Java
 * 
 * Client.java creates the client application GUI in addition to establishing
 * and maintaining its connection to the server. Method runClient() is called 
 * from the main application to establish the client object. Of note is that
 * runClient calls method processConnection(), which loops repeatedly until 
 * either the action listener defined in Client's constructor detects that the 
 * user typed "terminate", or it is detected that the user manually closed the 
 * Client application window. Every message a client user types is first encoded
 * into Morse Code via method convertToMorse(), then transmitted to the server
 * whereupon the serve disseminates the message to all other connected clients.
 * When a client application receives a message it is decoded back into text
 * via method convertToText(), and then appended to the display area.
 *
 * NOTE: Some code of this class was modified from the course textbook
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and Pearson Education, 
 * Inc. All Rights Reserved.   
 */
package NetworkedMorseCode_client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Client extends JFrame 
{
   private JTextField enterField; // enters information from user
   private JTextArea displayArea; // display chat to user
   private JTextArea notificationArea; // display notices to user
   private JScrollPane notices; // display scroll pane for notifications
   private ObjectOutputStream output; // output stream to server
   private ObjectInputStream input; // input stream from server
   private String message = ""; // message from server
   private String chatServer; // host server for this application
   private String name = ""; // name of client user
   private Socket client; // socket to communicate with server
   private Boolean openConnection; // state of connection (i.e. open/closed)
   // Array of Morse Code characters
   final private Character[] characters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
       'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
       'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9','0', '.',
   ',', ':', '?', '\'', '-', '/', '"', '@', '='};
   // Array of Morse Codes
   final private String[] codes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.",
       "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-",
   ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", ".----",
   "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", 
   "-----", ".-.-.-", "--..--", "---...", "..--..", ".----.", "-....-", "-..-.",
   ".-..-.", ".--.-.", "-...-"};
   private  Map<Character, String> encodeMorse; // Map for encoding text-to-Morse
   private Map<String, Character> decodeMorse; // Map for decoding Morse-to-text

   // constructor sets up chat Client and GUI
   public Client( String host )
   {
      super( "Happiness Enchancer 2.0" ); // Set Client window's title

      chatServer = host; // set server to which this client connects
      // Get a chat name from user, must contain at least one nonspace character
      while ( (name.length() == 0) || (name.replace(" ", "").length() == 0))
        name = JOptionPane.showInputDialog("Please enter your display name:");
      enterField = new JTextField(); // create enterField for text input
      enterField.setEditable( false ); // Disallow input until connection
      enterField.addActionListener( // Detect and process user-input
         new ActionListener() 
         {
            public void actionPerformed( ActionEvent event )
            {
                // If user typed terminate, notify server and close connection
                if( event.getActionCommand().equalsIgnoreCase("terminate"))
                {
                    sendData("\\quit");
                    openConnection = false;
                }
                // Else convert client message to morse code and xmit to server
                else
                    sendData(convertToMorse( event.getActionCommand()) );
                enterField.setText( "" ); // Clear input field
            } // End actionPerformed() method
         } // End anonymous inner class
      ); // End addActionListener
      
      this.addWindowListener( // Detect if user closed window
        new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e)
            {
                // If a connection was open on window close, force method
                // closeConnection() to run
                if (openConnection)
                {
                    sendData("\\quit");
                    openConnection = false;
                }
                dispose(); // Releases all resources used by this Window
            } // End windowClosing() method
        } // End anonymouse inner class
      ); // End addWindowListener()

      add( enterField, BorderLayout.NORTH ); // Add input field

      displayArea = new JTextArea(); // Create displayArea
      displayArea.setEditable(false); // Disallow input for display area
      add( new JScrollPane( displayArea ), BorderLayout.CENTER ); // Add display area to JFrame
      
      notificationArea = new JTextArea(); // create notification area
      notificationArea.setEditable(false); // Disallow input for notification area
      notificationArea.setBackground(Color.LIGHT_GRAY);
      notificationArea.setForeground(Color.DARK_GRAY);
      notices = new JScrollPane(notificationArea);
      notices.setPreferredSize( new Dimension(800, 150)); // Set size of notificatin window 
      add( notices, BorderLayout.SOUTH); // Add notification area to JFrame

      setSize( 800, 600 ); // Set size of JFrame window
      setVisible( true ); // Show window
      
      openConnection = true;
      encodeMorse = new TreeMap<>();
      decodeMorse = new TreeMap<>();
      // Create encode and decode Maps
      for(int i = 0; i < characters.length; i++)
      {
          encodeMorse.put( characters[i], codes[i]);
          decodeMorse.put( codes[i], characters[i]);
      }
   } // end Client constructor

   // connect to server and process messages from server
   public void runClient() 
   {
       Boolean socketConnected = false;
       try // Connect to server, get streams, process connection
       {
           // If socket creation is successful. On failure abort operation
           if ( socketConnected = connectToServer() )
           {
               getStreams(); // Get the input and output streams
               processConnection(); // Process connection
           } // end if socketConnect
       } // end try
       catch ( EOFException eofException ) 
       {
          displayNotice( "Client terminated connection" );
       } // end catch
       catch ( IOException ioException ) 
       {
          ioException.printStackTrace();
       } // end catch
       finally 
       {
           // If socket didn't connect, ignore socket close method
           if (socketConnected)
             closeConnection(); // close connection
       } // end finally
    } // end method runClient

   // Connect to server
   private boolean connectToServer() throws IOException
   {      
      displayNotice( "Welcome " + name + "!" ); // Greet user

      try
      {
        // create Socket to make connection to server
        client = new Socket( InetAddress.getByName( chatServer ), 12345 );
      }
      catch(IOException e)
      {
          displayNotice("Server connection could not be established");
          return false;
      }
      
      // display connection information
      displayNotice( "Connected to: " + client.getInetAddress().getHostName() );
      return true;
   } // End connectToServer() method

   // Get I/O streams
   private void getStreams() throws IOException
   {
      // Set up output stream for objects
      output = new ObjectOutputStream( client.getOutputStream() );
      output.writeObject(name); // Send name of chat user to server
      output.flush(); // Flush output buffer to send header information

      // set up input stream for objects
      input = new ObjectInputStream( client.getInputStream() );

      displayNotice( "Got I/O streams" );
   } // End getStreams() method

   // Process connection with server
   private void processConnection() throws IOException
   {
      // Enable enterField so client user can send messages
      setTextFieldEditable( true );
      int newLine; // String index of a detected newline character

      while ( openConnection ) // Process messages sent from server
      { 
         try // Read message and display it
         {
            message = ( String ) input.readObject(); // read new message
            
            newLine = message.indexOf('\n');
            // Remove name of client from coded message
            displayMessage( message.substring( 0, newLine) );
            // Display message
            displayMessage( "     " + convertToText(message.substring(newLine + 1)) );
         } // End try
         catch ( ClassNotFoundException classNotFoundException ) 
         {
            displayNotice( "Unknown object type received" );
         } // End catch

      } // End while openConnection
   } // End processConnection() method

   // Close streams and socket
   private void closeConnection() 
   {
      displayNotice( "Closing connection" );
      setTextFieldEditable( false ); // disable enterField

      try 
      {
         output.close(); // close output stream
         input.close(); // close input stream
         client.close(); // close socket
      } // end try
      catch ( IOException ioException ) 
      {
         ioException.printStackTrace();
      } // end catch
   } // end closeConnection() method

   // Send message to server
   private void sendData( String message )
   {
      try // Send object to server
      {
          output.writeObject(message); // Send message to server
          output.flush(); // Flush data to output
      } // End try
      catch ( IOException ioException )
      {
         displayNotice( "Error writing object" );
      } // End catch
   } // End sendData() method

   // Manipulate displayArea in the event-dispatch thread
   private void displayMessage( final String messageToDisplay )
   {
      SwingUtilities.invokeLater(
         new Runnable()
         {
            public void run() // Updates displayArea
            {
                displayArea.append( messageToDisplay + "\n");
            } // End run() method
         }  // End anonymous inner class
      ); // End SwingUtilities.invokeLater
   } // End displayMessage() method
   
   // Manipulate displayArea in the event-dispatch thread
   private void displayNotice( final String messageToDisplay )
   {
      SwingUtilities.invokeLater(
         new Runnable()
         {
            public void run() // Updates displayArea
            {
                notificationArea.append( messageToDisplay + "\n");
            } // End run() method
         }  // End anonymous inner class
      ); // End SwingUtilities.invokeLater
   } // End displayMessage() method

   // Manipulate enterField in the event-dispatch thread
   private void setTextFieldEditable( final boolean editable )
   {
      SwingUtilities.invokeLater(
         new Runnable() 
         {
            public void run() // Sets enterField's editability
            {
               enterField.setEditable( editable );
            } // End run() method
         } // End anonymous inner class
      ); // End SwingUtilities.invokeLater
   } // End setTextFieldEditable() method
   
   // Encode message into morse code
   private String convertToMorse( String encodeMessage )
   {
       // Tokenize encodeMessage by whitespace
       String[] tempWords = encodeMessage.split(" ");
       Character tempChar; // Store each individual token
       StringBuilder codedString = new StringBuilder(); // Build encoded message
       for (String token : tempWords) // For each token from encodeMessage
       {
           for (int i = 0; i < token.length(); i++) // For each character of token
           {
               // Convert character to lowercase
               tempChar = Character.toLowerCase(token.charAt(i) );
               // Convert character to matched morse code value
               if ( encodeMorse.containsKey(tempChar) )
                   codedString.append( encodeMorse.get( tempChar ) );
               // Is no morse code equivalent, insert as literal character
               else
                   codedString.append( token.charAt(i));
               // If at end of token's length, do not add a single '/' separator
               if (i != (token.length() - 1) )
                   codedString.append('/'); // Separate morse code characters by a /
           }
           codedString.append("//"); // Separate morse code words by //
       }
       
       displayNotice( "     Your Encoded Message is:\n          " + 
                      codedString.toString() + "\n" );
       
       return codedString.toString();
   } // end convertToMorse() method
   
   // Decode message into a string
   private String convertToText( String decodeMessage )
   {
       // Tokenize decodeMessage by "//" into Morse Code words
       String[] tempWords = decodeMessage.split("//");
       String[] tempString; // Store tokens of individual Morse Code letters
       StringBuilder decodedString = new StringBuilder(); // Build decodeMessage
       Boolean capNext = true; // Detect when a letter should be capitalized
 
       for ( String token : tempWords) // For each encoded word
       {
           tempString = token.split("/"); // Break each encoded word into a single coded character
           for (String code : tempString) // For each encoded character of a word
           {
               // Convert coded character to matched alphabet character
               if ( decodeMorse.containsKey( code ) )
               {
                   if (capNext) // Capitalize next character
                   {
                       decodedString.append( Character.toUpperCase(decodeMorse.get( code )) );
                       capNext = false;
                   } // End if capNext
                   else
                       decodedString.append( decodeMorse.get( code ) );
                    if ( code.equals(".-.-.-") || code.equals("..--..") || code.equals('!') )
                        capNext = true;                   
               } // End if code exists in decode Map
               // Is no mapped morse code equivalent, insert as literal character
               else
                   decodedString.append( code );
           } // End for each encoded character of a word
           decodedString.append(" "); // Separate words by a space
       } // End for each encoded word
       
       return decodedString.toString();
   } // End convertToText() method
   
} // End class Client