package com.socket.server;

import java.net.*;
import java.io.*;

public class Server extends Thread
{
   private ServerSocket serverSocket;
   private static int counter = 1;
   private static long size;
   
   // subject to change
   static final private String type = ".txt";
   static final private String folder = "Log";
   // subject to change
   
   public Server(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(10000);
   }

   public void run()
   {
      while(true)
      {
         try
         {
        	// Print the port
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            System.out.println("Server address is " + serverSocket.getInetAddress() + "...");
            // If this executed, it's working
            Socket server = serverSocket.accept();
            System.out.println("Server address is " + server.getInetAddress() + "...");
            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            
            try
            {
            	// from socket
            	InputStream is = server.getInputStream();
            	// buffer socket
            	BufferedInputStream bis = new BufferedInputStream(is);
            	// Establish new file
                File output = new File("//Users//fuhao//Desktop//"+folder+"//"+Integer.toString(counter)+type);
                // File output stream begin
                FileOutputStream fos = new FileOutputStream(output);
                // Buffered output 
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                // create standard buffer
                byte [] buffer = new byte [8];
                // create header buffer
                byte [] header = new byte [8];
                // create 
                // read header
                bis.read(header, 0, 8);
                size = 0;
                for (int k=0; k<8; k++)
                {
                	size = size + (long)((header[k] & 0x000000ff) << (56-k*8));
                }
                System.out.println("The file length is " + (size) +" bytes");
                // write from buffer to bos
                for (int k=0; k<size/buffer.length; k++)
                {
                	bis.read(buffer, 0, buffer.length);
                	bos.write(buffer, 0, buffer.length);
                }
                byte [] end = new byte [(int)(size)-(int)(size/buffer.length)*buffer.length];
                bis.read(end, 0, end.length);
            	bos.write(end, 0, end.length);
                bos.close();
                counter++;
                server.close();
                System.out.println("File successfully generated.");
                
            }
            catch (IOException e)
            {
            	System.out.println("File output error !");
            	break;
            }
            
         }catch(SocketTimeoutException s)
         {
            System.out.println("Session timed out!");
            //break;
         }
         catch(IOException e)
         {
            e.printStackTrace();
            break;
         }
      }
      
   }
   public static void main(String [] args)
   {
	   // create a directory
	   File imdir = new File("//Users//fuhao//Desktop//"+folder);
	   if (!imdir.exists())
	   {
		   imdir.mkdir();
		   System.out.println("Directory made !");
	   }
	   if (imdir.exists())
	   {
		   System.out.println("Directory exists !");
		   File [] files = new File("//Users//fuhao//Desktop//"+folder).listFiles();
		   counter = files.length;
		   if (counter == 0)
		   {
			   counter = 1;
		   }
	   }
	   
	   // Main program
	   int port = 9999;
	   try
	   {
		   Thread t = new Server(port);
		   t.start();
	   }catch(IOException e)
	   {
		   e.printStackTrace();
	   }
   }
}
