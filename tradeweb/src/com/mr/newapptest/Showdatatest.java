package com.mr.newapptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import com.mr.datainsert.Dataconn;
import com.mr.newapp.Finalnsebasedatadnldcarmilla;
import com.mr.newapp.Nsebasedatadnldcarmilla;
import com.mr.newdata.StockOtherTechnicals;
import com.mr.newdata.TickData;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class Showdatatest {
static int interval =600*60;
	public static void main(String args[]) throws MalformedURLException, ParseException
	{
		Dataconn dataconn =new Dataconn();
		Connection conn = dataconn.getconn();
		Nsebasedatadnldcarmilla ft = new Nsebasedatadnldcarmilla("900","2-6","21");
		StockOtherTechnicals st = ft.getstochdata("ADANIPORTS",conn);
		System.out.println(st.getDayk()+";"+st.getDayd()+";"+st.getDownvalue()+";"+st.getUpvalue());
     
	}
	
	
}
