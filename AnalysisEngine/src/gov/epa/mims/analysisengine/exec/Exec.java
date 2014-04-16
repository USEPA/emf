package gov.epa.mims.analysisengine.exec;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.SuspendRequestor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * abstract class for exec-ing a child process
 *
 * @author Tommy E. Cathey
 * @version $Id: Exec.java,v 1.3 2010/07/19 20:25:25 rross67 Exp $
 *
 **/
abstract public class Exec implements java.io.Serializable, Cloneable {
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   static final long serialVersionUID = 1;

   /** DOCUMENT_ME */
   private static Process p = null;

   /** DOCUMENT_ME */
   private static String plottingAppCmdLine = null;

   /** DOCUMENT_ME */
   private static BufferedReader bri; //Receive Child's Stdout

   /** DOCUMENT_ME */
   private static BufferedReader bre; //Receive Child's Stderr

   /** DOCUMENT_ME */
   private static PrintStream pstream; //Write to Child's Stdin

   /** DOCUMENT_ME */
   private StdIOThread outThread;

   /** DOCUMENT_ME */
   private StdIOThread errThread;

   /** DOCUMENT_ME */
   private HashMap stdoutPipes = new HashMap();

   /** DOCUMENT_ME */
   private HashMap stderrPipes = new HashMap();

   /** DOCUMENT_ME */
   private boolean logging = false;

   /** DOCUMENT_ME */
   private PrintWriter outLog;

   /** DOCUMENT_ME */
   private String[] searchStrings;

   /** DOCUMENT_ME */
   private String[] foundStrings;

   /******************************************************
    *
    * methods
    *
    *****************************************************/
   /**
    * start the child process
    *
    * @author Tommy E. Cathey
    *
    * @exception java.io.RuntimeException if a child is currently running
    * @exception java.io.IOException if unable to start child
    * @param cmdLine the command to exec which starts the child
    * @pre cmdLine != null
    *******************************************************************/
   public void StartChildProcess(String cmdLine) throws IOException
   {
      if (p != null)
      {
         throw new RuntimeException(plottingAppCmdLine + 
                                    " currently running");
      }

      plottingAppCmdLine = cmdLine;

      p = Runtime.getRuntime().exec(cmdLine);
      connectToIOstreams();
   }

   /**
    * kill the child process
    *
    * @author Tommy E. Cathey
    *
    *******************************************************************/
   public void killChildProcess()
   {
      if (p != null)
      {
         outThread.interrupt();
         errThread.interrupt();

         try
         {
            bri.close();
            bre.close();
            pstream.close();
         }
         catch (IOException e)
         {
         }

         p.destroy();
         p = null;
      }
   }

   /**
    * register to receive the child's stdout
    *
    * @author Tommy E. Cathey
    *
    * @exception java.io.IOException if unable set up Pipes for reading and writing
    * @return the PipedReader the observer will use to receive the child's stdout
    *
    *******************************************************************/
   public PipedReader getChildsStdout() throws IOException
   {
      PipedWriter pw;
      PipedReader pr = null;
      pw = new PipedWriter();
      pr = new PipedReader(pw);
      synchronized (stdoutPipes)
      {
         stdoutPipes.put(pr, new PrintWriter(pw));
      }

      return pr;
   }

   /**
    * register to receive the child's stderr
    *
    * @author Tommy E. Cathey
    *
    * @exception java.io.IOException if unable set up Pipes for reading and writing
    * @return the PipedReader the observer will use to receive the child's stderr
    *
    *******************************************************************/
   public PipedReader getChildsStderr() throws IOException
   {
      PipedWriter pw;
      PipedReader pr = null;
      pw = new PipedWriter();
      pr = new PipedReader(pw);
      synchronized (stderrPipes)
      {
         stderrPipes.put(pr, new PrintWriter(pw));
      }

      return pr;
   }

   /**
    * write a message to the child's stdin
    *
    * @author Tommy E. Cathey
    *
    * @param str the message to send
    * @exception gov.epa.mims.analysisengine.AnalysisException if no child is currently running
    * @exception java.io.IOException if unable to write to stream
    *
    *******************************************************************/
   public void writeToChildsStdin(String str) throws IOException, 
                                                     gov.epa.mims.analysisengine.AnalysisException
   {
      if (pstream == null)
      {
         throw new AnalysisException(
               "writeToChildsStdin: Child process not started.");
      }

      if (logging)
      {
         outLog.println("STDIN:  " + str);
      }

      pstream.println(str);
//4-21-04
//      pstream.println(str);
//      byte[] b = str.getBytes();
//      pstream.write(b,0,b.length);
//4-21-04
      pstream.flush();
   }

