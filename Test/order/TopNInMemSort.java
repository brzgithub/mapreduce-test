package com.qf.MR.Test.order;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 前N个
 */
public class TopNInMemSort extends ToolRunner implements Tool{
    static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
        }

        private static Text k = new Text();
        private static Text v = new Text("1");

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();

            String[] words = line.split(" ");

            for (String word : words) {
                k.set(word);
                context.write(k,v);
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
        }
    }

    static class MyReducer extends Reducer<Text,Text,Text,Text>{
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
        }

        private static List<String> list =  new ArrayList<String>();

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int count = 0;

            Iterator<Text> it = values.iterator();

            while (it.hasNext()){
                Text next = it.next();

                count += Integer.parseInt(next.toString());
            }

            list.add(key.toString() + "_" + count);
        }

        private static Text k = new Text();
        private static Text v = new Text();

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            //冒泡排序
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (Integer.parseInt(list.get(i).split("_")[1]) < Integer.parseInt(list.get(j).split("_")[1])){
                        String tmp = list.get(i);

                        list.set(i,list.get(j));
                        list.set(j,tmp);
                    }
                }
            }

            //循环获取前3个值
            for (int i = 0; i < 3; i++) {
                k.set(list.get(i).split("_")[0]);
                v.set(list.get(i).split("_")[1]);
                context.write(k,v);
            }
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        setConf(conf);

        //2、创建Job
        Job job = Job.getInstance(conf,"TopNInMemsort");

        //3、设置Job的执行路径
        job.setJarByClass(TopNInMemSort.class);

        //4、设置map端的属性
        job.setMapperClass(MyMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        //5、设置reduce端的属性
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        //6、设置输入输出的参数
        FileInputFormat.setInputPaths(job,new Path("F:\\hdfs\\wordcount\\topN"));

        FileOutputFormat.setOutputPath(job,new Path("F:\\hdfs\\wordcount\\topNoutput"));

        //7、提交job
        boolean b = job.waitForCompletion(true);

        return (b?0:1);
    }


    public void setConf(Configuration conf) {
        conf.set("fs.defaultFS","file:///");
        conf.set("mapreduce.framework.name","local");
    }


    public Configuration getConf() {
        return new Configuration();
    }

    public static void main(String[] args) {
        try {
            System.exit(ToolRunner.run(new Configuration(),new TopNInMemSort(),args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
