package cz.dagblog.echo2blogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import cz.dagblog.echo2blogger.Echo.Comment;

public class Blogger {

	private static final Logger log = Logger.getLogger(Blogger.class.getName());
	private final String blogId;
	private final GoogleService googleService;

	public Blogger(String username, String password, String blogId) {
		super();
		this.blogId = blogId;
		this.googleService = new GoogleService("blogger",
				"Echo to Blogger comments migration tool");
		try {
			this.googleService.setUserCredentials(username, password);
		} catch (AuthenticationException e) {
			throw new RuntimeException("Authentication error, please see details below", e);
		}
	}
	
	public void createOrUpdateComments(String postId, List<Comment> comments) {
		if(comments.size() > 0) {
			try {			
				String commentsFeedUri = "http://www.blogger.com/feeds/" + blogId + "/" + postId + "/comments/default";
				log.info("Creating or updating comments for post " + postId);
				URL feedUrl = new URL(commentsFeedUri);
				deleteComments(commentsFeedUri);			
				for (Comment comment : comments) {
					try {
						String author = "Author:" + comment.author  + "<br><br>";
						if(comment.text.length() + author.length() <= 4000) {	
							Entry commentEntry = new Entry();
							commentEntry.setContent(new HtmlTextConstruct(author + comment.text));
							
								googleService.insert(feedUrl, commentEntry);
						
						} else {
							log.info("Comment is too long for Blogger, splitting into smaller comment chunks");
							List<String> split = split(comment.text, 4000 - author.length());
							for (int i = 0; i < split.size(); i++) {
								Entry commentEntry = new Entry();							
								String html;
								if(i == 0) {
									html = author + split.get(i) + "...";
								} else {
									html = author + "..." + split.get(i);
								}							
								commentEntry.setContent(new HtmlTextConstruct(html));
								googleService.insert(feedUrl, commentEntry);
							}
						}
					} catch(AuthenticationException e) {
						log.log(Level.SEVERE, "Cannot create comment", e.getMessage());
					}	
				}
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			
			catch (ServiceException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void deleteComments(String commentsFeedURL) {
		try {
			URL feedUrl = new URL(commentsFeedURL);
			int startIndex = 1;
			int maxResult = 10;
	
			for (;;) {
				Query myQuery = new Query(feedUrl);
				myQuery.setStartIndex(startIndex);
				myQuery.setMaxResults(maxResult);
				Feed resultFeed = googleService.query(myQuery, Feed.class);
				int resultSetSize = resultFeed.getEntries().size();
				boolean fetchMore = resultSetSize % maxResult == 0 && resultSetSize != 0;
				for (int i = 0; i < resultSetSize; i++) {
				    Entry entry = resultFeed.getEntries().get(i);				    
				    String editURL = entry.getEditLink().getHref();				    
					googleService.delete(new URL(editURL));			   
				}
				if (fetchMore) {
					startIndex = startIndex + maxResult;
				} else {
					break;
				}
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> fetchPostIds() {
		List<String> ids = new ArrayList<String>();
		try {
			String postsFeed = "http://www.blogger.com/feeds/" + blogId + "/posts/default";
			log.info("Fetching id for all posts " + postsFeed);
			URL feedUrl = new URL(postsFeed);
			int startIndex = 1;
			int maxResult = 100;

			for (;;) {
				Query myQuery = new Query(feedUrl);
				myQuery.setStartIndex(startIndex);
				myQuery.setMaxResults(maxResult);
				Feed resultFeed = googleService.query(myQuery, Feed.class);
				int resultSetSize = resultFeed.getEntries().size();
				boolean fetchMore = resultSetSize % 100 == 0 && resultSetSize != 0;
				for (int i = 0; i < resultSetSize; i++) {
				    Entry entry = resultFeed.getEntries().get(i);				    
				    String id = entry.getId();
				    String postIdNumericPart = id.substring(id.lastIndexOf("-") + 1);
				    ids.add(postIdNumericPart);				   
				}
				if (fetchMore) {
					startIndex = startIndex + maxResult;
				} else {
					break;
				}
			}
		} catch (AuthenticationException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("Totaly fetched posts " + ids.size());
		return ids;
	}

	private static List<String> split(String s, int n) {
		List<String> result = new ArrayList<String>();
		
		int startPosition = 0;
		int endPosition = n;
		char[] charArray = s.toCharArray();
		for(;;) {
			if(endPosition == charArray.length || charArray[endPosition] == ' ') {
				result.add(s.substring(startPosition, endPosition));
				if(endPosition == charArray.length) {
					break;
				}
				startPosition = endPosition + 1;
				endPosition = startPosition + n;
				if(endPosition > charArray.length) {
					endPosition = charArray.length;
				}				
			} else {
				endPosition--; 
			}			
		}
		return result;
	}


}
