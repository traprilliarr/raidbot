package com.example.raid_bot_2.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.swing.text.html.FormSubmitEvent;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class Dev {

    private OAuthConsumer oAuthConsumer;

    public Dev() {
        String consumerKey = "U1ySk9EE3LZTlxqsX9eA6O1Bs";
        String consumerSecret = "1la80FSgWzWouicvcjoM1lTECzyHRwLgkrLaObZANoUqr4XPY8";
        String accessToken = "1076702630724829184-MRvkiiCs7HnzqpfhvnZwWrbnicYnwm";
        String accessTokenSecret = "VmTBh5y0NlA3bf9jlkM0fniaLNZ6UsxlJJImrXeV3PU29";

        setupContext(consumerKey, consumerSecret, accessToken, accessTokenSecret);

    }

    public void setupContext(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        this.oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
        oAuthConsumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
    }

    public void authorize(HttpRequestBase httpRequest) throws Exception {
        try {
            oAuthConsumer.sign(httpRequest);
        } catch (OAuthMessageSignerException e) {
            throw new Exception(e);
        } catch (OAuthExpectationFailedException e) {
            throw new Exception(e);
        } catch (OAuthCommunicationException e) {
            throw new Exception(e);
        }
    }

    public Data executeGetRequest(String customURIString)throws Exception{
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");

        HttpRequestBase httpRequest = null;
        URI uri = null;

        try {
            uri = new URI(customURIString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String methodtype = "GET";

        if (methodtype.equals(FormSubmitEvent.MethodType.GET.toString())) {
            httpRequest = new HttpGet(uri);
        }

        httpRequest.addHeader("content-type", "application/xml");
        httpRequest.addHeader("Accept","application/xml");

        try {
            authorize(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }


        HttpResponse httpResponse = null;
        try {
            HttpHost target = new HttpHost(uri.getHost(), -1, uri.getScheme());
            httpResponse = client.execute(target, httpRequest);
            System.out.println("Connection status : " + httpResponse.getStatusLine());


            InputStream inputStraem = httpResponse.getEntity().getContent();

            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStraem, writer, "UTF-8");

            String output = writer.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            Data userData = objectMapper.readValue(output, Data.class);


            Data userData1 = userData;
            return userData1;
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception("Exception when hit twitter api");
        }
    }

}
