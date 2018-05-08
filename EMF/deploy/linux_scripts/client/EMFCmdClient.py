#!/usr/bin/env python

##--------------------------------------------------------------##
## EMFCmdClient:
##
## EMF command line client. Writes messages to a preset log file.  Can
## also call a Java EMF client to send messages periodically to the
## EMF server
##
## Note: Most users would want to set the following variables in their
##       wrapper script that call this python script:
##         EMF_LOGGERPYTHONDIR - directory to write log files
##         EMF_JOBNAME - the EMF job name (no spaces)
##         CASE - the EMF case
##         EMF_JAVACMDLINE -  full EMF Java Cmd line client call (optional)
##         EMF_MSGTIME - how long to wait b/w sending messages in sec (optional)
##
## Note:  For some reason, the message needs to be a string, NOT
##        an environmanetal variable, eg.:
##    the following works:
##    $ EMFCmdClient.py -k $JOBKEY -m "running smkinven -- $emf_period"
##    the following doesn't works:
##    $ set emf_msg = "running smkinven -- $emf_period"
##    $ EMFCmdClient.py -k $JOBKEY -m  $emf_msg
##    instead do the following:
##    $ EMFCmdClient.py -k $JOBKEY -m  "$emf_msg"
##
##
##  11/14/2007  -- Alexis Zubrow  IE UNC
##--------------------------------------------------------------##

from optparse import OptionParser
from shutil import copy
import os, time, signal

## define a function that parses ps
def testprocess(pid):
    ## tests a process and returns if it still in /proc

    ## use ps, returns 0 if exists, 1 if doesn't
    progStr = "ps -p %d > /dev/null" %pid
    status = os.system(progStr)
    if status == 0:
        return True
    else:
        return False
    

##-----------------------------------------------##

if __name__ =="__main__":

    ## Command line options
    usage = "usage: %prog [options] "
    parser = OptionParser(usage=usage)

    parser.add_option("-k", metavar=" jobKey", dest="jobKey", default="0",
                      help="Job key code.  Required.")

    parser.add_option("-x", metavar=" execPath", dest="execPath", default="",
                      help="full path to script or executable")
    parser.add_option("-p", metavar=" period", dest="period", default="",
                      help="EMF period")
    parser.add_option("-m", metavar=" message", dest="message", default="",
                      help="log message")
    parser.add_option("-s", metavar=" status", dest="status", default=None,
                      help="Status, acceptable values: 'Submitted', 'Running', 'Failed', or 'Completed'.")
    parser.add_option("-t", metavar=" msg_type", dest="msg_type", default="i",
                      help="Message type: i (info), e (error), w (warning)")
    parser.add_option("-F", metavar=" outFile", dest="outFile", default="",
                      help="Single output file to register")
    parser.add_option("-D", metavar=" outDir", dest="outDir", default="",
                      help="Directory of output files.  Usually paired with -P.")
    parser.add_option("-P", metavar=" outPattern", dest="outPattern", default="",
                      help="Pattern to match in output directory.  Example '*.txt' "
                      "will match all output files with the suffix '.txt' in the "
                      "output directory")
    parser.add_option("-T", metavar=" outType", dest="outType", default="",
                      help="Dataset type of the output datasets. Must be included "
                      "if your registering output.")
    parser.add_option("-N", metavar=" dsName", dest="dsName", default="",
                      help="Dataset name of output datasets.  Default is '', "
                      "i.e. autogenerate dataset name.")
    parser.add_option("-O", metavar=" outName", dest="outName", default="",
                      help="Output name.  Default is '', i.e. autogenerate name "
                      "to be the same as the dataset name.")
    parser.add_option("--debug", dest="debug", default=True, action="store_true",
                      help="Turn debugging on.")


    (options, args) = parser.parse_args()

    ## get options
    jobKey=options.jobKey
    message=options.message
    status=options.status
    period = options.period
    execPath=options.execPath
    msg_type = options.msg_type
    debug = options.debug
    outFile = options.outFile
    outDir = options.outDir
    outPattern = options.outPattern
    outType = options.outType
    dsName = options.dsName
    outName = options.outName

    
    ## ---------------------------------------------------##
    if debug: print "EMF cmd client: python client starting:  %s" %time.asctime()

    ## test status
    statusLst = [None,"Submitted", "Running", "Failed", "Completed"]
    if status not in statusLst: 
        print "EMF cmd client: status= %s not recognized" %status
        raise LookupError, "status (%s) must be one of: %s" %(status,statusLst)

    ## Construct log file based on environmental variables
    try:
        logDir = os.path.expanduser(os.environ["EMF_LOGGERPYTHONDIR"])
    except:
        logDir = "/tmp"
    try:
        jobName = os.environ["EMF_JOBNAME"]
    except:
        jobName = "jobTest"
    try:
        case = os.environ["CASE"]
    except:
        case = "caseTest"
    try:
        msgTime = int(os.environ["EMF_MSGTIME"])
    except:
        msgTime = 120

    ## Create a log file and a date file, also create one for the Java client
