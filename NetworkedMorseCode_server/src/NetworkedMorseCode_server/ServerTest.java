/* Filename:        ServerTest.java
 * Last Modified:   15 April 2014
 * Author:          Todd Parker
 * Email:           todd.i.parker@maine.edu
 * Course:          CIS314 - Advanced Java
 * 
 * ServerTest.java creates a server object, and then runs the server method
 * execute(). execute() is responsible for the creation and execution of chat
 * clients, and, runs until either 100 clients have connected or the server 
 * application is terminated. execute() is the only public method of the class.
 */
package NetworkedMorseCode_server;

import javax.swing.JFrame;

public class ServerTest
{
   public static void main( String[] args )
   {
      Server application = new Server(); // Create server
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      application.execute(); // Run server application
   } // End main
} // End class ServerTest