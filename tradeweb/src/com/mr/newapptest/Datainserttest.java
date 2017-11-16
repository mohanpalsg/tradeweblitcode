package com.mr.newapptest;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.text.ParseException;

import com.mr.datainsert.Dataconn;
import com.mr.newapp.Nsebasedatadnldcarmilla;
import com.mr.newapp.Nsebasedownloader;

public class Datainserttest {

	public static void main(String args[])
	{
		//Thread thread = new Thread(new Nsebasedownloader("300"));
		//thread.start();
		Dataconn dataconn = new Dataconn();
		Connection conn = dataconn.getconn();
		Nsebasedatadnldcarmilla crm = new Nsebasedatadnldcarmilla("600");
		try {
			crm.getstochdata("ITC", conn);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataconn.closeconn();
	}
}
