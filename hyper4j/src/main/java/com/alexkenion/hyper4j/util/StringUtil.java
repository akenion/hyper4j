package com.alexkenion.hyper4j.util;

import java.util.List;

public class StringUtil {
	
	public static String join(String delimiter, List<String> components) {
		StringBuilder builder=new StringBuilder();
		boolean first=true;
		for(String component:components) {
			if(!first)
				builder.append(delimiter);
			builder.append(component);
			first=false;
		}
		return builder.toString();
	}

}
