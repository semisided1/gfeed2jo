package com.designr8.gfeed2jo;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Feedalizer {

	public static XdmNode source = null;

	// get feed from google as a string with g+ urlString 
	public static String getuserfeed( String urlString) {
		URL feedUrl = null;
		try {
			feedUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		URLConnection yc = null;
		try {
			yc = feedUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String inputLine;
		StringBuffer result = new StringBuffer();
		try {
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}


	// change the g+ xml to something simpler using xsl style
	
	// this step is completely unnecessary
	// buildAlbums using the Feed instead (its not that messy )
	public static String getcleanfeed(String feed, String style) {

		Processor proc = new Processor(false);
		XsltCompiler comp = proc.newXsltCompiler();

		// get xslt executor from local file using compiler
		XsltExecutable exp = null;
		try {
			exp = comp.compile(new StreamSource(new File(style)));
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}

		// get xdmnode document with using processor and feed(string)
		// XdmNode source = null;
		try {
			source = proc.newDocumentBuilder().build(new StreamSource(new ByteArrayInputStream(feed.getBytes(StandardCharsets.UTF_8))));
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}

		// set up output stream
		ByteArrayOutputStream stringwriter = new ByteArrayOutputStream();
		Serializer out = new Serializer();
		out.setOutputStream(stringwriter);

		// transform using executor and source xdmnode
		XsltTransformer trans = exp.load();
		trans.setInitialContextNode(source);
		trans.setDestination(out);
		try {
			trans.transform();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		return stringwriter.toString();
	}




	private static String getphotofeed(String photofeedurl) {

		return getuserfeed(photofeedurl);
	}

	public static XdmNode buildResultXdmNode(String feedstr) {
		XdmNode res = null;
		Processor proc = new Processor(false);
		try {
			res = proc.newDocumentBuilder().build(new StreamSource(new ByteArrayInputStream(feedstr.getBytes(StandardCharsets.UTF_8))));
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static List<Album> buildAlbums(String feedstr) {
		//	XdmSequenceIterator 	axisIterator(Axis axis)

		List<Album> la =  new ArrayList<Album>();
		XdmNode result = null;
		Album aaa = null;

		result = buildResultXdmNode(feedstr);
		XdmSequenceIterator iter = result.axisIterator(Axis.DESCENDANT);

		String nodename = null;

		while (iter.hasNext()) {
			XdmNode child = (XdmNode) iter.next();

			if (child==null) {
				return la;
			}

			QName gnqn = child.getNodeName();
			if (gnqn == null) {
				// probably whitespace check this out???
						continue;
			}
			nodename = gnqn.toString();


			if (child.getNodeKind() == XdmNodeKind.TEXT ) {
				continue;
			}

			if (nodename.equals("album")) {
				aaa = new Album();
				la.add(aaa);
			}

			if (nodename.equals("albumname")) {
				//	 System.out.println("*******************found albumname");
				child = (XdmNode) iter.next();
				aaa.setAlbumname(child.toString());
			}

			if (nodename.equals("numphotos")) {
				child = (XdmNode) iter.next();
				aaa.setNumphotos( Integer.parseInt(child.toString()) );
			}

			if (nodename.equals("photofeed")) {
				child = (XdmNode) iter.next();
				aaa.setPhotofeed(child.toString());
			}
		}

		return la;		
	}

	public static List<Photo> buildPhotos(String photofeed) {

		List<Photo> lp =  new ArrayList<Photo>();
		XdmNode result = null;
		Photo ppp = null;

		result = buildResultXdmNode(photofeed);
		XdmSequenceIterator iter = result.axisIterator(Axis.DESCENDANT);

		String nodename = null;

		while (iter.hasNext()) {
			XdmNode child = (XdmNode) iter.next();

			if (child==null) {
				return lp;
			}

			String srcstr = child.getAttributeValue(new QName("src"));
			if (srcstr!=null ) {
				ppp.setPurl(srcstr);
			}

			QName gnqn = child.getNodeName();
			if (gnqn == null) {
				// probably whitespace check this out???
				continue;
			}
			nodename = gnqn.toString();
			if (child.getNodeKind() == XdmNodeKind.TEXT ) {
				continue;
			}

			if (nodename.equals("entry")) {
				ppp = new Photo();
				lp.add(ppp);
			}

			if (nodename.equals("summary")) {            	 
				ppp.setCaption("" + child.getStringValue() );
			}

		}

		return lp;		
	}



	public static void main(String[] args) {
		
		String stevensalbums = "http://picasaweb.google.com/data/feed/api/user/stevenlaytonphotography?kind=album&access=public&fields=entry%28media:group/media:title,gphoto:numphotos,link[@rel=%27http%3A%2F%2Fschemas.google.com%2Fg%2F2005%23feed%27]%28@href%29%29";
		String albumsstyle = "styles/albums.xsl";

		String uf = getuserfeed(stevensalbums);		
		String cf = getcleanfeed(uf,albumsstyle);

		List<Album> la = buildAlbums(cf);

		String photofeedstr = null;
		List<Photo> lp = null;

		String photofieldselector = "?fields=entry/summary,entry/content";

		for ( Album ab : la) {
			System.out.println("+++++++++++++++++++++");
			System.out.println(ab.getAlbumname());
			photofeedstr = getphotofeed(ab.getPhotofeedurl() + photofieldselector );
			//	System.out.println("photo feed: " + photofeedstr);
			lp = buildPhotos(photofeedstr);
			for ( Photo pb : lp) {
				System.out.println("photo pojo: url: " + pb.getPurl() + " cap: " + pb.getCaption());
			}

		}

	}





}
