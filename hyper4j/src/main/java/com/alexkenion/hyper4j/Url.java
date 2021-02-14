package com.alexkenion.hyper4j;

public class Url {
	
	public static final String
		SCHEME_SEPARATOR="://",
		QUERY_SEPARATOR="?",
		FRAGMENT_SEPARATOR="#",
		PORT_SEPARATOR=":",
		PATH_SEPARATOR="/",
		USER_INFO_SEPARATOR="@";
	
	public static enum Scheme {
		
		HTTP("http", 80),
		HTTPS("https", 443);
		
		private String urlScheme;
		private int assignedPort;
		
		private Scheme(String urlScheme, int assignedPort) {
			this.urlScheme=urlScheme;
			this.assignedPort=assignedPort;
		}
		
		public String getUrlScheme() {
			return urlScheme;
		}
		
		public int getAssignedPort() {
			return assignedPort;
		}
		
		public String toString() {
			return getUrlScheme();
		}
		
	}

	private Scheme scheme;
	private String host;
	private String userInfo;
	private int port;
	private String path;
	private String query;
	private String fragment;
	
	public Url(Scheme scheme, String host, String userInfo, int port, String path, String query) {
		this.scheme=scheme;
		this.host=host;
		this.userInfo=userInfo;
		this.port=port;
		this.path=path;
		this.query=query;
	}
	
	public Url() {
		this(Scheme.HTTP, "", "", Scheme.HTTP.getAssignedPort(), "", "");
	}
	
	public Scheme getScheme() {
		return this.scheme;
	}
	
	public Url setScheme(Scheme scheme) {
		this.scheme=scheme;
		return this;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public Url setHost(String host) {
		this.host=host;
		return this;
	}
	
	public String getUserInfo() {
		return this.userInfo;
	}
	
	public Url setUserInfo(String userInfo) {
		this.userInfo=userInfo;
		return this;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public Url setPort(int port) {
		this.port=port;
		return this;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public Url setPath(String path) {
		this.path=path;
		return this;
	}
	
	public boolean hasQuery() {
		return query!=null&&!query.isEmpty();
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public Url setQuery(String query) {
		this.query=query;
		return this;
	}
	
	public boolean hasFragment() {
		return fragment!=null&&!fragment.isEmpty();
	}
	
	public String getFragment() {
		return this.fragment;
	}
	
	public Url setFragment(String fragment) {
		this.fragment=fragment;
		return this;
	}
	
	public String getAuthority() {
		StringBuilder builder=new StringBuilder();
		if(!userInfo.isEmpty())
			builder.append(userInfo).append(USER_INFO_SEPARATOR);
		builder.append(host);
		if(port!=scheme.getAssignedPort())
			builder.append(PORT_SEPARATOR).append(port);
		return builder.toString();
	}

	/**
	 * @return the relative portion of this URL(path, query, and fragment)
	 */
	public String toRelativeString() {
		StringBuilder builder=new StringBuilder(path);
		if(!query.isEmpty())
			builder.append(QUERY_SEPARATOR).append(query);
		if(!fragment.isEmpty())
			builder.append(FRAGMENT_SEPARATOR).append(fragment);
		return builder.toString();
	}
	
	/**
	 * @return the absolute URL
	 */
	public String toString() {
		StringBuilder builder=new StringBuilder(scheme.toString());
		builder.append(SCHEME_SEPARATOR);
		builder.append(getAuthority());
		builder.append(toRelativeString());
		return builder.toString();
	}
	
	public static Url parseRelative(String relativeUrl) {
		Url url=new Url();
		int fragmentStart=relativeUrl.indexOf(FRAGMENT_SEPARATOR);
		int queryStart=relativeUrl.indexOf(QUERY_SEPARATOR);
		if(fragmentStart!=-1&&queryStart>fragmentStart)
			queryStart=-1;
		int pathEnd=Math.min(queryStart, fragmentStart);
		url.setPath((pathEnd!=-1)?relativeUrl.substring(0, pathEnd):relativeUrl);
		if(fragmentStart!=-1&&fragmentStart<relativeUrl.length()-1);
			url.setFragment(relativeUrl.substring(fragmentStart+1));
		if(queryStart!=-1&&queryStart<relativeUrl.length()-1)
			url.setQuery(url.hasFragment()?relativeUrl.substring(queryStart+1, fragmentStart):relativeUrl.substring(queryStart+1));
		return url;
	}
	
}
