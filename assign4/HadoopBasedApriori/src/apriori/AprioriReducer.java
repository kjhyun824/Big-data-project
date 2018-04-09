package apriori;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import utils.AprioriUtils;

import java.io.IOException;
import java.util.Iterator;

/*
 * Reducer for all phases would collect the emitted itemId keys from all the mappers
 * and aggregate it to return the count for each itemId.
 */

public class AprioriReducer extends Reducer<Text, IntWritable, Text, IntWritable>
{
    public void reduce(Text itemSet, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        /** COMPLETE **/

        Double minSup = Double.parseDouble(context.getConfiguration().get("minSup"));
        Integer numTxns = context.getConfiguration().getInt("numTxns", 2);
        int itemCount = 0;
        Iterator it = values.iterator();
        while (it.hasNext()) {
            it.next();
            itemCount++;
        }

        if(AprioriUtils.hasMinSupport(minSup,numTxns,itemCount)) {
            IntWritable _itemCount = new IntWritable(itemCount);
            context.write(itemSet,_itemCount);
        }
    }
}
