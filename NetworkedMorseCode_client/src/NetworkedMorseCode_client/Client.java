package NetworkedMorseCode_client;

// Fig. 27.7: Client.java
// Client portion of a stream-socket connection between client and server.
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame 
{
   private JTextField enterField; // enters information from user
   private JTextArea displayArea; // display chat to user
   private JTextArea notificationArea; // display notices to user
   private ObjectOutputStream output; // output stream to server
   private ObjectInputStream input; // input stream from server
   private String message = ""; // message from server
   private String chatServer; // host server for this application
   private String name; // name of client user
   private Socket client; // socket to communicate with server
   private Boolean openConnection;
   final private Character[] characters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
       'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
       'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9','0', '.',
   ',', ':', '?', '\'', '-', '/', '"', '@', '='};
   final private String[] codes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.",
       "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-",
   ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", ".----",
   "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", 
   "-----", ".-.-.-", "--..--", "---...", "..--..", ".----.", "-....-", "-..-.",
   ".-..-.", ".--.-.", "-...-"};
   
   private  Map<Character, String> encodeMorse;
   private Map<String, Character> decodeMorse;

   // initialize chatServer and set up GUI
   public Client( String host )
   {
      super( "Happiness Enchancer 2.0" );

      chatServer = host; // set server to which this client connects
      name = JOptionPane.showInputDialog("Please enter your display name:");
      enterField = new JTextField(); // create enterField
      enterField.setEditable( false );
      enterField.addActionListener(
         new ActionListener() 
         {
            // send message to server
            public void actionPerformed( ActionEvent event )
            {
                if( event.getActionCommand().equalsIgnoreCase("terminate"))
                {
                    sendData("\\quit");
//                    try
//                    {
//                        String temp = (String) input.readObject();
//                        openConnection = temp.equalsIgnoreCase("false");
//                    } catch (IOException ex) 
//                    {
//                        ex.printStackTrace();
//                    } catch (ClassNotFoundException ex) {
//                        ex.printStackTrace();
//                    }
                }
                else
                    // Convert client message to morse code and xmit to server
                    sendData(convertToMorse( event.getActionCommand()) );
//                    sendData( "    " + event.getActionCommand() );
                enterField.setText( "" );
            } // end method actionPerformed
         } // end anonymous inner class
      ); // end call to addActionListener

      add( enterField, BorderLayout.NORTH );

      displayArea = new JTextArea(); // create displayArea
      displayArea.setEditable(false);
      add( new JScrollPane( displayArea ), BorderLayout.CENTER );
      
      notificationArea = new JTextArea(); // create notification area
      notificationArea.setEditable(false);
      notificationArea.setPreferredSize( new Dimension(800, 200));
      add( new JScrollPane( notificationArea ), BorderLayout.SOUTH);

      setSize( 800, 600 ); // set size of window
      setVisible( true ); // show window
      
      openConnection = true;
      encodeMorse = new TreeMap<>();
      decodeMorse = new TreeMap<>();
      for(int i = 0; i < characters.length; i++)
      {
          encodeMorse.put( characters[i], codes[i]);
          decodeMorse.put( codes[i], characters[i]);
      }
   } // end Client constructor

   // connect to server and process messages from server
   public void runClient() 
   {
      try // connect to server, get streams, process connection
      {
         connectToServer(); // create a Socket to make connection
         getStreams(); // get the input and output streams
         processConnection(); // process connection
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
         closeConnection(); // close connection
      } // end finally
   } // end method runClient

   // connect to server
   private void connectToServer() throws IOException
   {      
      displayNotice( "Welcome " + name + "!" );

      // create Socket to make connection to server
      client = new Socket( InetAddress.getByName( chatServer ), 12345 );
      
      // display connection information
      displayNotice( "Connected to: " + client.getInetAddress().getHostName() );
   } // end method connectToServer

   // get streams to send and receive data
   private void getStreams() throws IOException
   {
      // set up output stream for objects
      output = new ObjectOutputStream( client.getOutputStream() );
      output.writeObject(name);
      output.flush(); // flush output buffer to send header information

      // set up input stream for objects
      input = new ObjectInputStream( client.getInputStream() );

      displayNotice( "Got I/O streams" );
   } // end method getStreams

   // process connection with server
   private void processConnection() throws IOException
   {
      // enable enterField so client user can send messages
      setTextFieldEditable( true );
      int newLine;

      while ( openConnection ) // process messages sent from server
      { 
         try // read message and display it
         {
            message = ( String ) input.readObject(); // read new message
            
            newLine = message.indexOf('\n');
            displayMessage( message.substring( 0, newLine) ); // remove name of client from coded message
            displayMessage( "     " + convertToText(message.substring(newLine + 1)) ); // display message
         } // end try
         catch ( ClassNotFoundException classNotFoundException ) 
         {
            displayNotice( "Unknown object type received" );
         } // end catch

      }
   } // end method processConnection

   // close streams and socket
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
   } // end method closeConnection

   // send message to server
   private void sendData( String message )
   {
      try // send object to server
      {
//          displayMessage("\n" + name + ":");
          output.writeObject(message);
          output.flush(); // flush data to output
      } // end try
      catch ( IOException ioException )
      {
         displayNotice( "Error writing object" );
      } // end catch
   } // end method sendData

   // manipulates displayArea in the event-dispatch thread
   private void displayMessage( final String messageToDisplay )
   {
      SwingUtilities.invokeLater(
         new Runnable()
         {
            public void run() // updates displayArea
            {
                displayArea.append( messageToDisplay + "\n");
            } // end method run
         }  // end anonymous inner class
      ); // end call to SwingUtilities.invokeLater
   } // end method displayMessage
   
   // manipulates displayArea in the event-dispatch thread
   private void displayNotice( final String messageToDisplay )
   {
      SwingUtilities.invokeLater(
         new Runnable()
         {
            public void run() // updates displayArea
            {
                notificationArea.append( messageToDisplay + "\n");
            } // end method run
         }  // end anonymous inner class
      ); // end call to SwingUtilities.invokeLater
   } // end method displayMessage   

   // manipulates enterField in the event-dispatch thread
   private void setTextFieldEditable( final boolean editable )
   {
      SwingUtilities.invokeLater(
         new Runnable() 
         {
            public void run() // sets enterField's editability
            {
               enterField.setEditable( editable );
            } // end method run
         } // end anonymous inner class
      ); // end call to SwingUtilities.invokeLater
   } // end method setTextFieldEditable
   
   // Encode message into morse code
   private String convertToMorse( String encodeMessage )
   {
       String[] tempWords = encodeMessage.split(" ");
       Character tempChar;
       StringBuilder codedString = new StringBuilder();
       for (String token : tempWords)
       {
           for (int i = 0; i < token.length(); i++)
           {
               tempChar = Character.toLowerCase(token.charAt(i) );
               // Convert character to matched morse code value
               if ( encodeMorse.containsKey(tempChar) )
                   codedString.append( encodeMorse.get( tempChar ) );
               // Is no morse code equivalent, insert character as literal character
               else
                   codedString.append( token.charAt(i));
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
       String[] tempWords = decodeMessage.split("//");
       String[] tempString;
       StringBuilder decodedString = new StringBuilder();
       Boolean capNext = true;
       // Loop through each encoded word
       for ( String token : tempWords)
       {
           tempString = token.split("/"); // Break each encoded word into a single coded character
           for (String code : tempString)
           {
               // Convert coded character to matched alphabet character
               if ( decodeMorse.containsKey( code ) )
               {
                   if (capNext)
                   {
                       decodedString.append( Character.toUpperCase(decodeMorse.get( code )) );
                       capNext = false;
                   }
                   else
                       decodedString.append( decodeMorse.get( code ) );
                    if ( code.equals(".-.-.-") || code.equals("..--..") || code.equals("!") )
                        capNext = true;                   
               }
               // Is no mapped morse code equivalent, insert as literal character
               else
                   decodedString.append( code );
           }
           decodedString.append(" "); // Separate words by a space
       }
       
       return decodedString.toString();
   } // end convertToText() method
   
} // end class Client

/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 *************************************************************************/
