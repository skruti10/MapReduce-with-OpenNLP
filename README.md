# README #

This ReadMe is a step by step guide to download and run this application on your local machine.

### What is this repository for? ###

* The project focusses on implementing Hadoop Map Reduce framework to generate unique name identifers appearing in a set of text files. The text files are the research papers that are downloaded using API feeds from (https://arxiv.org/). The project is divided into two sections - the first project is responsible to download the PDFs from the API feed, extract the plain text from these PDFs and write into a normal text files. These text files will then be used by the Hadoop Map Reduce framework to extract unique identifiers. For the plain text extraction - Apache Tika Toolkit is used and for extracting unique name identifiers - OpenNLP Tokenizer and Name Finder are used.

* Version : 1.0


### How do I get set up? ###

* Summary of set up

In order to set up this project in your machine, it is assumed to be a Windows machine. Start by cloning the project from the Repository into your local system in a folder : **C:\ksharm22hw2** - inside this folder all the folders from the repository must be cloned. VMware Workstation and Horton Sandbox must be installed and running in your system. Please refer : http://hortonworks.com/products/sandbox/ for installation in VMWare. It is assumed that you are connected to your VM and the connection is already established to the server. The URL **"192.168.92.128"** - is the URL for the windows machine on which this project was developed. You must change this URL to run any scripts with your own URL. The project contains text command files and batch files is responsible for executing these command text files. Once the URL is replaced in the batch files - then only any batch file must be used to run the commands, if you do not wish to change the batch files, please copy the commands from the command text files and then run it on the server or the commands are given in this file itself, please refer to the steps below.

Important for the batch file executions : In order to run any batch files - please make sure to change the URL from **"192.168.92.128"** to the url as per your VM. The user name is mentioned as root and password for logging to Port 2222 is cloud441. You must provide your username and password for connecting to your server. Any failures in running the code can be because of the URL being not changed. 

Change the location where your Putty (containing putty.exe and pscp.exe ) are present on your local system. It is assumed - **C:\Program Files (x86)\Putty** - this contains putty.exe and pscp.exe. If your folder structure is different, make sure to change this path before executing batch files. All the commands are mentioned in the text files - which are also mentioned below and are used for executing the map reduce project.

There are three main folders cloned when the repository is cloned :

@**Test** => this contains data for unit and integration testing. It has a PDF file and two text files that will be used for performing unit and integration tests.

@**MapReduceParse** => this contains the project for Hadoop Map Reduce implementation. In order to generate key,value pairs for the Mapper - the key is the unique name identifiers that are extracted from the plain text files using OpenNLP. First the plain text is converted into strings of tokens using Tokenizer and then the Tokens are passed to the Name Finder which helps in extracting unique names. Two pre-trained models are used (en-ner-person.bin and en-token.bin) for exracting unique names from the plain text.

@**ResPaperDownloadParse** => this contains the project for downloading research paper from an API and then extract plain text from the downloaded PDF and save it in text files. The plain text extraction is performed using Apache Tika Toolkit PDFParser. So the input to the map reduce program is the plain text obtained from these sharded pdfs. The project has a folder **"PDF0809585855"** - this has some sample files and they are small in size which can be used for testing purpose.

Other Files:

@**BatchToPutFile.bat** => this helps in copying the files from local system to the root server. This accepts an argument - FolderName from your local system which has the latest PDFs generated.

@**PSCPCopyFilesRoot.bat** => this batch file is internally called by BatchToPutFile.bat - which iterates in a folder to fetch all text files and copy to root server.

@**PSCPCopyJar.bat** => this batch file is to copy the MapReduceParse.jar to the root server.

@**PuttyCmdsCopyFiles.txt AND PuttyCopyTxts.bat ** => the batch file calls the commands in text file conencting to the server (ssh) => to copy the files from root server into the user/khadoop/khw2/kinput

@**PuttyCmdsDeleteFiles.txt AND PuttyDeleteFilesRoot.bat ** => the batch file calls the commands in text file connecting to the server (ssh) to remove all the jars, txt files and remove the entire directory of the user being created in processing the data. This should be run with caution - used only at the end of the project when the data and the directories are no more needed.

@**PuttyCmdsDirs.txt AND PuttyServerDirs.bat ** => the batch file executes the commands in text file conencting to the server (ssh) to create user specific directory for performing any operations. All the input text files and output files will be saved in the directory created by this batch file.

@**PuttyMRProgramRun.bat AND PuttyRunMRProgram.txt ** => the batch executes the commands in text file conencting to the server (ssh) to run the map reduce framework. This is generally avoided and its better to use the command itself in SSH directly.


* Configuration

1. Windows 7 and up with minimum 8 GB RAM and 40 GB harddisk (minimum requirements for Horton Sandbox to run on your system)
2. Java 1.7 with Java Home and Path variables set as required.

* Dependencies

1. VMWare Workstation
2. Horton Sandbox
3. Putty.exe and PSCP.exe - present in C:\Program Files (x86)\Putty

* How to run tests

1. **Project : ResPaperDownloadParse**

In order to run this project - run the batch file **ResPaperDownloadParseBatch.bat** - this will ask for search key words for searching APIs and the number of files that needs to be downloade. Provide the search keywords (ex : "electrons" or "metabolic activity" or "atoms molecules" ) and the number of PDFs to be downloaded. If no search keyword is provided - default search is "electron" and default number of pdf (if given 0 by user) is 2. The search is based on the searching the contents with the provided search keywords.

**The API feed fetches only PDF related feeds from the API**, thus using this feed only PDFs are downloaded. In order to check the downloaded text files - you can check the folder - resources folder. The folder will have initials as PDF###### - the latest folder shows the latest downloaded files. **Please copy the folder name - this will be further used to transfer the files to the server** - used as a parameter in running the batch file. The folder contains only text files as we require only text files to be processed by our Hadoop framework and all PDFs are deleted.

The PDFs downloaded are used to generate text files. Apache Tika Toolkit is used to extract the plain text from PDF files and then the extracted plain text is saved in a text files. Only these text files will now be used by the Map Reduce framework to generate unique identifiers. The files are still maintained - i.e. for each PDF - a single text file is made with its plain text. These sharded PDF files as text files serves as input for the Map Reduce project.

2. **Project : MapReduceParse**

In order to run this project, please follow the below steps:

1. Start the VM machine to connect to the Horton Sandbox server. The default login is root/hadoop. Once this VM is connected, we can SSH (using Putty) into **root@192.168.92.128**
Note: the URL may be different for you, SSH using the URL that you see in your VMWare. SSH root@192.168.92.128 with Port 2222 - if this is done for the first time - it will ask to change the password.

2. In order to make sure that your root user does not have any other txt files and jar files, run the following commands in root@sandbox (ssh root)

ls

rm -rf *.txt

rm -rf *.jar


Please make sure to change the URL in batch file before running any batch files.

Now run : **PSCPCopyJar.bat**  - this will copy the **MapReduceParse.jar** to the root in Horton Sandbox server. 

**For manual run** : please use the below commands

Open Command Prompt and type the following commands: 

cd/

cd Program Files (x86)\Putty

pscp -P 2222 C:\ksharm22hw2\MapReduceParse.jar root@192.168.92.128:.

Check the files in root :

ls => this will display the files copied in the root server - this should contain the MapReduceParse.jar.


3. Now copy the text files downloaded in local system to the server. Open Command prompt and type : 

cd/

cd C:\ksharm22hw2

BatchToPutFile **PDF071152061**  -- please replace the folder name with the name generated in your system, i.e. :

The above command accepts the folder name PDF###### - that is generated on running Project : **ResPaperDownloadParse** - this folder will be present in your system - project - resources. For Example : C:\ksharm22hw2\ResPaperDownloadParse\src\main\resources\PDF071152061

Copy the folder name and run the batch file in command prompt to copy the text files from this folder to the Horton Sandbox root server. It will prompt for password for root, please enter the password as set by you for your URL <192.168.92.128> and Port 2222

**For manual run** : please run the following commands:

Open Command Prompt - > 

cd/

cd C:\Program Files (x86)\Putty

pscp -P 2222 **C:\ksharm22hw2\ResPaperDownloadParse\src\main\resources\PDF#####\RP######.txt** root@192.168.92.128:.


The bold path in the above shows the path that must reflect to the path of the files as downloaded in your local system. Please replace the bold section (C:...) with the path of the latest files as downloaded in your local machine. This will transfer one file at a time to the root server.

Verify the files present on your server, in command prompt write the following command :

ls (on ssh - check the root directory)

or run the below commands in command prompt:

cd/

cd C:\Program Files (x86)\Putty

pscp -P 2222 -ls root@192.168.92.128:.

Now **(Putty) SSH root@192.168.92.128 Port : 2222**, username: root / password as set by you

Once connected, either run the batch file : **PuttyServerDirs.bat** or write the below commands to setup the directory to run the sample files. All the below commands are present in **PuttyCmdsDirs.txt** also

su hdfs

hdfs dfs -chmod 777 /user

hdfs dfs -mkdir /user/khadoop

hdfs dfs -mkdir /user/khadoop/khw2

hdfs dfs -mkdir /user/khadoop/khw2/kinput

hdfs dfs -mkdir /user/khadoop/khw2/koutput

Similarly transfer all text files into kinput folde either by executing the **PuttyCopyTxts.bat** or execute the command present in file **PuttyCmdsCopyFiles.txt** also or you can write the commands as mentioned below. You can also upload the file from the UI. Once the directory kinput is created - you can see the UploadFile button - this allows you to upload the file directly to the server.

su root (provide password)

ls (check which txt files are present.)

hdfs dfs -put *.txt /user/khadoop/khw2/kinput

** Note : it is assumed to transfer all txt files from the root to the kinput folder - assuming it has the required text files by user **

Verify the uploaded files :

su hdfs

hdfs dfs -ls /user/khadoop/khw2/kinput

This should list all the text files as uploaded by you in folder kinput.

Now run the below commands to run the Map-Reduce program on your input folder: **Make sure to run using user as root.**

su root (provide password)

**hadoop jar MapReduceParse.jar /user/khadoop/khw2/kinput /user/khadoop/khw2/koutput**

Above command is also listed in **PuttyRunMRProgram.txt** and can be executed by running the batch file : **PuttyMRProgramRun.bat**

**Note: koutput if present is deleted and again created to contain the latest output**.

Once the execution is complete, the output file is produced in /user/khadoop/khw2/koutput/part-r-00000 - open this to check (command is provided below) the generated unique name identifiers along with the file names in which they have appeared. The file names are seperated by |. If a file name appears more than once, this means the name appeared more than once in that file.

**Use the following commands to check the contents of the file. In SSH root@192.168.92.128 2222 , write the below commands : **

**hdfs dfs -ls /user/khadoop/khw2/koutput** => this will display two files - SUCCESS and other starting as : part-r-00000 and now run the below command:

Run the below command to check the output generated by Map-Reduce project.

**hadoop fs -text /user/khadoop/khw2/koutput/part-r-00000**

* Deployment instructions:

1. Make sure to have the correct folder structure while cloning the project (repository cloned under C:\ksharm22hw2 - this is case sensitive, make sure to give everything in lower case)

2. If there is issue in running the Project for downloading research paper - open the project in IntelliJ or Eclipse and run the project from there.

3. URL for connecting to Horton Sandbox server : 192.168.92.128 is different for you (can be checked with ifconfig in your VM) - make sure to replace this URL in all the batch files - if the execution is required from the batch file. Else you can directly access the commands for execution.

4. Make sure to have 8 GB RAM on your system. If the system has only 8 GB RAM the project may take time to execute and produce output.

5. The VM must be connected before executing any project.

6. Putty.exe and Pscp.exe are assumed in a folder C:\Program Files (x86)\Putty - if this is different for your system, please make sure to update the batch files.


### Contribution guidelines ###

* Writing tests

**IMPORTANT NOTE** : Before running the unit and integration test for the project - make sure to run **TestBatchToPutFile.bat** => without this the integration test (FullTest) will fail.

For Automated testing - open the project (ResPaperDownloadParse) in any IDE (IntelliJ - preferred) - run **ResearchPaperParsingTest.java**. There are 5 test cases that run to test the following functionality:

1. extractPlainText - this test case tests the extraction of plain text from the Apache Tika Tool. The new text file is saved under C:\ksharm22hw2\Test

2. searchPapers_Rome - this test case tests the API feed that fetches the PDFs of research papers to be downloaded. By default search keyword is : "electrons atoms molecules" and no of pdfs = 2, the parameters can be changed to test downloading from different feeds with different keywords (**assuming that the keywords are only words and no special characters**).

3. downloadPDF - this test case tests the downloading of PDF. The feed is already provided and the new PDF is downloaded under C:\ksharm22hw2\Test - with file name NewFile1.pdf

4. main - this tests the entire flow of the project - ResPaperDownloadParse => from search papers - to download - to extract to plain text - delete pdfs - and generate text files.

5. FullTest - this is the integration test - which tests the entire flow for a test data. This calls the map reduce framework to process and generate key value pair for the files already present in Test folder. This test may take time - so please do no close the server window. The output is generated and can be checked on server b executing command => ** hadoop fs -text /user/ktest/koutput/part-r-00000 **. 

Finally in order to delete all the folders from the root, please execute **"PuttyDeleteFilesRoot.bat"** - this will delete the MapReduceParse.jar, all the text files and the users directories created for processing the inputs and storing the output. Run this batch file with caution as all output and all directories are removed.


* Other guidelines

1. For big files, the program may take time to produce the output. Do not close the connection unless the job is finished.

2. The Project for downloading Research Papers has signed jars thus creating an independent jar does not works and hence the jar needs to be executed along with dependencies in the artifacts folder itself.

3. The jar for MapReduceParse.jar is copied outside into the folder ksharm22hw2 - directly. If any changes are made to the project make sure to replace this jar with your latest artifact jar from your out folder before running the hadoop program.

4. If the project needs to be run through scripts => please change the URL **192.168.92.128** and username/password in all the batch files. Without changing the URL - the batch files cannot execute and putty.exe and pscp.exe are assumed in folder C:\ Program Files (x86)\Putty - make sure to change these as per your location and then follow the below sequence of batch file execution:

Step 1 : **ResPaperDownloadParseBatch.bat** - download the papers in your local machine. Please copy the folder name generated in your machine.(PDF##### - present in ResPaperDownloadParse -> src - > main -> resources -> the latest folder generated here )

Step 2 : **PuttyDeleteFilesRoot.bat** - this will delete any jars present in your root server and any text files present in your root server. Execute if required.

Step 3 : Open command prompt to Run : **BatchToPutFile.bat PDF######** - provide the folder name as generated from first project.

Step 4 : Run **PSCPCopyJar.bat** -> this will copy the JAR for executing the Map-Reduce Framework.

Step 5 : Run **PuttyServerDirs.bat** -> this will create the necessary folders required to run the map reduce program

Step 6 : Run **PuttyCopyTxts.bat** -> this will copy all the txt files present in your root to the directory /user/khadoop/khw2/kinput

Step 7 : Run the command -> **hadoop jar MapReduceParse.jar /user/khadoop/khw2/kinput /user/khadoop/khw2/koutput** in your server (SSH) to run the map reduce program.

Step 8 : Run the command -> ** hadoop fs -text /user/khadoop/khw2/koutput/part-r-00000 ** in your server (SSH) to get the output from the file - shows the key value pair as unique name identified in all files. The file names are seperated from "|" - if a file name appears multiple times, this means the same name has appeared more than once in that file.

All the references used for this project are mentioned in References.docx.

### Who do I talk to? ###

* Kruti Sharma | Mark Grechanik | Jyoti Arora