package gov.epa.emissions.framework.client.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

public class FileDownloader {

    public static void main(String[] args) throws Exception {
        FileDownloader.ZeroCopyDownload("http://localhost:8080/exports/emf/emf_reference.backup", "c:\\temp\\temp");
    }
    
    public static void ZeroCopyDownload(String downloadURL, String destinationFolder) throws Exception {

        final HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
        httpclient.getParams()
        .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 3000)
        .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000)
        .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
        .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
        httpclient.start();
        try {
//            File download = new File(downloadURL);  //http://localhost:8080/exports/emf/asADSDSAFSAF.csv
//            ZeroCopyPost httpost = new ZeroCopyPost("http://localhost:8080/exports/emf/asADSDSAFSAF_4.csv", upload,
//                    ContentType.create("text/plain"));
              
            final URL uri=new URL(downloadURL);
            File uploadOld = new File(destinationFolder + "//" + downloadURL.substring(downloadURL.lastIndexOf("/")+1, downloadURL.length()));
            // Create a writable file channel
            final FileChannel wChannel = new FileOutputStream(uploadOld, false).getChannel();

//            
//            final RandomAccessFile channel = new RandomAccessFile(destinationFolder + "//" + downloadURL.substring(downloadURL.lastIndexOf("/")+1, downloadURL.length()), "rw");
            
            URLConnection ucon;
            ucon=uri.openConnection();
            ucon.connect();
            final String contentLengthStr = ucon.getHeaderField("content-length");
            System.out.println("Response file contentLengthStr: " + contentLengthStr);
            ZeroCopyConsumer<File> consumer = new ZeroCopyConsumer<File>(uploadOld) {

                long position = 0;
//                @Override
//                protected void onResponseReceived(HttpResponse httpResponse) {
//                    
//                }
                
                int count = 1;
                Double downloadedBytes = new Double(0F);

                @Override
                protected void onContentReceived(org.apache.http.nio.ContentDecoder decoder, org.apache.http.nio.IOControl ioctrl)  {
//                    try {
//    //Read data in
////                        super.onContentReceived(decoder, ioctrl);
//    ByteBuffer dst = ByteBuffer.allocate(2048);
//    position += decoder.read(dst);
//    
//    
////    if (position != 0) 
////        position += dst.position() + 1;
//    System.out.println("onContentReceived " + " " + position);
//    // Write the ByteBuffer contents; the bytes between the ByteBuffer's
//    // position and the limit is written to the file
//    wChannel.write(dst);
//    
//    while (dst.hasRemaining()) {
//        System.out.println("onContentReceived hasRemaining ");
//        dst.compact();
//        wChannel.write(dst);
//    }
////    if (position == 0) 
//        position = wChannel.size()+1;
//
//    
////                        System.out.println("onContentReceived " + " " + decoder.isCompleted());
////    
//    // Decode will be marked as complete when the content entity is fully transferred
////    if (decoder.isCompleted()) {
////        // Done
////    }
////    decoder.read(arg0)
//} catch (IOException e) {
//    // NOTE Auto-generated catch block
//    e.printStackTrace();
//}
//
//
////                    System.out.println("onContentReceived getBufferSize = " + httpclient.getParams().getParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE));
////                    System.out.println("onContentReceived " + count + " " + ((8 * 1024 * count) / (Double.parseDouble(contentLengthStr))));
//                    count++;
//
//                
//                
                
                
//                    System.out.println("Calling Consuming content");
                    boolean allRead = false;
                    ByteBuffer t = ByteBuffer.allocate(2048);

                    while(!allRead) {
                      int count = 0;
                    try {
                        count = decoder.read(t);
                        downloadedBytes += count;
                    } catch (IOException e1) {
                        // NOTE Auto-generated catch block
                        e1.printStackTrace();
                    }
                      if(count <= 0) {
                        allRead = true;
//                        System.out.println("Buffer reading is : " + decoder.isCompleted());
                      } else {
//                          System.out.println("****** Number of Bytes read is : " + count);
                         t.flip();
                         try {
                            wChannel.write(t);
                        } catch (IOException e) {
                            // NOTE Auto-generated catch block
                            e.printStackTrace();
                        }
                        t.clear();
                      }
                      
                    }
                
                    System.out.println(downloadedBytes / Double.parseDouble(contentLengthStr) * 100.0);
                }
                
                @Override
                protected File process(
                        final HttpResponse response, 
                        final File file,
                        final ContentType contentType) throws Exception {
                    System.out.println("process");
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new ClientProtocolException("Upload failed: " + response.getStatusLine());
                    }
                    return file;
                }

            };
