package com.science.inc.tabloid.data;

public class TagData {
	
	public long id;
	public String serverId;
	public String name;
	
	public TagData() {

	}
	
	public TagData(long id, String serverId, String name) {
		this.id = id;
		this.serverId = serverId;
		this.name = name;
	}
}
