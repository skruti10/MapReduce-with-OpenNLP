import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by kruti on 10/7/2016.
 *
 * This project is use to download research papers from an API.
 * The downloaded PDFs are stored in resources folder and then these PDFs are read by
 * Apache Tika Toolkit to extract plain text and write in new text files
 * The PDFs are deleted at the end and only text files are feeded to another project -
 * Hadoop Map reduce framework to generate unqiue name identifers along with the files
 * in which they have appeared.
 */
public class ResearchPaperParsing {
    private static final int BUFFER_SIZE = 4096;

    private static String DestinationFolder ="";

    public static void main(final String[] args) {

        String localDir = "";

        /** in order to create a new folder for the downloaded research papers
         * the logic is to get the current time from the system and append this with another random
         * number. The generated folder will be unique on the local file system.
         */
        Random rand = new Random();

        java.util.Date currDate = new java.util.Date();

        /** get the current time from the system in format hhmss - this will be used
         * in creating the folder name and pdf file names
         */
        DateFormat currFormat = new SimpleDateFormat("ddhhmmss");

        String firstRandom = currFormat.format(currDate);
        int secondRandom = rand.nextInt(100) + 1;

        /** this is the new folder name generated and will be stored in the local system */
        String newFolderName = "PDF"+firstRandom+secondRandom;
        System.out.println("new Folder name: " + newFolderName);

        String searchKeyword = "" ;
        StringBuilder buildSearch = null;
        long noOfPDFs = 2;

        /** take the inputs from the user for searching the feeds - specific reserach papers
         * which will have the search keywords in their content.
         * Also the user can mention the number of pdfs that needs to be downloaded.
         */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Scanner in = new Scanner(System.in);


        /** check if  any arguments are passed from any command line or methods **/
        try
        {
            if(args != null )
            {
                if(args.length == 2)
                {
                    searchKeyword = args[0];
                    noOfPDFs = Long.parseLong(args[1]);
                }
                else {
                    System.out.println("Enter the searchkeywords : ");
                    searchKeyword = br.readLine();

                    System.out.println("Enter the no of files to be downloaded : ");
                    noOfPDFs = in.nextLong();
                }
            }
            else
            {
                System.out.println("Enter the searchkeywords : ");
                searchKeyword = br.readLine();

                System.out.println("Enter the no of files to be downloaded : ");
                noOfPDFs = in.nextLong();
            }


            /* all the pdfs downloaded will be stored in the project resources folder **/
            localDir = Paths.get("src/main/resources").toAbsolutePath().normalize().toString() + File.separator+ newFolderName;

            System.out.println("lcoal dir :"+localDir);



            /** if no search keyword is provided by user - the default value is electron
             * if the key word has  space - replace by %20 to make URL defined and not broken**/
            if(searchKeyword.equals(""))
            {
                buildSearch = new StringBuilder("electron");
            }
            else if(searchKeyword.contains(" "))
            {
                String[] words = searchKeyword.split(" ");
                buildSearch = new StringBuilder(words[0]);

                for(String eachWord : words)
                {
                    buildSearch.append("%20");
                    buildSearch.append(eachWord);
                }

            }
            else
            {
                buildSearch = new StringBuilder(searchKeyword);
            }

            /** if no input for no of downloads is provided by user - the default value is 10 **/
            if(noOfPDFs == 0)
                noOfPDFs = 2;

            /** call the function to process the API and get the feeds of pdfs that have the search keyword **/
            searchPapers_Rome(localDir,buildSearch.toString(),firstRandom,noOfPDFs);

            /** below code is to process the pdfs generated by TIKA **/

            /** get all the files listing from the directory created for downloading the pdfs **/
            File[] files = new File(localDir).listFiles();

            for (File mainPDF : files) {
                if (mainPDF.isFile() && mainPDF.getName().toString().toLowerCase().contains("pdf"))
                {
                    /* call the extraction method to obtain plain text from the pdfs downloaded*/
                    extractPlainText(mainPDF.getAbsolutePath(),localDir);

                }
            }
            DeleteAllPDFs(localDir);

            //CopyFilesToServer(newFolderName);

        }
        catch(IOException ex)
        {
            System.out.println("IO exception : "+ex);
        }
        catch(FeedException ex)
        {
            System.out.println("Feed exception : "+ex);
        }
        catch(Exception ex){
            System.out.println("exception : "+ex);
        }
    }

    /* this function can be used in future to copy the files to the root server*/
    public static void CopyFilesToServer(String folder)
    {
        try {
            String path = "cmd /c start C:/ksharm22hw2/TestBatchToPutFile.bat " + folder;
            Runtime rn = Runtime.getRuntime();
            Process pr = rn.exec(path);
            pr.waitFor();
        }
        catch(Exception ex)
        {
            System.out.println("exception in running batch file to copy files from local to root");
        }

    }


