import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class PositionalIndexMapper extends Mapper<LongWritable, Text, Text, Text> { 
    
	@Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        
		String line = value.toString();
        String[] words = line.split("\\s+"); 
        String fileName = ((FileSplit) context.getInputSplit()).getPath().getName(); 
        
        for (int i = 0; i < words.length; i++) {
            String term = words[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase(); 
            if (!term.isEmpty()) {
                context.write(new Text(term), new Text(fileName + ":" + i));
            }
        }
    }
}


