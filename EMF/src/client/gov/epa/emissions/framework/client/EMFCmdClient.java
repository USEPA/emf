package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.casemanagement.CaseAssistService;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EMFCmdClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private static ServiceLocator serviceLocator;

    private static int exitValue = 0;

    private static boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        String debugString = System.getProperty("DEBUG_EMF_CLIENT");
        if (debugString != null && debugString.equalsIgnoreCase("TRUE"))
            DEBUG = true;

        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList(args));

        if (options.contains("-d")) {
            DEBUG = true;
        }

        if (DEBUG)
            System.out.println("EMF command line client initialized on: " + new Date() + "; version ="
                    + LoginWindow.EMF_VERSION);

        if (args.length <= 1) {
            displayHelp();
            return;
        }

        if (args.length == 2 && (args[1].equalsIgnoreCase("-h") || args[1].equalsIgnoreCase("--help"))) {
            displayHelp();
            return;
        }

        if (options.contains("-f")) {
            final List<String> optionsVal = options;

            Thread runningThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        runFromFile(optionsVal);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    } finally {
                        if (DEBUG)
                            System.out.println("runFromFile() exits before 60 second timeout.");
                        System.exit(exitValue);
                    }
                }
            });

            runningThread.start();

            Thread.sleep(60000);

            if (DEBUG)
                System.out.println("runFromFile() exits after 60 second timeout.");
            
            System.err.println("ERROR: EMF command client frozen.");
            System.exit(5);
        }

        if (!options.contains("-k")) {
            System.out.println("Please specify required options '-k'.");
            displayHelp();
            return;
        }

        if ((options.contains("-F") || options.contains("-P")) && !options.contains("-T")) {
            System.out.println("Please specify required dataset type. Outputs not registered.");
            displayHelp();
        }

        int keyIndex = options.indexOf("-k");
        int typeIndex = options.indexOf("-t");
        String keyString = options.get(++keyIndex);
        String typeString = (typeIndex < 0) ? "" : options.get(++typeIndex);

        if (keyString.startsWith("-")) {
            System.out.println("Please specify a correct jobkey.");
            return;
        }

        String msgTypeError = "Please specify a correct message type - i (info), e (error), w (warning).";

        if (typeString != null && !typeString.isEmpty() && typeString.startsWith("-")) {
            System.out.println(msgTypeError);
            return;
        }

        if (typeString != null && !typeString.isEmpty() && !typeString.equalsIgnoreCase("i")
                && !typeString.equalsIgnoreCase("e") && !typeString.equalsIgnoreCase("w")) {
            System.out.println(msgTypeError);
            return;
        }

        run(options);

        System.exit(0);
    }

    private static void displayHelp() {
        System.out.println("Usage:\njava " + EMFClient.class.getName() + " [url] [options]\n"
                + "\n\turl: location of EMF Services. Defaults to " + DEFAULT_URL + "\n" + "\n\toptions:\n"
                + "\n\t-h --help\tShow this help message and exit" + "\n\t-k --jobkey\tJob key code"
                + "\n\t-x --execPath\tFull path to script or executable" + "\n\t-p --period\tEMF period"
                + "\n\t-m --message\tText of message"
                + "\n\t-s --status\tStatus, acceptable values: 'Submitted', 'Running', 'Failed', or 'Completed'"
                + "\n\t-t --msg_type\tMessage type: i (info), e (error), w (warning)"
                + "\n\t-F single file to register as a dataset"
                + "\n\t-D path to the directory where files to register as datasets"
                + "\n\t-P pattern of the files to register as datasets" + "\n\t-T dataset type" + "\n\t-N dataset name"
                + "\n\t-O output name (optional, default to dataset name)");

        System.out.println("\nOr:\toptions:\n" + "\n\t-f --file\tSend job messages directly from this file\n"
                + "\n\t-n --line_number\tSending job messages should begin at this line number\n");
    }

    private static void run(List<String> args) throws Exception {
        int keyIndex = args.indexOf("-k");
        int execIndex = args.indexOf("-x");
        int periodIndex = args.indexOf("-p");
        int msgIndex = args.indexOf("-m");
        int statusIndex = args.indexOf("-s");
        int typeIndex = args.indexOf("-t");
        int logIntervalIndex = args.indexOf("-l");
        int resendTimesIndex = args.indexOf("-r");
        int singleFileIndex = args.indexOf("-F");
        int fileFolderIndex = args.indexOf("-D");
        int filePatternIndex = args.indexOf("-P");
        int outputTypeIndex = args.indexOf("-T");
        int outputDatasetNameIndex = args.indexOf("-N");
        int outputNameIndex = args.indexOf("-O");
        int relocationIndex = args.indexOf("-M");

        String authKey = (keyIndex < 0) ? "" : args.get(++keyIndex);
        String singleFile = (singleFileIndex < 0) ? "" : args.get(++singleFileIndex);
        String fileFolder = (fileFolderIndex < 0) ? "" : args.get(++fileFolderIndex);
        String filePattern = (filePatternIndex < 0) ? "" : args.get(++filePatternIndex);
        
        if (relocationIndex >= 0)
            relocateFiles(args, authKey, singleFile, fileFolder, filePattern);
            
        String execPath = (execIndex < 0) ? "" : args.get(++execIndex);
        String period = (periodIndex < 0) ? "" : args.get(++periodIndex);
        String message = (msgIndex < 0) ? "" : args.get(++msgIndex);
        String status = (statusIndex < 0) ? "" : args.get(++statusIndex);
        String type = (typeIndex < 0) ? "" : args.get(++typeIndex);
        int logInterval = (logIntervalIndex < 0) ? 0 : Integer.parseInt(args.get(++logIntervalIndex));
        int resendTimes = (resendTimesIndex < 0) ? 1 : Integer.parseInt(args.get(++resendTimesIndex));
        String outputType = (outputTypeIndex < 0) ? "" : args.get(++outputTypeIndex);
        String outputDatasetName = (outputDatasetNameIndex < 0) ? "" : args.get(++outputDatasetNameIndex);
        String outputName = (outputNameIndex < 0) ? "" : args.get(++outputNameIndex);

        String loggerDir = System.getenv("EMF_LOGGERDIR");
        String jobName = createSafeName(System.getenv("EMF_JOBNAME"));
        String caseName = createSafeName(System.getenv("CASE"));
        String logFile = loggerDir + File.separator + jobName + "_" + caseName + "_" + authKey + ".csv";

        if (execPath.startsWith("-") || period.startsWith("-") || message.startsWith("-") || status.startsWith("-"))
            throw new Exception("Please specify valid values for options.");

        File execFile = new File(execPath);

        JobMessage jobMsg = new JobMessage();
        jobMsg.setExecName(execPath.isEmpty() ? "" : execPath.substring(execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setExecPath(execPath.isEmpty() ? "" : execPath.substring(0, execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setMessage(message);
        jobMsg.setMessageType((type.isEmpty()) ? "i" : type);
        jobMsg.setStatus(status);
        jobMsg.setPeriod(period);
        jobMsg.setRemoteUser(System.getProperty("user.name"));
        jobMsg.setExecModifiedDate(execFile.exists() ? new Date(execFile.lastModified()) : null);
        jobMsg.setReceivedTime(new Date());

        CaseOutput output = new CaseOutput(outputName);
        output.setDatasetFile(singleFile);
        output.setDatasetName(outputDatasetName);
        output.setPath(fileFolder);
        output.setDatasetType(outputType);
        output.setPattern(filePattern);
        // output = setOutputEmptyProp(output);

        if (loggerDir == null || loggerDir.isEmpty())
            sendMessage(args, authKey, jobMsg, output);
        else
            writeToLogger(args, logFile, logInterval, resendTimes, authKey, jobMsg, output);
    }

    private static void runFromFile(List<String> args) throws Exception {
        int startLineIndex = args.indexOf("-n");
        int msgFileIndex = args.indexOf("-f");
        int resendTimes = 1; //Just want to send one time if reading job messages/outputs from a file
        String msgFile = (msgFileIndex < 0) ? "" : args.get(++msgFileIndex).trim();
        
        if (startLineIndex < 0)
             throw new Exception("Starting line number not specified for the job message file.");
        
        long startLine = Long.parseLong(args.get(++startLineIndex));
         

        if (!msgFile.isEmpty()) {
            sendLogsFromFile(args, resendTimes, msgFile, startLine);
        }
    }

    private static void sendMessage(List<String> args, String jobkey, JobMessage jobMsg, CaseOutput output)
            throws Exception {
        int exitValue = 0;

        try {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey }, new CaseOutput[] { output });
        } catch (Exception exc) {
            String errorString = exc.getMessage();

            exitValue = getExitValue(errorString);

            if (DEBUG)
                System.out.println("Exception starting client: " + exc.getMessage());

            throw exc;
        } finally {
            System.exit(exitValue);
        }
    }

    /* make a consistant exit value based on message contents */
    private static int getExitValue(String errorString) {
        if (exitValue != 0)
            return exitValue;

        if (errorString == null || (errorString.length() == 0))
            exitValue = 0;

        else if (errorString.contains("Error recording job messages"))
            exitValue = 1;

        else if (errorString.contains("Error registering output"))
            exitValue = 2;
        else
            exitValue = 3; // unkown errors

        return exitValue;
    }

    private static void sendLogs(List<String> args, int logInterval, int resendTimes, String logfile,
            String logAsistFile, String jobkey, JobMessage jobMsg, CaseOutput output, boolean now) throws Exception {
        List<JobMessage> msgs = new ArrayList<JobMessage>();
        List<String> keys = new ArrayList<String>();
        List<CaseOutput> outputs = new ArrayList<CaseOutput>();
        long start = 0;
        long end = 0;

        File logFile = new File(logfile);

        if (!logFile.exists()) {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey }, new CaseOutput[] { output });
            return;
        }

        if (DEBUG)
            System.out.println("EMF client starts reading log assistance file: " + logAsistFile + " " + new Date());

        BufferedReader asistFileReader = new BufferedReader(new FileReader(logAsistFile));
        long sentLines = Long.parseLong(asistFileReader.readLine().trim());
        asistFileReader.close();

        if (DEBUG)
            System.out.println("EMF client starts reading log assistance file: " + logAsistFile + " " + new Date());

        if (DEBUG)
            System.out.println("EMF client starts reading log file: " + logfile + " " + new Date());

        BufferedReader br = new BufferedReader(new FileReader(logfile));
        String line = br.readLine();
        long lineCount = 1;

        while ((line = br.readLine()) != null) {
            ++lineCount;

            if (lineCount >= sentLines) {
                msgs.add(extractJobMsg(line));
                keys.add(extractJobkey(line));
                CaseOutput newOutput = extractOutput(line);
                outputs.add(newOutput);

                if (start == 0)
                    start = getTime(line);

                end = getTime(line);
            }
        }

        br.close();

        if (DEBUG)
            System.out.println("EMF client has finished reading log file: " + logfile + " " + new Date());

        String errorString = null;

        if (now || ((end - start) / 1000 > logInterval)) {
            errorString = resend(args, resendTimes, msgs, keys, outputs);
            rewriteSentLinesNumber(++lineCount, logAsistFile);
        }

        if (DEBUG)
            System.out.println("EMF command line client exited.");

        System.exit(getExitValue(errorString));
    }

    private static void sendLogsFromFile(List<String> args, int resendTimes, String logfile, long sentLines)
            throws Exception {
        List<JobMessage> msgs = new ArrayList<JobMessage>();
        List<String> keys = new ArrayList<String>();
        List<CaseOutput> outputs = new ArrayList<CaseOutput>();
        File logFile = new File(logfile);

        if (!logFile.exists()) {
            System.out.println("Specified log file: " + logfile + " doesn't exist.");
            return;
        }

        if (DEBUG)
            System.out.println("EMF client starts reading log file: " + logfile + " " + new Date());

        BufferedReader br = new BufferedReader(new FileReader(logfile));
        String line = br.readLine();
        long lineCount = 1;

        while ((line = br.readLine()) != null) {
            ++lineCount;

            if (lineCount >= sentLines) {
                msgs.add(extractJobMsg(line));
                keys.add(extractJobkey(line));

                CaseOutput nextOutput = extractOutput(line);
                outputs.add(nextOutput);
            }
        }

        br.close();

        if (DEBUG)
            System.out.println("EMF client has finished reading log file: " + logfile + " " + new Date());

        String errorString = null;

        if (msgs.size() > 0)
            errorString = resend(args, resendTimes, msgs, keys, outputs);

        System.exit(getExitValue(errorString));
    }

    private static void rewriteSentLinesNumber(long lineCount, String logAsistFile) throws Exception {
        if (DEBUG)
            System.out.println("EMF client starts writing log assistance file: " + logAsistFile + " " + new Date());

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logAsistFile)));
        writer.println(lineCount);
        writer.close();

        if (DEBUG)
            System.out.println("EMF client has finished writing log assistance file: " + logAsistFile + " "
                    + new Date());
    }

    private static String resend(List<String> args, int resendTimes, List<JobMessage> msgs,
            List<String> keys, List<CaseOutput> outputs) {
        String errorString = null;

        while (resendTimes > 0) {
            try {
                send(args, msgs.toArray(new JobMessage[0]), keys.toArray(new String[0]), outputs
                        .toArray(new CaseOutput[0]));
                resendTimes = 0;
                errorString = "";
            } catch (Exception e) {
                --resendTimes;
                errorString += e.getMessage();
            }
        }

        return errorString;
    }

    private static void send(List<String> args, JobMessage[] msgs, String[] keys, CaseOutput[] outputs)
            throws Exception {
        try {
            if (DEBUG)
                System.out.println("EMF Command Client starts sending messages to server at: " + new Date());

            CaseAssistService service = getService(args);
            ArrayList keyArrayList = new ArrayList();
            JobMessage[] nonEmptyMessages = getNonEmptyMsgs(msgs, keys, keyArrayList);
            String[] nonEmptyKeys = new String[nonEmptyMessages.length];
            keyArrayList.toArray(nonEmptyKeys);

            service.recordJobMessages(nonEmptyMessages, nonEmptyKeys); // just need to send nonEmptykeys here

            if (DEBUG) {
                System.out.println("EMF command client sent " + nonEmptyMessages.length + " messages successfully.");
            }
            ArrayList outputKeyArrayList = new ArrayList();
            CaseOutput[] nonEmptyOutputs = getNonEmptyOutputs(outputs, keys, outputKeyArrayList);
            String[] nonEmptyOutputKeys = new String[outputKeyArrayList.size()];
            outputKeyArrayList.toArray(nonEmptyOutputKeys);

            registerOutputs(nonEmptyOutputKeys, service, nonEmptyOutputs);

            if (DEBUG) {
                System.out.println("EMF command client sent " + nonEmptyOutputs.length + " outputs successfully.");
                System.out.println("EMF Command Client exited successfully on " + new Date());
            }
        } catch (Exception e) {
            System.out.println("EMF Command Client encountered a problem on " + new Date() + "\nThe error was: "
                    + e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private static void registerOutputs(String[] keys, CaseAssistService service,
            CaseOutput[] nonEmptyOutputs) {
        if (nonEmptyOutputs.length >= 1) {
            try {
                if (DEBUG)
                    System.out.println("Registering " + nonEmptyOutputs.length + " outputs");

                for (int i = 0; i < nonEmptyOutputs.length; i++) {
                    if (DEBUG)
                        System.out.println("Output: " + nonEmptyOutputs[i].getName() + ";"
                                + nonEmptyOutputs[i].getDatasetFile() + nonEmptyOutputs[i].getPattern() + ";"
                                + nonEmptyOutputs[i].getPath());
                }
                service.registerOutputs(nonEmptyOutputs, keys);
                
                if (DEBUG)
                    System.out.println("EMF command client registered " + nonEmptyOutputs.length
                            + " outputs successfully.");
            } catch (Exception e) {
                if (e.getMessage().startsWith("Error registering"))
                    System.out.println(e.getMessage());
                else
                    System.out.println("Error registering outputs: " + e.getMessage());
            }
        }
    }

    private static JobMessage[] getNonEmptyMsgs(JobMessage[] msgs, String[] keys, ArrayList keyArray) {
        ArrayList nonEmpty = new ArrayList();

        for (int i = 0; i < msgs.length; i++) {
            if (!msgs[i].isEmpty()) {
                nonEmpty.add(msgs[i]);
                keyArray.add(keys[i]);
            }
        }
        JobMessage[] nonEmptyMsgs = new JobMessage[nonEmpty.size()];
        nonEmpty.toArray(nonEmptyMsgs);

        return nonEmptyMsgs;
    }

    private static CaseOutput[] getNonEmptyOutputs(CaseOutput[] outputs, String[] keys, ArrayList keyArray) {
        List<CaseOutput> all = new ArrayList<CaseOutput>();

        for (int i = 0; i < outputs.length; i++) {
            if (!outputs[i].isEmpty()) {
                // only add the output if it's not already there
                CaseOutput nextOutput = outputs[i];

                if (!all.contains(nextOutput)) {
                    if (nextOutput.getDatasetType() == null || nextOutput.getDatasetType().trim().isEmpty()) {
                        System.out.println("ERROR: Dataset type is empty for output " + nextOutput.getName()
                                + ". Dataset name: " + nextOutput.getDatasetName() + ".");
                        exitValue = 4;
                    } else {
                        all.add(nextOutput);
                        keyArray.add(keys[i]);
                    }
                } else {
                    // if (DEBUG)
                    // System.out.println("Ignoring redundant output "+nextOutput.getName());
                }
            }
        }
        return all.toArray(new CaseOutput[0]);
    }

    private static void writeToLogger(List<String> args, String logFile, int logInterval, int resendTimes,
            String jobkey, JobMessage jobMsg, CaseOutput output) throws Exception {
        boolean logFileExisted = new File(logFile).exists();
        String logAsistFile = logFile + ".ast";

        if (DEBUG)
            System.out.println("EMF Command Client starts writing messages to log file: " + logFile + " " + new Date());
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
        writeLogs(writer, logAsistFile, jobMsg, jobkey, output, logFileExisted);
        writer.close();
        if (DEBUG)
            System.out.println("Finished writing messages to log file: " + logFile + " " + new Date());

        if (jobMsg.getStatus() == null || jobMsg.getStatus().isEmpty()) {
            sendLogs(args, logInterval, resendTimes, logFile, logAsistFile, jobkey, jobMsg, output, false);
        } else
            sendLogs(args, logInterval, resendTimes, logFile, logAsistFile, jobkey, jobMsg, output, true);
    }

    private static void writeLogs(PrintWriter writer, String logAsistFile, JobMessage jobMsg,
            String jobkey, CaseOutput output, boolean logFileExisted) throws Exception {
        String record = jobkey;
        record += "," + jobMsg.getExecName();
        record += "," + jobMsg.getExecPath();
        record += "," + jobMsg.getMessage();
        record += "," + jobMsg.getMessageType();
        record += "," + jobMsg.getStatus();
        record += "," + jobMsg.getPeriod();
        record += "," + jobMsg.getRemoteUser();
        record += "," + CustomDateFormat.format_MM_DD_YYYY_HH_mm(jobMsg.getExecModifiedDate());
        record += "," + new Date().getTime();
        record += "," + output.getDatasetFile();
        record += "," + output.getPath();
        record += "," + output.getPattern();
        record += "," + output.getDatasetType();
        record += "," + output.getDatasetName();
        record += "," + output.getName();

        if (!logFileExisted) {
            if (DEBUG)
                System.out.println("EMF Command Client starts writing messages to log assistance file: " + logAsistFile
                        + " " + new Date());

            writeInitialAsistFile(new File(logAsistFile));

            if (DEBUG)
                System.out.println("EMF Command Client starts writing messages to log assistance file: " + logAsistFile
                        + " " + new Date());

            writer.println("job key,exec name,exec path,message,"
                    + "message type,status,period,user,last mod date,log time(ms),"
                    + "output file,output dir,pattern,dataset type,dataset name,output name");
        }

        writer.println(record);
    }

    private static void writeInitialAsistFile(File file) throws Exception {
        if (!file.exists()) {
            PrintWriter asistWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            asistWriter.println("2"); // line number to start extract job messages and send
            asistWriter.close();
        }

        return;
    }

    private static CaseAssistService getService(List<String> args) throws Exception {
        String url = DEFAULT_URL;

        if (!args.get(0).startsWith("-"))
            url = args.get(0);

        if (serviceLocator == null)
            serviceLocator = new RemoteServiceLocator(url);

        return serviceLocator.caseAssistService();
    }

    private static long getTime(String line) {
        int timeIndex = line.lastIndexOf(",");
        String time = line.substring(timeIndex + 1);

        if (time != null || !time.trim().isEmpty())
            return Long.parseLong(time.trim());

        return 0;
    }

    private static String extractJobkey(String line) {
        return line.substring(0, line.indexOf(","));
    }

    private static JobMessage extractJobMsg(String line) throws Exception {
        CommaDelimitedTokenizer tokenizer = new CommaDelimitedTokenizer();
        String[] fields = tokenizer.tokens(line);

        JobMessage msg = new JobMessage();
        msg.setExecName(fields[1]);
        msg.setExecPath(fields[2]);
        msg.setMessage(fields[3]);
        msg.setMessageType(fields[4]);
        msg.setStatus(fields[5]);
        msg.setPeriod(fields[6]);
        msg.setRemoteUser(fields[7]);

        try {
            msg.setExecModifiedDate((fields[8] == null || fields[8].trim().isEmpty()) ? null : CustomDateFormat
                    .parse_MM_DD_YYYY_HH_mm(fields[8]));
            msg.setReceivedTime(new Date(Long.parseLong(fields[9])));
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }

        return msg;
    }

    private static CaseOutput extractOutput(String line) throws Exception {
        CommaDelimitedTokenizer tokenizer = new CommaDelimitedTokenizer();
        String[] fields = tokenizer.tokens(line);

        CaseOutput output = new CaseOutput();
        output.setDatasetFile(fields[10]);
        output.setPath(fields[11]);
        output.setPattern(fields[12]);
        output.setDatasetType(fields[13]);
        output.setDatasetName(fields[14]);
        output.setName(fields[15]);
        // output.setEmpty(empty);

        return output;
    }

    private static String createSafeName(String name) {
        if (name == null)
            return name;

        String safeName = name.trim();

        for (int i = 0; i < safeName.length(); i++) {
            if (!Character.isLetterOrDigit(safeName.charAt(i))) {
                safeName = safeName.replace(safeName.charAt(i), '_');
            }
        }

        return safeName;
    }
    
    private static void relocateFiles(List<String> args, String authKey, String singleFile, String fileFolder, String filePattern) throws Exception {
        try {
            if (DEBUG)
                System.out.println("EMF Command Client starts relocating external files on server at: " + new Date());

            CaseAssistService service = getService(args);
            System.out.println("Test assisant service on relocating external files: " + service.toString());

            if (DEBUG) {
                System.out.println("EMF command client relocated external files successfully.");
                System.out.println("EMF Command Client exited successfully on " + new Date());
            }
        } catch (Exception e) {
            System.out.println("EMF Command Client encountered a problem on " + new Date() + "\nThe error was: "
                    + e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

}
