package cz.dagblog.echo2blogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class Echo {

	private static final Logger log = Logger.getLogger(Echo.class.getName());
	public static final String JS_KIT_RSS_FEED = "http://js-kit.com/rss/";
	public final String registeredSite;

	public Echo(String registeredSite) {
		super();
		this.registeredSite = registeredSite;
	}	
	
	public List<Comment> fetchComments(final String postId) {
		try {
			String commentsFeed = JS_KIT_RSS_FEED + registeredSite + "/" + postId;
			log.fine("Fetching comments from " + commentsFeed);
			URL url = new URL(commentsFeed);
			URLConnection urlConnection = (URLConnection)url.openConnection();
		
			HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;		
			httpConnection.setRequestMethod("POST");
			httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);		
			if(httpConnection.getResponseCode() >= 400) {
				String error = "Fetching comments for post " + postId + " failed. Reason: " + isToString(httpConnection.getErrorStream());
				log.severe(error);
				throw new RuntimeException(error);
			} else {
				List<Comment> comments = parseXML(httpConnection.getInputStream());
				Collections.sort(comments);
				return comments;
			}
			
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (ProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {	
			
		}
	}
	
	private List<Comment> parseXML(InputStream is) throws IOException {
		List<Comment> comments = new ArrayList<Echo.Comment>();
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(is);
			Comment comment = null;
			boolean guid = false;
			boolean pubDate = false;
			boolean creator = false;
			boolean description = false;
			
			while(reader.hasNext()){
			    int eventType = reader.next();
			    if(eventType == XMLStreamReader.START_ELEMENT){
			    	if(reader.getLocalName() == "item") {
			    		comment = new Comment();
			    	}
			    	
			    	if(reader.getLocalName() == "guid") {
			    		guid = true;
			    	}
			    	
			    	if(reader.getLocalName() == "pubDate") {
			    		pubDate = true;
			    	}
			    	
			    	if(reader.getLocalName() == "creator") {
			    		creator = true;
			    	}
			    	
			    	if(reader.getLocalName() == "description") {
			    		description = true;
			    	}			    	
			    }
			    
			    
			    
			    if(eventType == XMLStreamReader.CHARACTERS && comment != null) {
			    	if(guid) {
			    		comment.id = reader.getText();
			    	}
			    	
			    	if(pubDate) {
			    		SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");			    		
			    		comment.published  = sdf.parse(reader.getText()).getTime();			    		
			    	}
			    	
			    	if(creator) {
			    		comment.author = reader.getText();
			    	}
			    	
			    	if(description) {
			    		comment.text += reader.getText();
			    	}
			    }
			    
			    if(eventType == XMLStreamReader.END_ELEMENT){
			    	if(reader.getLocalName() == "item") {
			    		comments.add(comment);
			    		comment = null;
			    	}
			    	
			    	if(reader.getLocalName() == "guid") {
			    		guid = false;
			    	}
			    	
			    	if(reader.getLocalName() == "pubDate") {
			    		pubDate = false;
			    	}
			    	
			    	if(reader.getLocalName() == "creator") {
			    		creator = false;
			    	}
			    	
			    	if(reader.getLocalName() == "description") {
			    		description = false;
			    	}	
			    }

			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} finally {
			is.close();
		}
		return comments;
	}
	
	private String isToString(InputStream is) throws IOException {
		if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
	}
	
	public static final class Comment implements Comparable<Comment>{
		String id = "";
		String author = "";
		String text = "";
		long published;
		
		@Override
		public String toString() {
			return "Comment [id=" + id + ", author=" + author + ", published="
					+ published + ", text=" + text + "]";
		}

		@Override
		public int compareTo(Comment o) {			
			if(this.published == o.published){
				return 0;				
			} 
			return this.published > o.published ? 1 : -1;
		}		
	}
}
