Multithreaded-Networked---Client-Server-Chat-with-Morse-Code
============================================================

Networked Morse Code

Complete exercise 27.3 on page 1169 of the Deitel & Deitel book.  You will be implementing a multi-threaded, 
client-server project that allows clients to chat in Morse code across a network.  You can use Listing 27.5 
(or any of the other listings in the chapter) as the basis for your client-server implementation.

When designing your program you may take advantage of these allowances:

    •	There should be a single server that does not understand Morse code.
    •	There can be two or more clients that exchange messages in Morse code.
    •	You may rely on a fixed number of clients.
    •	Your server may wait until all clients have connected before passing the messages. (i.e., clients may not join or leave during the chat session)
    •	Your clients should accept English sentences as input, but should transmit Morse code.
    •	You will have two programs --  a client program and a server program.  The easiest way to do this in NetBeans appears to be putting each in a separate Project – feel free to do so.
    •	You may use the port 12345 that Deitel & Deitel use in their examples.
    •	While this is a socket implementation, all the processes can run on the same machine for your testing (use localhost, 127.0.0.1 for the network address).
    •	The server MUST be multi-threaded.
