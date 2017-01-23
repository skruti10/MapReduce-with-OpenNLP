/**
 * Created by kruti on 10/6/2016.
 * this project is the basic implementation of Hadoop Map Reduce Framework
 * the output is key,value pair of unique name identifeirs, file names => where 
 * file names are seperated by | - showing the unique name appeared in all these files.
 * if the same file name appeared multiple times => it shows the word has appeared more than once in the file
 */

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.io.InputStream;

public class ResPaperMapReduceApp extends Configured implements Tool {

    

    /* the main class to run the map reduce jobs*/
    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(new ResPaperMapReduceApp(), args);
        System.exit(res);
    }

    /* Run method - configuring all the parameters for the job being executed*/
    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Inptut and output paths not provided");
            System.exit(-1);
        }
        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        Configuration conf = getConf();
        //Job pdfJob = Job.getInstance(conf);
        Job pdfJob = new Job(conf, this.getClass().toString());

        /* input and output paths - provided while executing the hadoop jar*/
        FileInputFormat.setInputPaths(pdfJob, inputPath);
        FileOutputFormat.setOutputPath(pdfJob, outputPath);

        pdfJob.setJobName("ResPaperMapReduceApp");
        pdfJob.setJarByClass(ResPaperMapReduceApp.class);

        /* input and output format class - both being as a text files*/
        pdfJob.setInputFormatClass(TextInputFormat.class);
        pdfJob.setOutputFormatClass(TextOutputFormat.class);


        /* set the map output key and value - as String and IntWritable - accepting integer*/
        //job.setMapOutputKeyClass(Text.class);
        //job.setMapOutputValueClass(Tuple2.class);

        pdfJob.setOutputKeyClass(Text.class);
        pdfJob.setOutputValueClass(Text.class);

        /* set the mapper, reducer and intermediate combiner class for the job being executed*/
        pdfJob.setMapperClass(ResPaperMap.class);
        pdfJob.setCombinerClass(ResPaperReduce.class);
        pdfJob.setReducerClass(ResPaperReduce.class);

        /* if the output folder already exists - delete the folder to create a new*/
        FileSystem checkOutput = FileSystem.get(conf);
        if(checkOutput.exists(outputPath))
        {
            checkOutput.delete(outputPath,true);
        }

        //pdfOutputReader = new SequenceFile.Reader(checkOutput,outputPath,conf);

        return pdfJob.waitForCompletion(true) ? 0 : 1;
    }



    /* define the mapper class for the map- reduce implementation */

    public static class ResPaperMap extends Mapper<LongWritable, Text, Text, Text> {
        private final static IntWritable one = new IntWritable(1);

        private Text fileNameText = new Text();
        private Text word = new Text();


        @Override
        public void map(LongWritable key, Text value,
                        Mapper.Context context) throws IOException, InterruptedException {
            if (key != null &&  value != null)
            {
                String line = value.toString();
                
                /* get the file name from the context being passed. */
                FileSplit splitFile = (FileSplit) context.getInputSplit();
                String FileName = splitFile.getPath().getName();
                fileNameText = new Text(FileName);

            /* The below code uses the OPEN NLP - Tokenizer to get the tokens from the
            * file being read by the mapper.*/

                InputStream tokenIS = null; /* input stream for tokens*/
                String tokens[] = null;

                InputStream findNameIS = null; /* input stream for extracting names*/
                Span nameSpans[] = null;

                try {

                /* load the token model - these are already trained models
                * downloaded from the OPEN NLP*/
                    tokenIS = ResPaperMap.class.getResourceAsStream("/en-token.bin");

                /* load the model with trained model*/
                    TokenizerModel model = new TokenizerModel(tokenIS);

                /* define the tokenizer to obtain individual tokens from the plain text*/
                    Tokenizer nlptokenizer = new TokenizerME(model);

                    tokens = nlptokenizer.tokenize(line);

                /* now use the name finder model - this trained model helps in finding
                * unique names from the tokens */
                    findNameIS = ResPaperMap.class.getResourceAsStream("/en-ner-person.bin");

                /* load the the token name finder model with the trained model*/
                    TokenNameFinderModel namemodel = new TokenNameFinderModel(findNameIS);

                /* the name finder now loaded with the trained model will be used
                 * to find unique named entities - like authors name from the tokens
                 */
                    NameFinderME nameFinder = new NameFinderME(namemodel);

                    nameSpans = nameFinder.find(tokens);

                /* after getting the unique names from the name finder model -
                * parse the name finder to obtain the strings obtained*/
                    String[] getspans = Span.spansToStrings(nameSpans, tokens);

                /* these names - obtained from the name finder will now serve as the
                * key for writing the keys in mapper.*/
                    for (String eachString : getspans) {

                        word.set(eachString);
                        /* set the key value pair as unique name identified and the file name*/
                        context.write(word, fileNameText);
                    }
                } catch (Exception ex) {
                    System.out.println("Exception in Tokenizing: " + ex);
                } finally {
                    if (tokenIS != null) {
                        tokenIS.close();
                    }
                }
            }
        }
    }

    /* This is the basic reducer class - which just generates the output with key,values pair
    * showing how many times a name occurred in different files*/
    public static class ResPaperReduce extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            if (key != null && values != null) {
                StringBuilder fileNames = new StringBuilder();
                Text fileNameVal = new Text();
                IntWritable finalCount = new IntWritable();

                int nameCount = 0;

                for (Text value : values) {
                    fileNames.append(value.toString());
                    if(values.iterator().hasNext())
                        fileNames.append(("|"));
                }

                //finalTuple = new Tuple2<>(finalCount,fileNameVal);
            /* write the output to the context - key being the unique names,
            * value - files in which it appeared - multiple files seperated by |. */
                context.write(key, new Text(fileNames.toString()));
            }
        }
    }
}
