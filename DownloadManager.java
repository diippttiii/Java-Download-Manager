
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.*;
import java.net.HttpURLConnection;
public class DownloadManager {
	public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TreeMap<Integer, Queue<String>> downloadQueueByPriority = new TreeMap<>();

        System.out.println("Enter the priority, category, and URLs to download (priority, category, URL per line, type 'done' to finish):");

        String inputLine;
        while (!(inputLine = scanner.nextLine()).equalsIgnoreCase("done")) {
            String[] parts = inputLine.split(",\\s*", 3);
            if (parts.length == 3) {
                try {
                    int priority = Integer.parseInt(parts[0].trim());
                    String category = parts[1].trim();
                    String url = parts[2].trim();

                    downloadQueueByPriority.putIfAbsent(priority, new LinkedList<>());
                    downloadQueueByPriority.get(priority).add(category + "," + url);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid priority format. Please enter a valid integer for priority.");
                }
            } else {
                System.out.println("Invalid input format. Please enter 'priority, category, URL'.");
            }
        }

        scanner.close();

        System.out.println("Starting simultaneous downloads...");

        for (Integer priority : downloadQueueByPriority.keySet()) {
            Queue<String> priorityQueue = downloadQueueByPriority.get(priority);
            while (!priorityQueue.isEmpty()) {
                String[] parts = priorityQueue.poll().split(",", 2);
                String category = parts[0];
                String url = parts[1];
                checkAndDownloadFile(url, category);
            }
        }

        System.out.println("All downloads completed.");
    }

    private static void checkAndDownloadFile(String fileUrl, String category) {
    	 try {
             URL url = new URL(fileUrl);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("HEAD");

             // Get the "Content-Type" header value to identify the file type
             String contentType = connection.getHeaderField("Content-Type");
             if (contentType != null && (contentType.startsWith("image") || contentType.startsWith("video")
                     || contentType.startsWith("application/pdf") || contentType.startsWith("application/msword")
                     || contentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                     || contentType.startsWith("application/vnd.ms-excel")
                     || contentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                     || contentType.startsWith("audio/mpeg") || contentType.startsWith("audio/wav")
                     || contentType.startsWith("audio/ogg") || contentType.startsWith("audio/mp3")|| contentType.startsWith("text/html"))) {
                 System.out.println("File detected: " + fileUrl);
                 downloadFile(fileUrl, category);
             } else {
                 System.out.println("Not a supported file type: " + fileUrl);
                 // Handle other content types or skip
             }
         } catch (IOException e) {
             System.out.println("Error checking " + fileUrl + ": " + e.getMessage());
         }
     }


    private static void downloadFile(String fileUrl, String category) {
        try {
            URL url = new URL(fileUrl);
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            File categoryFolder = new File(category);
            if (!categoryFolder.exists()) {
                categoryFolder.mkdir();
            }

            File file = new File(categoryFolder, fileName);
            long downloadedBytes = 0;

            if (file.exists()) {
                downloadedBytes = file.length();
            }

            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                 BufferedOutputStream bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream)) {

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
                connection.connect();

                int responseCode = connection.getResponseCode();

                if (responseCode / 100 != 2) {
                    System.out.println("Error response code: " + responseCode);
                    return;
                }

                long remoteFileSize = connection.getContentLengthLong();

                if (downloadedBytes >= remoteFileSize) {
                    System.out.println(fileName + " has already been downloaded completely.");
                    return;
                }

                bufferedInputStream.skip(downloadedBytes);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    bufferedFileOutputStream.write(buffer, 0, bytesRead);
                }
                bufferedFileOutputStream.flush(); // Flush the buffered output stream

                System.out.println(fileName + " downloaded in the " + category + " category.");
            } catch (IOException e) {
                System.out.println("Error downloading " + fileUrl + ": " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error handling " + fileUrl + ": " + e.getMessage());
        }
    }
}