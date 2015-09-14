/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
***/
package concurrency;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class FileSize {
  
  private final static ForkJoinPool forkJoinPool = new ForkJoinPool();
  
  private static class FileSizeFinder extends RecursiveTask<Long> {
    final File file;
    
    public FileSizeFinder(final File theFile) {
      file = theFile;
    }
    
    @Override public Long compute() {
      long size = 0;
      if (file.isFile()) {
        size = file.length();
      } else {
        final File[] children = file.listFiles();
        if (children != null) {
          List<ForkJoinTask<Long>> tasks = 
            new ArrayList<ForkJoinTask<Long>>();
          for(final File child : children) {
            if (child.isFile()) {
              size += child.length();
            } else {
              tasks.add(new FileSizeFinder(child));
            }
          }
          
          for(final ForkJoinTask<Long> task : invokeAll(tasks)) {
            size += task.join();
          }
        }
      }
      
      return size;
    }
  }

  public static void main(final String[] args) {
	//File file = new File("/Users/rene/learn/learn5/docker");
	String directory = "/Users/rene/learn/learn5/docker";
    final long start = System.nanoTime();
    final long total = forkJoinPool.invoke(
        new FileSizeFinder(new File(directory)));  //args[0])));
    final long end = System.nanoTime();
    System.out.println("Total Size: " + total);
    System.out.println("Time taken: " + (end - start)/1.0e9);    
  }
}