    /* Use Tika Tool Parser to extract the text from the PDF in the form of Palin text
    * this plain text is again saved in another text file. Npw these text files will be
    * used by the map - reduce program to identify unique names*/
    public static int extractPlainText(String pdfPath,String localDir)
    {
        Metadata metadata = new Metadata();
        FileInputStream pdfInputStream = null;
        ParseContext pcontext = new ParseContext();
        BodyContentHandler handler = new BodyContentHandler(-1);

        /**parsing the document using PDF parser **/
        PDFParser pdfparser = new PDFParser();
        // AutoDetectParser parser = new AutoDetectParser();
        try
        {
            pdfInputStream = new FileInputStream(new File(pdfPath));
            pdfparser.parse(pdfInputStream, handler, metadata, pcontext);

            File fileName = new File(pdfPath);
            /* get the file name without extension - the substring gets the file name with .pdf removed
            * this will be used to create a text file*/
            String originalFileName = fileName.getName().substring(0, (fileName.getName().length() - 4));
            WriteToTextFile(handler.toString(),originalFileName,localDir);
        }
        catch (Exception ex)
        {
            System.out.println("Exception in extracting plain text :"+ex);
            return -1;
        }
        finally{
            try{
                /* close all the stream - since these pdfs will be deleted later*/
                if(pdfInputStream != null)
                {
                    pdfInputStream.close();
                }
            }
            catch (IOException ex)
            {
                System.out.println("IOexception in closing input stream for PDF: "+ex);
            }

        }
        return 1;
    }

    /* Once the plain text is extracted from the PDFs- this function
    * is called to generate text files for each of the pdfs*/
    public static void WriteToTextFile(String plainText,String FileName,String localDir)
    {
        StringBuilder newTextFile = new StringBuilder();
        newTextFile.append(localDir).append(File.separator).append(FileName).append(".txt");
        BufferedWriter textwriter = null;
        try
        {
            /* write to the text files*/
            textwriter = new BufferedWriter( new FileWriter( newTextFile.toString()));
            textwriter.write(plainText);

        }
        catch ( IOException e)
        {
            System.out.println("IOExcption in writing to text file "+e);
        }
        finally
        {
            /* close all the input and bufferred streams*/
            try
            {
                if ( textwriter != null)
                    textwriter.close( );
            }
            catch ( IOException e)
            {
                System.out.println("IOExcption in closing the stream "+e);
            }
        }

    }

    /* delete all the extra pdfs - once the corresponding text files are created.*/
    public static void DeleteAllPDFs(String localDir)
    {
        /** get all the files listing from the directory created for downloading the pdfs **/
        File[] files = new File(localDir).listFiles();

        for (File mainPDF : files)
        {
            if (mainPDF.isFile() && mainPDF.getName().toString().toLowerCase().contains("pdf"))
            {
                /*delete the pdf files downloaded - not needed since we have the text files*/
                mainPDF.delete();
            }
        }
    }

