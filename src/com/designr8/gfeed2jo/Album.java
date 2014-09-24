package com.designr8.gfeed2jo;

public class Album {

    private String albumname;  

    private int numphotos;

    private String photofeedurl;
	public String getAlbumname() {
		return albumname;
	}
	public void setAlbumname(String name) {
		this.albumname = name;
	}
	public int getNumphotos() {
		return numphotos;
	}
	public void setNumphotos(int numphotos) {
		this.numphotos = numphotos;
	}
	public String getPhotofeedurl() {
		return photofeedurl;
	}
	public void setPhotofeed(String photofeedurl) {
		this.photofeedurl = photofeedurl;
	}

	public String toString() {
		return  "pojo: " + albumname + numphotos + photofeedurl;
	}
	
}
