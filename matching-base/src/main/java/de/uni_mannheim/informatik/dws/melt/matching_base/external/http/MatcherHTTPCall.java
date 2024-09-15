package de.uni_mannheim.informatik.dws.melt.matching_base.external.http;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a matcher service.
 */
public class MatcherHTTPCall extends MatcherURL implements IMatcher<URL, URL, URL> {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherHTTPCall.class);

    public static boolean CHECK_URL = true;
    
    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    
    public static void setSSLValidation(boolean checkSSLCertificates){
        if(checkSSLCertificates){
            httpClient = HttpClients.createDefault();
        }else{
            try {
                httpClient = HttpClients.custom()
                        .setSSLContext(SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();
            } catch (NoSuchAlgorithmException|KeyStoreException|KeyManagementException ex) {
                LOGGER.error("Could not create SSLContext while disabeling ssl validation. It will still check ssl certificates. Error Message: {}", ex.getMessage());
                httpClient = HttpClients.createDefault();
            }
        }
    }
    
    /**
     * The number of retry operations to perform when an exception occurs.
     */
    private int maxTrials = 5;

    /**
     * The amount of seconds to sleep when an exception occurred before retrying.
     */
    private int sleepTimeInSeconds = 10;
    /**
     * URI where the matching service is located.
     */
    private URI uri;

    /**
     * If true, then the content of the file URI is read and transferred.
     * If false, then only the URI is transferred but then the matching system needs to have access to the URI.
     */
    private boolean sendContent;

    /**
     * The RequestConfig which contains timeouts to be used in http call.
     */
    private RequestConfig requestConfig;

    /**
     * Creates a matcher which wraps a matching service available at the given URI with timeout options.
     *
     * @param uri                      URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     * @param sendContent              If true, then the content of the file URI is read and transferred.
     *                                 If false, then only the URI is tranferred but then the matching system needs to have access to the URI.
     * @param socketTimeout            the time in milliseconds waiting for data – after the connection is established;
     *                                 maximum time between two data packets. Zero means infinite timeout. Negative usually means systems default.
     * @param connectTimeout           the timeout in milliseconds until a connection is established.
     *                                 Zero means infinite timeout. Negative usually means systems default.
     * @param connectionRequestTimeout timeout in milliseconds when requesting a connection from the connection manager.
     *                                 Zero means infinite timeout. Negative usually means systems default.
     */
    public MatcherHTTPCall(URI uri, boolean sendContent, int socketTimeout, int connectTimeout, int connectionRequestTimeout) {
        this.uri = uri;
        if(CHECK_URL && this.uri.getScheme().equals("http") == false && this.uri.getScheme().equals("https") == false){
            LOGGER.warn("The scheme of the URI given to call a matcher is not http(s). This may be a cause of error (if the URL is fine, you can disable this warning with MatcherHTTPCall.CHECK_URL=false;)");
        }
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
     *
     * @param uri         URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     * @param sendContent If true, then the content of the file URI is read and transferred.
     *                    If false, then only the URI is transferred but then the matching system needs to have access to the URI.
     */
    public MatcherHTTPCall(URI uri, boolean sendContent) {
        this(uri, sendContent, 0, 0, 0);
    }

    /**
     * Creates a matcher which wraps a matching service available at the given URI.
     * Only the URIs of the test case are transferred to the system and no timeout is applied.
     *
     * @param uri URI where the matching service is located. URI can be created from string with {@link URI#create(java.lang.String) }.
     */
    public MatcherHTTPCall(URI uri) {
        this(uri, true);
    }
    
    /**
     * Creates a matcher which wraps a matching service available at the given URI.Only the URIs of the test case are transferred to the system and no timeout is applied.
     *
     * @param uri URI where the matching service is located as string.
     * @throws java.net.URISyntaxException in case the given URI is not parsable
     */
    public MatcherHTTPCall(String uri) throws URISyntaxException {
        this(new URI(uri));
    }


    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        return match(source, target, inputAlignment, null);
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment, URL parameters) throws Exception {
        //https://stackoverflow.com/questions/1378920/how-can-i-make-a-multipart-form-data-post-request-using-java?rq=1

        int currentTrials = 0;
        boolean isStatusCodeError = false;

        while (currentTrials < maxTrials) {

            HttpPost request = new HttpPost(uri);

            if (this.sendContent) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("source", source.openStream(), ContentType.DEFAULT_BINARY, getFileName(source.getPath())); //TODO: close stream?
                builder.addBinaryBody("target", target.openStream(), ContentType.DEFAULT_BINARY, getFileName(target.getPath()));

                if (inputAlignment != null){
                    String filename = getFileName(inputAlignment.getPath());
                    LOGGER.info("Add input alignment to HTTP request: {}", filename);
                    builder.addBinaryBody("inputAlignment", inputAlignment.openStream(), ContentType.DEFAULT_BINARY, filename);
                }
                if (parameters != null){
                    String filename = getFileName(parameters.getPath());
                    LOGGER.info("Add parameters to HTTP request: {}", filename);
                    builder.addBinaryBody("parameters", parameters.openStream(), ContentType.DEFAULT_BINARY, filename);
                }
                request.setEntity(builder.build());
            } else {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("source", source.toString()));
                params.add(new BasicNameValuePair("target", target.toString()));
                if (inputAlignment != null){
                    LOGGER.info("Add iinput alignment to HTTP request");
                    params.add(new BasicNameValuePair("inputAlignment", inputAlignment.toString()));
                }
                if (parameters != null){
                    LOGGER.info("Add parameters to HTTP request");
                    params.add(new BasicNameValuePair("parameters", parameters.toString()));
                }

                request.setEntity(new UrlEncodedFormEntity(params));
            }
            request.setConfig(this.requestConfig);
            LOGGER.info("Execute now the following HTTP request: {}", request);


            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new Exception("No server response.");
                } else {
                    if (response.getStatusLine().getStatusCode() != 200) {
                        LOGGER.error("Server returned a non 200 status code: {}", EntityUtils.toString(entity));
                        isStatusCodeError = true;
                        throw new IOException("Server returned a non 200 status code.");
                    }
                    if (this.sendContent) {
                        File alignmentFile = File.createTempFile("alignment", ".rdf");
                        try (OutputStream out = new FileOutputStream(alignmentFile)) {
                            entity.writeTo(out);
                        }
                        return alignmentFile.toURI().toURL();
                    } else {
                        String resultString = EntityUtils.toString(entity);
                        return new URL(resultString);
                    }
                }
            } catch (Exception e) {
                if (isStatusCodeError) {
                    throw e;
                }
                currentTrials++;
                if (currentTrials < maxTrials) {
                    LOGGER.info("Endpoint is not ready / an exception occurred. Waiting {} seconds. Trial {}/{}", 
                            sleepTimeInSeconds, currentTrials, maxTrials);
                    try {
                        Thread.sleep(sleepTimeInSeconds * 1000);
                    } catch (InterruptedException ie) {
                        LOGGER.error("Problem occurred while trying to sleep.", ie);
                    }
                } else {
                    LOGGER.error("An exception occurred.", e);
                }
            }
        }
        throw new Exception("The service could not be reached after " + maxTrials + ".");
    }

    public void setTimeout(int socketTimeout, int connectTimeout, int connectionRequestTimeout) {
        this.requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }

    public void setTimeout(int timeout) {
        setTimeout(timeout, timeout, timeout);
    }

    /**
     * If true, then the content of the file URI is read and transferred.
     * If false, then only the URI is tranferred but then the matching system needs to have access to the URI.
     *
     * @param sendContent the choise if the whole content is send or not.
     */
    public void setSendContent(boolean sendContent) {
        this.sendContent = sendContent;
    }

    public boolean isSendContent() {
        return sendContent;
    }

    public int getMaxTrials() {
        return maxTrials;
    }

    public void setMaxTrials(int maxTrials) {
        this.maxTrials = maxTrials;
    }

    public int getSleepTimeInSeconds() {
        return sleepTimeInSeconds;
    }

    public void setSleepTimeInSeconds(int sleepTimeInSeconds) {
        this.sleepTimeInSeconds = sleepTimeInSeconds;
    }
    
    private String getFileName(String fullPath){        
        if (fullPath == null) {
            return null;
        }
        final int lastUnixPos = fullPath.lastIndexOf('/'); // unix separator 
        final int lastWindowsPos = fullPath.lastIndexOf('\\'); // windows separator
        final int index = Math.max(lastUnixPos, lastWindowsPos);        
        return fullPath.substring(index + 1);
    }

    @Override
    public String toString() {
        return "MatcherHTTPCall{" + uri + '}';
    }
}