//            HttpAsyncMethods.createGet("http://localhost:8080/")
//            HttpAsyncRequestProducer producer = HttpAsyncMethods.createGet("http://localhost:8080/exports/emf/emf.war");

              
            Future<File> future = httpclient.execute(HttpAsyncMethods.createGet(downloadURL), 
//                    new MyResponseConsumer(channel), 
                    consumer, 
                    new FutureCallback<File>() {

                        public void cancelled() {
                            // NOTE Auto-generated method stub
                            System.out.println("cancelled");
                        }

                        public void completed(File arg0) {
                            // NOTE Auto-generated method stub
                            System.out.println("completed");
                        }

                        public void failed(Exception arg0) {
                            // NOTE Auto-generated method stub
                            System.out.println("failed");
                        }
                    });
//            new FutureCallback<T>() {
//
//                public void completed(final T response) {
////                    latch.countDown();
//                    System.out.println("completed->" + response.getStatusLine());
//                }
//
//                public void failed(final Exception ex) {
////                    latch.countDown();
//                    System.out.println("failed->" + ex);
//                }
//
//                public void cancelled() {
////                    latch.countDown();
//                    System.out.println("cancelled");
//                }
//
//            }
            //            Future<File> future = httpclient.execute(httpost, consumer, null);
            File result = future.get();
            System.out.println("Response file length: " + result.length());
            System.out.println("Response file length: " + result.getAbsolutePath());
            
//            System.out.println("consumer.isDone() = " + consumer.isDone());
            System.out.println("Shutting down");

        
        
        
        
        
        
        
        
//            ExecutorService threadpool = Executors.newFixedThreadPool(2);
//            Async async = Async.newInstance().use(threadpool);
//
//            Request[] requests = new Request[] {
//                    Request.Get("http://www.google.com/"),
//                    Request.Get("http://www.yahoo.com/"),
//                    Request.Get("http://www.apache.com/"),
//                    Request.Get("http://www.apple.com/")
//            };
//
//            Queue<Future<File>> queue = new LinkedList<Future<File>>();
//            for (final Request request: requests) {
//                Future<File> future2 = async.execute(request, consumer, new FutureCallback<File>() {
//                    
//                    public void failed(final Exception ex) {
//                        System.out.println(ex.getMessage() + ": " + request);
//                    }
//                    
//                    public void completed(final File content) {
//                        System.out.println("Request completed: " + request);
//                    }
//                    
//                    public void cancelled() {
//                    }
//                    
//                });
//                queue.add(future2);
//            }
        
        
        
        
        
        
        
        } finally {
            httpclient.shutdown();
        }
        System.out.println("Done");
    }

    static class MyResponseConsumer extends AsyncCharConsumer<File> {
        private RandomAccessFile file;
        
        public MyResponseConsumer(RandomAccessFile file) {
            this.file = file;
        }
        
        @Override
        protected void onResponseReceived(final HttpResponse File) {
        }

        protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {
            while (buf.hasRemaining()) {
                this.file.write(buf.get());
//                System.out.print(this.file.);
//              System.out.print("onCharReceived");
            }
        }

        protected void releaseResources() {
        }

        protected File buildResult(final HttpContext context) throws Exception {
            return null;
        }


    }
}