    /** this function calls the API to download the research papers - pdfs. The returned feed is an ATOM response
     * which is then processed to get the entries seperately and extract the location of the pdf. Since the location
     * obtained from these feeds does not reflects to actual PDF location - the location is again processed to
     * obtain the real location of PDF and download the file from that location
     */
    public static SyndFeed searchPapers_Rome(String localDir, String searchKeywords, String RandomString, long NoOfPDFs) throws MalformedURLException, IOException, IllegalArgumentException, FeedException
    {
        StringBuilder AtomFeed_API_URL = new StringBuilder();
        SyndFeed feed = null;
        InputStream is = null;

        URL linkUrl = null;
        HttpURLConnection httpConn = null;

        Random rand = new Random();
        int value = rand.nextInt(100) + 1;

        /**  create the new folder in local machine **/
        File saveDir = new File(localDir);
        if(!(saveDir.exists()))
        {
            saveDir.mkdir();
        }
        StringBuilder newFileName ;

        List<String> pdfLinks2 = new ArrayList<String>();
        //AtomFeed_API_URL.append("http://export.arxiv.org/api/query?search_query=all:").append(searchKeywords).append("&start=0&max_results=5&title=pdf");
        /** this is the link of the API feed which returns ATOM response. the search_query is filtered for matching the contents of the
         * pdf with the search keywords given by user.
         */
        AtomFeed_API_URL.append("http://export.arxiv.org/api/query?search_query=all:").append(searchKeywords).append("&start=0&max_results=").append(NoOfPDFs).append("&title=pdf");

        System.out.println("feed api url: "+AtomFeed_API_URL.toString());
        try
        {
            /* open the connection to the API feed url with parameters as built
             * then read the feed in SynFeed */
            URLConnection openConnection = new URL(AtomFeed_API_URL.toString()).openConnection();
            is = new URL(AtomFeed_API_URL.toString()).openConnection().getInputStream();
            if("gzip".equals(openConnection.getContentEncoding())){
                is = new GZIPInputStream(is);
            }
            InputSource source = new InputSource(is);
            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(source);


            /*Once the feed is obtained process the feed to obtain the url of the pdf in order
            * to download it to the local system*/
            if(feed != null)
            {
                if(feed.getEntries() != null && feed.getEntries().size() > 0)
                {
                    for(int i = 0 ; i < feed.getEntries().size() ; i++)
                    {
                        if(feed.getEntries().get(i) != null && feed.getEntries().get(i).getLinks() != null &&
                                feed.getEntries().get(i).getLinks().size() > 0)
                        {
                            /* the getlinks - is the main part of the feeds that provide the URL of the
                            * lcoation from where the PDF can be downloaded
                            * the type is matched - application/pdf -this shows the path obtained
                            * points for a pdf. the other two text/html can be discarded*/
                            for(int j = 0; j < feed.getEntries().get(i).getLinks().size() ; j++ )
                            {
                                SyndLink tempLink = feed.getEntries().get(i).getLinks().get(j);
                                if(tempLink != null && tempLink.getType() != null &&
                                        tempLink.getType().toLowerCase().equals("application/pdf"))
                                {
                                    /*all the pdf links are added in a temporary array list*/
                                    pdfLinks2.add(tempLink.getHref().toString());
                                }
                            }
                        }
                    }
                }

            }

            /** after obtaining all the links of pdf to be downnloaded - open each as stream and download in local **/
            if(pdfLinks2 != null && pdfLinks2.size() > 0)
            {
                for(int k = 0 ; k < pdfLinks2.size() ; k++)
                {
                    if(linkUrl != null){
                        linkUrl = null;
                    }
                    linkUrl = new URL(pdfLinks2.get(k).toString()); // example : "http://arxiv.org/pdf/cond-mat/0102536v1."

                    if(httpConn != null) {
                        httpConn = null;
                    }
                    httpConn = (HttpURLConnection) linkUrl.openConnection();

                    httpConn.setRequestMethod("GET");
                    httpConn.setRequestProperty("User-Agent", "  Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
                    String myCookies = "cookie_name_1=cookie_value_1;cokoie_name_2=cookie_value_2";
                    httpConn.setRequestProperty("Cookie", myCookies);

                    System.out.println("head request for link :"+pdfLinks2.get(k).toString());

                    /* the links obtained from the feeds do not actually point to
                    * pdf, so on processing the header we can find the actual location
                    * of the pdf. The actual link needs to be used and read to download the pdf
                    * Reading this location only generates HTML data and hence writing this html
                    * data to pdf generates only corrupted pdf*/

                    for (Map.Entry<String, List<String>> header : httpConn.getHeaderFields()
                            .entrySet())
                    {
                        if(header.getKey() != null && header.getKey().equals("Location"))
                        {
                            //System.out.println(header.getKey() + "=" + header.getValue());
                            newFileName = new StringBuilder();
                            newFileName.append(localDir).append(File.separator).append("RP").append(RandomString).append(rand.nextInt(100) + 1).append(".pdf");
                            //System.out.println("New File Name: "+newFileName.toString());
                            /* call the download pdf function - this will actually read the
                               contents of the page and write to generate a PDF
                            * */
                            if(header.getValue() != null && header.getValue().size() > 0)
                                downloadPDF(header.getValue().get(0),newFileName.toString());
                        }

                    }
                }
            }
        } catch (Exception e){
            System.out.println("Exception occured when building the feed object out of the url"+e);
        } finally
        {
            if( is != null)
                is.close();
        }
        return feed;
    }

    /* This function receives a single URL at a time and writes a new pdf from the contents of the page*/
    public static int downloadPDF(String pdfURL, String saveDir)
            throws IOException {

        try {
            URL url = new URL(pdfURL);
            HttpURLConnection httpConnPDF = (HttpURLConnection) url.openConnection();
            int responseCode = httpConnPDF.getResponseCode();

            /**always check HTTP response code first **/
            if (responseCode == HttpURLConnection.HTTP_OK) {

                String disposition = httpConnPDF.getHeaderField("Content-Disposition");
                String contentType = httpConnPDF.getContentType();
                int contentLength = httpConnPDF.getContentLength();


                /** opens input stream from the HTTP connection**/
                InputStream inputStream = httpConnPDF.getInputStream();

                /** opens an output stream to save into file**/
                FileOutputStream outputStream = new FileOutputStream(saveDir);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                /** Write the file from the content read from the location opened using HttpConnection**/
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                /** close all the streams **/
                outputStream.close();
                inputStream.close();

                System.out.println("Downloaded file saved in path: " + saveDir);
            } else {
                System.out.println("No file found. Error with HTTP request: " + responseCode);
            }
            httpConnPDF.disconnect();
        }
        catch(Exception ex)
        {
            System.out.println("Exception in download file :" +ex);
            return -1;
        }
        return 1;
    }
}