##    logFile = jobName + "_" + case + "_" + jobKey
    ## We are not using the case abbreviation b.c. in some scripts this changes
    ## if we did want this identifier, we could set EMF_CASE to CASE in the top, EMF script
    ## and use the EMF_CASE environment variable
    logFile = jobName + "_" + jobKey
    logFileJava = logFile + "_java.csv"
    logFile = logFile + ".csv"
    logFile = os.path.join(logDir, logFile)
    dateFile = logFile + ".date"
    logFileJava = "/tmp/" + logFileJava

    ## get user
    user = os.getenv("USER")

    ## See if Java Cmd Client is setup
    useJava = False
    try:
        javaCmd = os.environ["EMF_JAVACMDLINE"]
        useJava = True
    except:
        javaCmd = None


    ## Check if log dir (where csv file is written) exists,
    ## if not create it and make it rwx for owner and group
    if not os.path.isdir(logDir):
        os.mkdir(logDir)
        os.chmod(logDir,0777)
        
        
    ## test if first time through, ie. the log file exists
    firstTime = True
    if os.path.isfile(logFile):
        firstTime = False ## if this file exists, not the first time

        ## append to already existing file open the date file for reading
        f = open(logFile, "a")
        g = open(dateFile,"r")

        ## get the date of last message sent
        buff = g.readline()
        buffLst = buff.split(",")
        lastmsgDate = float(buffLst[0])
        nextLine = int(buffLst[1])
        g.close()

    else:
        ## open new log file
        f = open(logFile, "w")
        ## write header
        f.write("job key,exec name,exec path,message,message type,status,period,user,last mod date,log time(ms),output file,output dir,pattern,dataset type,dataset name,output name\n")

        ## initialize message date to present and line to send to 2 (first message/output)
        lastmsgDate = time.time()
        nextLine = 2

        ## write to date file
        g = open(dateFile, "w")
        g.write("%.1f, %d\n" %(lastmsgDate, nextLine))
        g.close()



    ## for all messages write to log file:
    ##   status msg type, message, period, execpathj, execdate, timestamp
    execDate = ""
    if execPath != "":
        ## get the executable time stamp
        execDate = time.strftime("%m/%d/%Y %H:%M", \
                                 time.localtime(os.path.getmtime(execPath)))
    ## get path and name portion of executable
    execName = os.path.basename(execPath)
    execDir = os.path.dirname(execPath)

    ## Make sure you have something for status, at least ""
    if status is None:
        statusStr = ""
    else:
        statusStr = status
    ## time stamp in milliseconds
    timeStamp = int(time.time()*1000)

    ## if registering outputs, check to see if you have a message as
    ## well.  If not, set the message type to ''
    if (status is None) and (message == ""):
        msg_type = ""

    ## add quotes to message, outType, and outName
    if (message != ""):
        message = "'" + message + "'"
    if (outType != ""):
        outType = "'" + outType + "'"
    if (outName != ""):
        outName = "'" + outName + "'"
        
    ## write everything to log
    f.write("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n" \
            %(jobKey,execName, execDir,message,msg_type,statusStr,period,
              user,execDate,timeStamp,outFile,outDir,outPattern,outType,\
              dsName,outName))
    f.close()
    if debug: print "EMF cmd client: Python wrote csv file: %s" %time.asctime() 

    ## send messages through Java if get a status message
    ## or if the time elapsed since last send is > msgTime
    sendMsg = False
    if useJava:
        if status is not None:
            sendMsg = True
        if (time.time() - lastmsgDate > msgTime):
            sendMsg = True

    if sendMsg:
        ## also send it to the EMF server

        ## copy the logFile to a file for Java client
        if debug: print "EMF cmd client: Python copying csv to java csv file: %s" %time.asctime() 
        copy(logFile,logFileJava)
        if debug: print "EMF cmd client: Python copied csv to java csv file: %s" %time.asctime() 
        time.sleep(1)

        ## call java client w/ input from log file, in-line process
        javaCmd = javaCmd + " -k %s -n %d -f %s" %(jobKey, nextLine, logFileJava)
        if debug: print "EMF cmd client: Python calling java Cmd client -- sent status info to EMF server: %s" %time.asctime() 
        javaStatus = os.system(javaCmd)
        if debug: print "EMF cmd client: Python finished calling java Cmd client -- sent status info to EMF server: %s" %time.asctime() 
        if (javaStatus != 0):
            raise OSError, "Error sending message(s) through EMF Java command client"

        ## Calculate the number of lines in the csv file,
        ## the next time you send, it should be the next line in csv file
        f = open(logFile)
        buffLst = f.readlines()
        nextLine = len(buffLst) + 1
        f.close()

        ## write the date as seconds from epoch to the date file
        g = open(dateFile, "w")
        g.write("%.1f, %d\n" %(time.time(), nextLine))
        g.close()

    else:
        if debug: print "EMF cmd client: Not sending status info to EMF server: %s" %time.asctime()

    if debug: print "EMF cmd client: Python client done %s" %time.asctime()
