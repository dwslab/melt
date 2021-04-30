package de.uni_mannheim.informatik.dws.melt.matching_base.external.http;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a matcher service.
 */
public class MatcherHTTPCall extends MatcherURL{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherHTTPCall.class);
    
    private static CloseableHttpClient httpClient = HttpClients.createDefault();
        
    /**
     * URI where the matching service is located.
     */
    private URI uri;
    
    /**
     * If true, then the content of the file URI is read and transferred.
     * If false, then only the URI is tranferred but then the matching system needs to have access to the URI.
     */
    private boolean sendContent;
    
    /**
     * The RequestConfig which contains timeouts to be used in http call.
     */
    private RequestConfig requestConfig;
    
    
    /**
     * Creates a matcher which wraps a matching service available at the given URI with timeout options.
     * @param uri URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     * @param sendContent  If true, then the content of the file URI is read and transferred.
     *      If false, then only the URI is tranferred but then the matching system needs to have access to the URI.
     * @param socketTimeout the time in milliseconds waiting for data – after the connection is established; 
     *      maximum time between two data packets. Zero means infinite timeout. Negative usually means systems default.
     * @param connectTimeout the timeout in milliseconds until a connection is established.
     *      Zero means infinite timeout. Negative usually means systems default.
     * @param connectionRequestTimeout timeout in milliseconds when requesting a connection from the connection manager.
     *      Zero means infinite timeout. Negative usually means systems default.
     */
    public MatcherHTTPCall(URI uri, boolean sendContent, int socketTimeout, int connectTimeout, int connectionRequestTimeout) {
        this.uri = uri;
        this.sendContent = sendContent;
        this.requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }
    

    /**
     * Creates a matcher which wraps a matching service available at the given URI.
     * No timeout is applied.
     * @param uri URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     * @param sendContent  If true, then the content of the file URI is read and transferred. 
     *      If false, then only the URI is tranferred but then the matching system needs to have access to the URI.
     */
    public MatcherHTTPCall(URI uri, boolean sendContent) {
        this(uri, sendContent, 0, 0, 0);
    }
    
    /**
     * Creates a matcher which wraps a matching service available at the given URI.
     * Only the URIs of the test case are transferred to the system and no timeout is applied.
     * @param uri URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     */
    public MatcherHTTPCall(URI uri) {
        this(uri, false);
    }
    
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        //https://stackoverflow.com/questions/1378920/how-can-i-make-a-multipart-form-data-post-request-using-java?rq=1
        HttpPost request = new HttpPost(uri);
        
        Properties p = new Properties();
        p.setProperty("test", "bla");
        
        if(this.sendContent){            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addBinaryBody("source", source.openStream()) //TODO: close stream?
                .addBinaryBody("target", target.openStream());
            if(inputAlignment != null)
                builder.addBinaryBody("inputAlignment", inputAlignment.openStream());
            
            for(Entry<Object, Object> entry : p.entrySet()){
                builder.addTextBody(entry.getKey().toString(), entry.getValue().toString());
            }
            request.setEntity(builder.build());
        }else{
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("source", source.toString()));
            params.add(new BasicNameValuePair("target", target.toString()));
            if(inputAlignment != null)
                params.add(new BasicNameValuePair("inputAlignment", inputAlignment.toString()));
            
            for(Entry<Object, Object> entry : p.entrySet()){
                params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
            }
            request.setEntity(new UrlEncodedFormEntity(params));
        }        
        request.setConfig(this.requestConfig);        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                if(response.getStatusLine().getStatusCode() != 200){
                    LOGGER.error("Server returned a non 200 status code: {}",EntityUtils.toString(entity));
                    throw new IOException("Server returned a non 200 status code.");
                }
                if(this.sendContent){
                    File alignmentFile = File.createTempFile("alignment", ".rdf");
                    try(OutputStream out = new FileOutputStream(alignmentFile)){
                        entity.writeTo(out);
                    }
                    return alignmentFile.toURI().toURL();
                }else{
                    String resultString = EntityUtils.toString(entity);
                    return new URL(resultString);
                }
            }
        }
    }
}