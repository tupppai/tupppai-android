package com.pires.wesee.eventbus;

public class SearchEvent {

	private String search;

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public SearchEvent(String search) {
		this.search = search;
	}
}
