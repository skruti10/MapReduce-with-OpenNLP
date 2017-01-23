import com.rometools.rome.feed.synd.SyndFeed;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Created by kruti on 10/7/2016.
 *
 *   /* Pre Condition for these test cases to tun - THe batch file TestBatch.bat must be executed before
 *   executing any below test cases.
 */
public class ResearchPaperParsingTest {
    /* this test - main() - is the integration test - testing the entire fucntionality**/
    @Test
    public void main() throws Exception {

        String[] args = {"molecules","2"};
        ResearchPaperParsing.main(args);
    }

    /* test the extraction of plain text*/
    @Test
    public void extractPlainText() throws Exception {

        String pdfPath = "C:\\ksharm22HW2\\Test\\RP0701145814.pdf";
        String localDir = "C:\\ksharm22HW2\\Test";

        int ret = ResearchPaperParsing.extractPlainText(pdfPath,localDir);

        assert (ret == 1);
    }

    /* call the feeds api to check the files are downloaded in the system or not*/
    @Test
    public void searchPapers_Rome() throws Exception {

        StringBuilder buildSearch = null;
        String localDir = "C:\\ksharm22HW2\\Test";
        String searchKeyword = "electrons atoms molecules";

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

        Random rand = new Random();
        java.util.Date currDate = new java.util.Date();
        DateFormat currFormat = new SimpleDateFormat("ddhhmmss");
        String firstRandom = currFormat.format(currDate);
        long NoOfPdfs = 2;

        SyndFeed feed = ResearchPaperParsing.searchPapers_Rome(localDir,buildSearch.toString(),firstRandom,NoOfPdfs);
        if(feed != null)
            assert (true);
    }

    /* the test case tests the downloading of PDF - the API feed is provided.*/
    @Test
    public void downloadPDF() throws Exception {

        String PDFURL = "https://arxiv.org/pdf/cond-mat/0102536v1";
        String saveDir = "C:\\ksharm22HW2\\Test\\NewFile1.pdf"; //example : "C:\NewFile1.pdf"
        /*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the location where you want to save the PDF : ");
        saveDir = br.readLine();*/

        int ret = ResearchPaperParsing.downloadPDF(PDFURL,saveDir);
        assert (ret == 1);

    }


    /* This is the main integration test - where the batch file calls the server to process the files
    * and run the Map Reduce framework on the files present by default in root*/
    @Test
    public void FullTest() throws  Exception {
        //ResearchPaperParsing.CopyFilesToServer("PDF0809585855");

        String path = "cmd /c start C:/ksharm22hw2/TestBatch.bat ";
        Runtime rn = Runtime.getRuntime();
        Process pr = rn.exec(path);
        pr.waitFor();
    }

}