   /**
    * cancel a previous request to receive stdout or stderr
    *
    * @author Tommy E. Cathey
    *
    * @param readerPipe the pipe previously returned from getChildsStderr() or getChildsStdout()
    * @pre readerPipe != null
    *
    *******************************************************************/
   public void disconnectPipe(PipedReader readerPipe)
   {
      PrintWriter printer = null;

      if (stdoutPipes.containsKey(readerPipe))
      {
         synchronized (stdoutPipes)
         {
            printer = (PrintWriter) stdoutPipes.remove(readerPipe);
         }

         printer.close();
      }
      else if (stderrPipes.containsKey(readerPipe))
      {
         synchronized (stderrPipes)
         {
            printer = (PrintWriter) stderrPipes.remove(readerPipe);
         }

         printer.close();
      }
   }

   /**
    * connect to a child's IO
    *
    * @author Tommy E. Cathey
    *
    *******************************************************************/
   private void connectToIOstreams()
   {
      bri = new BufferedReader(new InputStreamReader(p.getInputStream()));

      bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));


      // Get the output stream of the subprocess.
      pstream = new PrintStream(p.getOutputStream());

      outThread = new StdIOThread("stdout: ", 1);
      outThread.requestResume();
      outThread.start();
      errThread = new StdIOThread("stderr: ", 2);
      errThread.requestResume();
      errThread.start();
   }

   /**
    * suspend IO threads
    *
    * @author Tommy E. Cathey
    *
    *******************************************************************/
   protected void suspendStdio()
   {
      outThread.requestSuspend();
      errThread.requestSuspend();
   }

   /**
    * broadcast child's stdout to all observers
    *
    * @author Tommy E. Cathey
    *
    * @param str message received from the child process over stdout
    *
    *******************************************************************/
   private void broadcastStdout(String str)
   {
      if (str != null)
      {
         synchronized (stdoutPipes)
         {
            Set keys = stdoutPipes.keySet();
            Iterator iter = keys.iterator();
            PrintWriter pw;

            if (logging)
            {
               outLog.println("STDOUT: " + str);
               outLog.flush();
            }

            while (iter.hasNext())
            {
               Object pr = iter.next();

               if (stdoutPipes.containsKey(pr))
               {
                  pw = (PrintWriter) stdoutPipes.get(pr);
                  pw.println(str);
                  pw.flush();
               }
            }
         }
      }
   }

   /**
    * broadcast child's stderr to all observers
    *
    * @author Tommy E. Cathey
    *
    * @param str message received from the child process over stderr
    *
    *******************************************************************/
   private void broadcastStderr(String str)
   {
      if (str != null)
      {
         synchronized (stderrPipes)
         {
            Set keys = stderrPipes.keySet();
            Iterator iter = keys.iterator();
            PrintWriter pw;

            if (logging)
            {
               outLog.println("STDERR: " + str);
               outLog.flush();
            }

            while (iter.hasNext())
            {
               Object pr = iter.next();
               pw = (PrintWriter) stderrPipes.get(pr);
               pw.println(str);
               pw.flush();
            }
         }
      }
   }

   /**
    * set log file
    *
    * @author Tommy E. Cathey
    *
    * @param f File object
    * @exception java.io.IOException if unable to open log file
    *
    *******************************************************************/
   public void setLog(File f) throws IOException
   {
      try
      {
         outLog = new PrintWriter(new FileOutputStream(f));
         logging = true;
      }
      catch (IOException e)
      {
         logging = false;
         throw e;
      }
   }

   /**
    * set search strings to be scanned for in the child's stdout
    *
    * @author Tommy E. Cathey
    *
    * @param searchStrings an array of search strings
    *
    *******************************************************************/
   protected void scanSetSearchStrings(String[] searchStrings)
   {
      this.searchStrings = searchStrings;
      foundStrings = new String[searchStrings.length];

      for (int i = 0; i < foundStrings.length; i++)
      {
         foundStrings[i] = null;
      }
   }

   /**
    * scan child's stdout for search strings set in scanSetSearchStrings(String[])
    *
    * @author Tommy E. Cathey
    *
    * @param s a string from the child's stdout
    *
    *******************************************************************/
   private void scanStdout(String s)
   {
      for (int i = 0; i < searchStrings.length; i++)
      {
         if (s != null)
         {
            if ((searchStrings[i] != null) && (foundStrings[i] == null))
            {
               int indx = s.indexOf(searchStrings[i]);

               if (indx > -1)
               {
                  foundStrings[i] = s;
               }
            }
         }
      }
   }

   /**
    * identify which search strings were found
    *
    * @author Tommy E. Cathey
    *
    * @param i index into searchStrings[] and foundStrings[]
    * @return true if searchStrings[i] was found; otherwise false
    *
    *******************************************************************/
   public boolean scanFound(int i)
   {
      if (foundStrings[i] != null)
      {
         return true;
      }

      return false;
   }

   /**
    * return the string found to match searchStrings[i]
    *
    * @author Tommy E. Cathey
    *
    * @param i index into searchStrings[] and foundStrings[]
    * @return string found to match searchStrings[i] in childs stdout
    *
    *******************************************************************/
   public String scanGetMessage(int i)
   {
      return foundStrings[i];
   }

   /**
    * print object to String
    *
    * @author Tommy E. Cathey
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(500);
      buffer.append("serialVersionUID = ");
      buffer.append(this.serialVersionUID);
      buffer.append("p = ");

      if (this.p != null)
      {
         buffer.append(this.p.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("plottingAppCmdLine = ");
      buffer.append(this.plottingAppCmdLine);
      buffer.append("bri = ");

      if (this.bri != null)
      {
         buffer.append(this.bri.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("bre = ");

      if (this.bre != null)
      {
         buffer.append(this.bre.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("pstream = ");

      if (this.pstream != null)
      {
         buffer.append(this.pstream.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("outThread = ");

      if (this.outThread != null)
      {
         buffer.append(this.outThread.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("errThread = ");

      if (this.errThread != null)
      {
         buffer.append(this.errThread.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("stdoutPipes = ");

      if (this.stdoutPipes != null)
      {
         buffer.append(this.stdoutPipes.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("stderrPipes = ");

      if (this.stderrPipes != null)
      {
         buffer.append(this.stderrPipes.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("logging = ");
      buffer.append(this.logging);
      buffer.append("outLog = ");

      if (this.outLog != null)
      {
         buffer.append(this.outLog.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("searchStrings = ");

      if (this.searchStrings != null)
      {
         buffer.append(this.searchStrings.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      buffer.append("foundStrings = ");

      if (this.foundStrings != null)
      {
         buffer.append(this.foundStrings.toString());
      }
      else
      {
         buffer.append("value is null");
      }

      return buffer.toString();
   }

   /******************************************************
   *
   * inner classes
   *
   *****************************************************/
   /**
    * IO thread to read childs IO
    *
    * @author Tommy E. Cathey
    * @version $Id: Exec.java,v 1.3 2010/07/19 20:25:25 rross67 Exp $
    *
    **/
   class StdIOThread extends Thread
   {
      /******************************************************
       *
       * fields
       *
       *****************************************************/
      private BufferedReader br;

      /** DOCUMENT_ME */
      private String prompt;

      /** DOCUMENT_ME */
      private int IOtype;

      /** DOCUMENT_ME */
      private SuspendRequestor suspender = new SuspendRequestor();

      /******************************************************
       *
       * methods
       *
       *****************************************************/
      /**
       * constructor
       *
       * @author Tommy E. Cathey
       *
       *******************************************************************/
      public StdIOThread(String prompt, int IOtype)
      {
         this.prompt = prompt;
         this.IOtype = IOtype;
      }

      /**
       *
       * @author Tommy E. Cathey
       *
       *******************************************************************/
      public void requestSuspend()
      {
         suspender.set(true);
      }

      /**
       *
       * @author Tommy E. Cathey
       *
       *******************************************************************/
      public void requestResume()
      {
         suspender.set(false);
      }

      /**
       * run method
       *
       * @author Tommy E. Cathey
       *
       *******************************************************************/
      public void run()
      {
         try
         {
            String s = null;
            int BytesAvailable = 0;

            if (IOtype == 1)
            {
               br = bri;
            }
            else
            {
               br = bre;
            }

            while (!interrupted())
            {
               s = br.readLine();

               if (IOtype == 1)
               {
                  scanStdout(s);
                  broadcastStdout(s);
               }
               else
               {
                  broadcastStderr(s);
               }

               //
               // wait for resume iff br.readLine() is returning null
               // i.e. all IO has completed. If the child is killed, then
               // br.readLine() will return null. So before sending a
               // terminate message to the child call suspendStdio(), else
               //  this thread spins continuously producing s = null
               //
               if (s == null)
               {
                  suspender.waitForResume();
               }
            }
         }
         catch (InterruptedException e)
         {
         }
         catch (IOException e)
         {
//            e.printStackTrace();
         }
      }
   }
}
