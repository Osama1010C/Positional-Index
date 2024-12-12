import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PositionalIndexReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        HashMap<String, ArrayList<Integer>> docPositions = new HashMap<>();

        for (Text value : values) {
            String[] docPos = value.toString().split(":");
            String docID = docPos[0];
            int position = Integer.parseInt(docPos[1]);
            
            if (!docPositions.containsKey(docID)) {
                docPositions.put(docID, new ArrayList<Integer>());
            }
            docPositions.get(docID).add(position);
        }

        StringBuilder result = new StringBuilder();
        for (String docID : docPositions.keySet()) {
            result.append(docID).append(":");
            ArrayList<Integer> positions = docPositions.get(docID);
            for (int i = 0; i < positions.size(); i++) {
                result.append(positions.get(i));
                if (i < positions.size() - 1) result.append(",");
            }
            result.append("; ");
        }

        context.write(key, new Text(result.toString().trim()));
    }
}


