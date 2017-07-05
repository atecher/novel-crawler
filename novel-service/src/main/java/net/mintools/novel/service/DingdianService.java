package net.mintools.novel.service;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by mark on 2017/1/9.
 */

public class DingdianService {
    public static void read(String url,String base) {
        Document doc = getDocument(url);
        String novel_title = doc.getElementsByTag("h1").first().text();
        String baseUrl = doc.baseUri();
        Elements als = doc.getElementById("at").getElementsByTag("a");

        ExecutorService pool = Executors.newFixedThreadPool(50);
        List list = new ArrayList();
        for (int i=0;i<als.size();i++) {
            String detailUrl = als.get(i).attr("abs:href");
            Callable c = new MyCallable(i,als.get(i).text(),detailUrl,base);
            try{
                Future f = pool.submit(c);
                list.add(f);
            }catch (Exception e){
                System.out.println(als.get(i).text());
                System.out.println(detailUrl);
            }

        }
        pool.shutdown();

    }

    public static void write(String base) throws IOException {
        File dir=new File(base+"/in/");
        List<File> files= Arrays.asList(dir.listFiles());

        Collections.sort(files, new Comparator<File>(){

            public int compare(File o1, File o2) {
                if(o1.isDirectory() && o2.isFile())
                    return -1;
                if(o1.isFile() && o2.isDirectory())
                    return 1;
                int i=Integer.valueOf(o1.getName().replace(".txt",""))>=Integer.valueOf(o2.getName().replace(".txt",""))?1:-1;
                return i;
            }
        });
        File out=new File(base+"/out/out.txt");
        for(File f:files){
            System.out.println(f.getName());
            FileUtils.writeStringToFile(out, FileUtils.readFileToString(f)+"\n\n",true);
        }
    }

    public static void main(String[] args) throws IOException {
        String listUrl = "http://www.23us.so/files/article/html/11/11882/index.html";
        String baseDir="d:/novel";
        read(listUrl,baseDir);
        write(baseDir);
    }




   static class MyCallable implements Callable {
        private int taskNum;
        private String url;
        private String title;
       private String base;

        MyCallable(int taskNum,String title,String url,String base) {
            this.taskNum = taskNum;
            this.url = url;
            this.title = title;
            this.base=base;
        }

        public Object call() throws Exception {
            System.out.println(">>>" + taskNum + "任务启动");
            Date dateTmp1 = new Date();
            String baseDir=base+"/in/";
            File f=new File(baseDir+taskNum+".txt");
            if(!f.exists()) {
                String content = getContent(url);
                FileUtils.writeStringToFile(f, title+"\n\n", true);
                FileUtils.writeStringToFile(f, content, true);
            }
            long time = new Date().getTime() - dateTmp1.getTime();
            System.out.println(">>>" + taskNum + "任务终止");
            return taskNum + "任务返回运行结果,当前任务时间【" + time + "毫秒】";
        }
    }

    public static String getContent(String url) {
        Document doc = getDocument(url);

        String content = doc.getElementById("contents").html();
        String text = Jsoup.parse(content.replaceAll("(?i)<br[^>]*>", "br2nl").replaceAll("\n", "br2nl")).text();
//        text = text.replace("86_86873", "");
        text = text.replace("br2nlbr2nl", "br2nl");
        text = text.replace("br2nl br2nl", "br2nl");
        text = text.replace("br2nl", "\n");
        text = text.replace("br2nl", "\n");
        text = text.replace("\n\n", "\n");
        text = text.replace(" ", "");
        text = text.replace("    ", "");
        text = text.replace("&nbsp;", "");
//        text = text.replace("最快更新，无弹窗阅读请。","");
//        text = text.replace("readx();", "").trim();
        return text;
    }


    public static Document getDocument(String url) {
        try {
            return Jsoup.parse(new URL(url).openStream(), "GBK", url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
