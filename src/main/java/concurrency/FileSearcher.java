package concurrency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class FileSearcher {

	private final static ForkJoinPool forkJoinPool = new ForkJoinPool();

	public static class FileFinder extends RecursiveTask<Set<String>> {

		final File file;
		final String extension;

		public FileFinder(final File theFile, final String theExtension) {
			file = theFile;
			extension = theExtension;
		}

		@Override
		public Set<String> compute() {

			Set<String> fileList = new TreeSet<String>();
			String fileName;
			long size = 0;

			if (file.isFile()) { // not a directory
				size = file.length();

				String absFileName;
				//try {
					absFileName =  file.getPath(); //child.getCanonicalPath();
					// System.out.println("File Name: " +
					// absFileName);
					if (absFileName.endsWith(extension)) {
						// System.out.println("File Name: " +
						// absFileName);
						try {
							fileList.add(file.getCanonicalPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

			} else { // is a directory

				final File[] children = file.listFiles();
				if (children != null) {

					List<ForkJoinTask<Set<String>>> tasks = new ArrayList<ForkJoinTask<Set<String>>>();

					for (final File child : children) {
						if (child.isFile()) {
							String absFileName;
 					 
 							    try {
									absFileName =  child.getCanonicalPath();
									if (absFileName.endsWith(extension)) {
										// System.out.println("File Path: " + child.getParent()); 								 
										fileList.add(child.getCanonicalPath());
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									System.out.println("Exception:" + e.getMessage());
								}								
								 
							size += child.length();
						} else {
							tasks.add(new FileFinder(child, extension));
						}
					}

					for (final ForkJoinTask<Set<String>> task : invokeAll(tasks)) {
						// size += task.join();
						fileList.addAll(task.join());
					}
				}
			}

			return fileList;
		}
	}

	public Set<String> getDirectoryFiles(String directory, String extension) {
		
		final long start = System.nanoTime();

		final Set<String> fileList = forkJoinPool.invoke(new FileFinder(new File(directory), extension)); // args[0])));

		final long end = System.nanoTime();
		System.out.println("Total Size: " + fileList.size());
		System.out.println("Time taken: " + (end - start) / 1.0e9);
		
		int counter = 0;
		//for (String fileName: fileList) {
		//	System.out.println("" + (++counter) + ". " + fileName);
		//}
		
		return fileList;
	}
	
	public static void main(final String[] args) {
		// File file = new File("/Users/rene/learn/learn5/docker");
		String directory = "/Users/rene/learn/learn4/guava";
		final long start = System.nanoTime();

		final Set<String> fileList = forkJoinPool.invoke(new FileFinder(new File(directory), ".java")); // args[0])));

		final long end = System.nanoTime();
		System.out.println("Total Size: " + fileList.size());
		System.out.println("Time taken: " + (end - start) / 1.0e9);
		
		int counter = 0;
		for (String fileName: fileList) {
			System.out.println("" + (++counter) + ". " + fileName);
		}
	}
}
