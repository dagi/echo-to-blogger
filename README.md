# Echo to Blogger comments migration tool

This tool migrates existing comments from Echo (HaloScan) to Blogger. The tool can be used just only for comments associated with posts created in Blogger. 

Why to use this tool instead of original Echo synchronization
* The Echo subscription for your domain has expired
* You would like to export only some comments
* You don't trust to anything what you cannot debug ;-)
* It's extensible, you can hack whatever you want

The tool preserves original authors on the first line of a comment because of known limitation of Blogger API. See <http://code.google.com/apis/blogger/docs/2.0/developers_guide_java.html#Comments>

## How does it work?

The tool fetches identifiers for all posts from Blogger. These identifiers are used for fetching comments from Echo and then imported to Blogger. You can run the tool multiple times, because existing comments are deleted when you re-run the tool. Beware you may lost some comments in Blogger if a post has independent comments in both systems.

## Running

Before you start
* Download and install Java 1.6 (JRE)
* Download the tool in binary form (<http://github.com/dagi/echo-to-blogger/downloads>) or build from source code
* Temporary disable Blogger comments email notification otherwise your maibox will blow up, because every imported comments firing a new mail


* run `java -jat echo-to-blogger.jar 'username' 'password' 'blogid' 'registered site name'`
** Username and Password used for login to Blogger
** Blog id in Blogger (you can find it in your RSS or Atom feed)
** Site name in Echo (probably your domain) 

Example:
* `java -jar echo-to-blogger.jar roman.pichlik@gmail.com secret 4053149 dagblog.cz`



