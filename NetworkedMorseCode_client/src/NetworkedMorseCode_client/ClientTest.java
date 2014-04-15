/* Filename:        ClientTest.java
 * Last Modified:   15 April 2014
 * Author:          Todd Parker
 * Email:           todd.i.parker@maine.edu
 * Course:          CIS314 - Advanced Java
 * 
 * ClientTest.java creates a client object, and then runs the client method
 * runClient(). runClient() is responsibile detecting and/or creating a server
 * connection, in addition to creating the I/O streams and maintaining the 
 * connection. If the server is not running at the time the client is launched, 
 * the client will notify the user of such and require the client be reopend 
 * after the server is started. A specific client host can be opened by passing 
 * the IP address as a command line argument, and the default is to utilize 
 * localhost. The client application runs on port 12345.
 */
package NetworkedMorseCode_client;

import javax.swing.JFrame;

public class ClientTest 
{
   public static void main( String[] args )
   {
      Client application; // declare client application

      // if no command line args, use localhost
      if ( args.length == 0 )
         application = new Client( "127.0.0.1" ); // connect to localhost
      // else try opening the host specified as command line argument
      else
         application = new Client( args[ 0 ] ); // use args to connect

      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      application.setResizable(false);
      application.runClient(); // run client application
   } // end main
} // end class ClientTest