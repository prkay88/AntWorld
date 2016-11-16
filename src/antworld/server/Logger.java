package antworld.server;

//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Date;


public class Logger
{
//
//  private static final String LOG_PREFIX = "Log_";
//  private static final String LOG_EXTENSION = ".txt";
//  private static final String PATH = "logs/";
//
//  private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH-mm");
//
//  private File logFile;
//  private BufferedWriter writer;
//
//
//  public Logger(String NestStr)
//  {
//    String fileName = LOG_PREFIX + FILE_DATE_FORMAT.format(new Date()) + "." + NestStr + LOG_EXTENSION;
//
//    logFile = new File(PATH, fileName);
//    logFile.getParentFile().mkdir();
//
//    try
//    {
//      writer = new BufferedWriter(new FileWriter(logFile));
//    }
//    catch (Exception e)
//    {
//      System.err.println("Logger(): " + e.getMessage());
//    }
//  }
//
//  public synchronized void write(String msg)
//  {
//    try
//    {
//      writer.write(msg);
//    }
//    catch (Exception e)
//    {
//      System.err.println("Logger.write(): " + e.getMessage());
//    }
//  }
//
//  public void closeLog()
//  {
//    try
//    {
//      writer.close();
//    }
//    catch (Exception e)
//    {}
//  }

}
